//
//  DaysListStore.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import Foundation
import Dependencies
import Factory

@Reducer
struct DaysList {
    @Dependency(\.dismiss) var dismiss
    @ObservationIgnored @Injected(\.logger) private var logger

    @ObservableState
    struct State: Equatable {
        let allDays: [TrashDay]
        var titlesForDay: [TrashDay.ID: String] = [:]
        var months: [TrashMonth] = []
        var titlesForMonth: [TrashMonth.ID: String] = [:]
    }

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case daysGrouped(_ months: [TrashMonth], _ titlesForMonth: [TrashMonth.ID: String], _ titlesForDay: [TrashDay.ID: String])
        case dismiss
    }

    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in return doReduce(into: &state, action: action) }
    }

    private func doReduce(into state: inout State, action: Action) -> Effect<Action> {
        switch action {
        case .onAppear:
            return handleOnAppear(&state)
        case let .daysGrouped(months, titlesForMonth, titlesForDay):
            return handleDaysGrouped(&state, months, titlesForMonth, titlesForDay)
        case .dismiss:
            return handleDismiss(&state)
        case .binding:
            return .none
        }
    }

    private func handleOnAppear(_ state: inout State) -> Effect<Action> {
        let allDays = state.allDays
        return .run { send in
            let calendar = Calendar.current

            var months: [TrashMonth] = []
            var titlesForMonths: [TrashMonth.ID: String] = [:]
            var titlesForDay: [TrashDay.ID: String] = [:]
            var daysForMonth: [TrashDay] = []

            let monthTitleFormatter = DateFormatter()
            monthTitleFormatter.dateFormat = "LLLL YYYY"

            let dayTitleDateFormatter = DateFormatter()
            dayTitleDateFormatter.dateFormat = "d.M.YYYY"

            for day in allDays {
                titlesForDay[day.id] = Self.titleForDate(formatter: dayTitleDateFormatter, date: day.date)

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

                    titlesForMonths[month.id] = Self.titleForDate(formatter: monthTitleFormatter, date: month.date)

                } else {
                    daysForMonth.append(day)
                }
            }

            if !daysForMonth.isEmpty, let firstDay = daysForMonth.first  {
                let month = TrashMonth(date: firstDay.date, days: daysForMonth)
                months.append(month)
                titlesForMonths[month.id] = Self.titleForDate(formatter: monthTitleFormatter, date: month.date)
            }

            await send(.daysGrouped(months, titlesForMonths, titlesForDay))
        }
    }

    private func handleDaysGrouped(
        _ state: inout State,
        _ months: [TrashMonth],
        _ titlesForMonth: [TrashMonth.ID: String],
        _ titlesForDay: [TrashDay.ID: String]
    ) -> Effect<Action> {
        state.months = months
        state.titlesForMonth = titlesForMonth
        state.titlesForDay = titlesForDay
        return .none
    }

    private func handleDismiss(_ state: inout State) -> Effect<Action> {
        return .run { _ in await dismiss() }
    }

    // MARK: - Misc

    private static func titleForDate(formatter: DateFormatter, date: Date) -> String {
        let result = formatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }
}
