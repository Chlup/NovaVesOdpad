//
//  TasksManagerImpl.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor protocol TasksManager {
    func addTask(id: String, _ operation: @escaping () async -> Void)
    func cancelTask(id: String)
}

extension Container { 
    var tasksManager: Factory<TasksManager> { self { TasksManagerImpl() }.singleton }
}

@MainActor final class TasksManagerImpl {
    private var tasks: [String: Task<Void, Never>] = [:]

    private func cancelAllTasks() {
        tasks.values.forEach { $0.cancel() }
        tasks.removeAll()
    }

    deinit {
        Task { [weak self] in
            await self?.cancelAllTasks()
        }
    }
}

extension TasksManagerImpl: TasksManager {
    func addTask(id: String, _ operation: @escaping () async -> Void) {
        let task = Task { [weak self] in
            await operation()
            Task { @MainActor in
                self?.tasks.removeValue(forKey: id)
            }
        }
        tasks[id] = task
    }

    func cancelTask(id: String) {
        tasks[id]?.cancel()
        tasks.removeValue(forKey: id)
    }
}
