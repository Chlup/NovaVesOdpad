//
//  DaysListModel.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class DaysListState {
    var allDays: [TrashDay]
    var months: [TrashMonth] = []

    init(allDays: [TrashDay]) {
        self.allDays = allDays
    }
}

@MainActor protocol DaysListModel {
    var coordinator: DaysListCoordinator { get }

    func onAppear()
    func titleForDay(_ date: Date) -> String
    func titleForMonth(_ date: Date) -> String
}

@MainActor final class DaysListModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger

    let groupDaysTaskID = "groupDaysTaskID"

    let state: DaysListState
    let coordinator: DaysListCoordinator

    let monthTitleFormatter: DateFormatter
    let dayTitleDateFormatter: DateFormatter

    init(state: DaysListState, coordinator: DaysListCoordinator) {
        self.coordinator = coordinator
        self.state = state

        dayTitleDateFormatter = DateFormatter()
        dayTitleDateFormatter.dateFormat = "d.M.YYYY"

        monthTitleFormatter = DateFormatter()
        monthTitleFormatter.dateFormat = "LLLL YYYY"
    }

    func groupDays() {
        let allDays = state.allDays
        guard !allDays.isEmpty else { return }

        let calendar = Calendar.current

        var months: [TrashMonth] = []
        var daysForMonth: [TrashDay] = []
        for day in allDays {
            guard !daysForMonth.isEmpty else {
                daysForMonth.append(day)
                continue
            }

            guard let previousDay = daysForMonth.last, let firstDay = daysForMonth.first else { break }

            let previousDayMonth = calendar.component(.month, from: previousDay.date)
            let currentDayMonth = calendar.component(.month, from: day.date)

            if previousDayMonth != currentDayMonth {
                let month = TrashMonth(date: firstDay.date, days: daysForMonth)
                months.append(month)
                daysForMonth = [day]

            } else {
                daysForMonth.append(day)
            }
        }

        if !daysForMonth.isEmpty, let firstDay = daysForMonth.first  {
            let month = TrashMonth(date: firstDay.date, days: daysForMonth)
            months.append(month)
        }

        state.months = months
    }
}

extension DaysListModelImpl: DaysListModel {
    func onAppear() {
        tasks.cancelTask(id: groupDaysTaskID)
        tasks.addTask(id: groupDaysTaskID, groupDays)
    }

    func titleForDay(_ date: Date) -> String {
        let result = dayTitleDateFormatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }

    func titleForMonth(_ date: Date) -> String {
        let result = monthTitleFormatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }
}
