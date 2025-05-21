//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class HomeModelState {
    var days: [TrashDay] = []
}

@MainActor protocol HomeModel {
    var coordinator: HomeCoordinator { get }

    func loadData()
    func titleForDay(_ date: Date) -> String
}

@MainActor final class HomeModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: HomeModelState
    let coordinator: HomeCoordinator

    private let loadDaysTaskID = UUID().uuidString
    private let dayTitleDateFormatter: DateFormatter

    init(state: HomeModelState, coordinator: HomeCoordinator) {
        self.coordinator = coordinator
        self.state = state
        dayTitleDateFormatter = DateFormatter()
        dayTitleDateFormatter.dateFormat = "EEEE dd. MM. yyyy"
        dayTitleDateFormatter.locale = Locale.current
    }

    private func loadDays() {
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "dd.MM.yyyy HH:mm:ss"

        let jsonDecoder = JSONDecoder()
        jsonDecoder.dateDecodingStrategy = .formatted(dateFormatter)

        guard let jsonURL = Bundle.main.url(forResource: "calendar", withExtension: "json") else {
            logger.debug("Can't find calendar file in resources.")
            return
        }

        do {
            let now = Date()
            let data = try Data(contentsOf: jsonURL)
            let days = try jsonDecoder.decode([TrashDay].self, from: data)
                .filter { $0.date >= now }

            updateDays(days)
        } catch {
            logger.debug("Failed to load calendar file: \(error)")
        }
    }

    private func updateDays(_ days: [TrashDay]) {
        state.days = days
    }
}

extension HomeModelImpl: HomeModel {
    func loadData() {
        tasks.addTask(id: loadDaysTaskID, loadDays)
    }

    func titleForDay(_ date: Date) -> String {
        let result = dayTitleDateFormatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }
}
