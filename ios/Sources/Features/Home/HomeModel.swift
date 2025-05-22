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
        let (nextWednesday, nextWednesdayIsToday) = nextWednesday()

        let calendar = Calendar.current

        var days: [TrashDay] = []
        for i in 0...52 {
            guard let date = calendar.date(byAdding: .day, value: i * 7, to: nextWednesday) else { continue }

            let weekNumber = calendar.component(.weekOfYear, from: date)

            let bins: [TrashDay.Bin]
            if weekNumber % 2 == 0 {
                bins = [.mix, .paper, .bio]
            } else {
                bins = [.mix, .plastic]
            }

            let day = TrashDay(date: date, isToday: i == 0 ? nextWednesdayIsToday : false, bins: bins)
            days.append(day)
        }

        self.state.days = days
    }

    private func nextWednesday() -> (date: Date, isToday: Bool) {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: Date())

        // Get the current weekday (1 = Sunday, 2 = Monday, ..., 4 = Wednesday, ..., 7 = Saturday)
        let currentWeekday = calendar.component(.weekday, from: today)
        let wednesdayWeekday = 4 // Wednesday

        // If today is Wednesday, return today with isToday = true
        if currentWeekday == wednesdayWeekday {
            return (date: today, isToday: true)
        }

        // Calculate days until next Wednesday
        let daysUntilWednesday = (wednesdayWeekday - currentWeekday + 7) % 7
        let nextWednesdayDate = calendar.date(byAdding: .day, value: daysUntilWednesday, to: today) ?? today

        return (date: nextWednesdayDate, isToday: false)
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
