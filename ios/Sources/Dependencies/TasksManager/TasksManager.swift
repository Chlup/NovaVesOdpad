//
//  TasksManagerImpl.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory
import Combine

@MainActor protocol TasksManager {
    func addTask(id: String, _ operation: @escaping () async -> Void)
    func cancelTask(id: String)
    func cancelTaskAndWait(id: String) async
}

extension Container {
    var tasksManager: Factory<TasksManager> { self { TasksManagerImpl() }.singleton }
}

@MainActor final class TasksManagerImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    enum TaskEvent {
        case finished(String)
        case canceled(String)

        var id: String {
            switch self {
            case let .finished(id):
                return id
            case let .canceled(id):
                return id
            }
        }
    }

    private var tasks: [String: Task<Void, Never>] = [:]

    private nonisolated let taskSubject = SendablePassthroughSubject<TaskEvent, Never>()
    private nonisolated var taskStream: AnyPublisher<TaskEvent, Never> { taskSubject.eraseToAnyPublisher() }

    private func cancelAllTasks() {
        tasks.values.forEach { $0.cancel() }
        tasks.removeAll()
    }

    deinit {
        Task { [weak self] in
            await self?.cancelAllTasks()
        }
    }

    private func waintUniltaskIsCanceledOrFinished(_ id: String) async {
        let stream = self.taskStream
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            let cancellable = UncheckedSendableMutableBox<AnyCancellable?>(nil)
            cancellable.item = stream
                .sink(
                    receiveValue: { taskEvent in
                        guard taskEvent.id == id else { return }
                        continuation.resume()
                        cancellable.item = nil
                    }
                )
        }
    }
}

extension TasksManagerImpl: TasksManager {
    func addTask(id: String, _ operation: @escaping () async -> Void) {
        let task = Task { [weak self] in
            await operation()

            self?.tasks.removeValue(forKey: id)

            if Task.isCancelled {
                self?.taskSubject.send(.canceled(id))
            } else {
                self?.taskSubject.send(.finished(id))
            }
        }

        tasks[id] = task
    }

    func cancelTask(id: String) {
        tasks[id]?.cancel()
    }

    func cancelTaskAndWait(id: String) async {
        guard tasks[id] != nil else { return }
        cancelTask(id: id)
        await waintUniltaskIsCanceledOrFinished(id)
    }
}
