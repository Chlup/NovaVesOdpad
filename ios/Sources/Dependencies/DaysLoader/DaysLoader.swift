//
//  DaysLoader.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 03.06.2025.
//

import Foundation
import Factory

extension Container { var daysLoader: Factory<DaysLoader> { self { DaysLoaderImpl() }.singleton } }

protocol DaysLoader: Sendable {
    func nextWednesday(from date: Date) -> Date
    func load() -> [TrashDay]
}

struct DaysLoaderImpl { }

extension DaysLoaderImpl: DaysLoader {
    func nextWednesday(from date: Date) -> Date {
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

    func load() -> [TrashDay] {
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

        addHeavyLoadDay(calendar: calendar, now: now, components: DateComponents(year: 2025, month: 9, day: 6), to: &days)
        addHeavyLoadDay(calendar: calendar, now: now, components: DateComponents(year: 2025, month: 10, day: 4), to: &days)
        addHeavyLoadDay(calendar: calendar, now: now, components: DateComponents(year: 2025, month: 11, day: 8), to: &days)

        return days.sorted(by: { $0.date < $1.date })
    }

    func addHeavyLoadDay(calendar: Calendar, now: Date, components: DateComponents, to days: inout [TrashDay]) {
        let date = Calendar.current.date(from: components)!
        let daysDifferenceToToday = now.daysDifference(to: date)
        let day = TrashDay(date: date, daysDifferenceToToday: daysDifferenceToToday, bins: [.heavyLoad])
        days.append(day)
    }
}

