//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class HomeModelState {
    var firstDay: TrashDay? {
        didSet { print("Did set first day") }
    }
    var homeDays: [TrashDay] = []
    var allDays: [TrashDay] = []
}

@MainActor protocol HomeModel {
    var coordinator: HomeCoordinator { get }

    func loadData()
    func titleForNextDay(_ date: Date) -> String
    func titleForDay(_ date: Date) -> String
    func daysToNextTrashDayText(numberOfDays: Int) -> String
}

@MainActor final class HomeModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: HomeModelState
    let coordinator: HomeCoordinator

    private let loadDaysTaskID = UUID().uuidString
    private let dayTitleDateFormatter: DateFormatter
    private let nextTrashDayFormatter: DateFormatter

    init(state: HomeModelState, coordinator: HomeCoordinator) {
        self.coordinator = coordinator
        self.state = state
        dayTitleDateFormatter = DateFormatter()
        dayTitleDateFormatter.dateFormat = "dd. MMMM"

        nextTrashDayFormatter = DateFormatter()
        nextTrashDayFormatter.dateFormat = "EEEE, dd. MMMM"
    }

    private func loadDays() {
        let now = Date()
        let nextWednesday = nextWednesday(from: now)

        let calendar = Calendar.current

        var days: [TrashDay] = []
        for i in 0...52 {
            guard let dayDate = calendar.date(byAdding: .day, value: i * 7, to: nextWednesday) else { continue }

            let daysDifferenceToToday = now.daysDifference(to: dayDate)
            let weekNumber = calendar.component(.weekOfYear, from: dayDate)

            let bins: [TrashDay.Bin]
            if weekNumber % 2 == 0 {
                bins = [.paper, .bio, .mix]
            } else {
                bins = [.plastic, .mix]
            }

            let day = TrashDay(date: dayDate, daysDifferenceToToday: daysDifferenceToToday, bins: bins)
            days.append(day)
        }

        self.state.allDays = days
        self.state.firstDay = days.first
        self.state.homeDays = Array(days[1...3])
    }

    private func nextWednesday(from date: Date) -> Date {
        let calendar = Calendar.current
        let today = calendar.startOfDay(for: date)

        // Get the current weekday (1 = Sunday, 2 = Monday, ..., 4 = Wednesday, ..., 7 = Saturday)
        let currentWeekday = calendar.component(.weekday, from: today)
        let wednesdayWeekday = 4 // Wednesday

        // If today is Wednesday, return today with isToday = true
        if currentWeekday == wednesdayWeekday {
            return today
        }

        // Calculate days until next Wednesday
        let daysUntilWednesday = (wednesdayWeekday - currentWeekday + 7) % 7
        let nextWednesdayDate = calendar.date(byAdding: .day, value: daysUntilWednesday, to: today) ?? today

        return nextWednesdayDate
    }
}

extension HomeModelImpl: HomeModel {
    func loadData() {
        tasks.addTask(id: loadDaysTaskID, loadDays)
    }

    func titleForNextDay(_ date: Date) -> String {
        return nextTrashDayFormatter.string(from: date)
    }

    func titleForDay(_ date: Date) -> String {
        let result = dayTitleDateFormatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }

    func daysToNextTrashDayText(numberOfDays: Int) -> String {
        switch numberOfDays {
        case 0:
            return "dnes"
        case 1:
            return "za \(numberOfDays) den"
        case 2...4:
            return "za \(numberOfDays) dny"
        default:
            return "za \(numberOfDays) dnů"
        }
    }
}
