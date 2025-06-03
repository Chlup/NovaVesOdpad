//
//  NotificationsBuilder.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 21.05.2025.
//

import Foundation
import Factory
@preconcurrency import UserNotifications

struct NotificationBuilderInput {
    let days: [TrashDay]
    let noticationEnabledThreeDaysBefore: Bool
    let selectedNotificationHourThreeDaysBefore: Int
    let noticationEnabledTwoDaysBefore: Bool
    let selectedNotificationHourTwoDaysBefore: Int
    let noticationEnabledOneDayBefore: Bool
    let selectedNotificationHourOneDayBefore: Int
    let noticationEnabledOnDay: Bool
    let selectedNotificationHourOnDay: Int
}

protocol NotificationsBuilder: Sendable {
    func build(input: NotificationBuilderInput) async
    func cancelAllNotifications()
}

extension Container {
    var notificationsBuilder: Factory<NotificationsBuilder> { self { NotificationsBuilderImpl() }.singleton }
}

struct NotificationsBuilderImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    private func scheduleNotification(day: TrashDay, input: NotificationBuilderInput) -> [UNNotificationRequest] {
        var requests: [UNNotificationRequest] = []

        if input.noticationEnabledThreeDaysBefore {
            if let request = scheduleNotification(
                day: day,
                offset: -(3 * 24 * 3600),
                hour: input.selectedNotificationHourThreeDaysBefore,
                title: "Odvoz odpadu se blíží",
                subtitle: "Za tři dny se budou vyvážet popelnice."
            ) {
                requests.append(request)
            }
        }

        if input.noticationEnabledTwoDaysBefore {
            if let request = scheduleNotification(
                day: day,
                offset: -(2 * 24 * 3600),
                hour: input.selectedNotificationHourTwoDaysBefore,
                title: "Odvoz odpadu se blíží",
                subtitle: "Za dva dny se budou vyvážet popelnice."
            ) {
                requests.append(request)
            }
        }

        if input.noticationEnabledOneDayBefore {
            if let request = scheduleNotification(
                day: day,
                offset: -(1 * 24 * 3600),
                hour: input.selectedNotificationHourOneDayBefore,
                title: "Odvoz odpadu je již skoro tady",
                subtitle: "Zítra se budou vyvážet popelnice:"
            ) {
                requests.append(request)
            }
        }

        if input.noticationEnabledOnDay {
            if let request = scheduleNotification(
                day: day,
                offset: 0,
                hour: input.selectedNotificationHourOnDay,
                title: "Dnes se vyváží odpad",
                subtitle: "Dnes se budou vyvážet popelnice."
            ) {
                requests.append(request)
            }
        }

        return requests
    }

    private func scheduleNotification(day: TrashDay, offset: TimeInterval, hour: Int, title: String, subtitle: String) -> UNNotificationRequest? {
        guard !Task.isCancelled else { return nil }

        let now = Date()
        guard let scheduleDate = createDateWithSameDay(originalDate: day.date.addingTimeInterval(offset), newHour: hour) else {
            logger.debug("Can't create schedule date for day \(day) \(offset) \(hour)")
            return nil
        }

        logger.debug("Now: \(now), ScheduleDate: \(scheduleDate), Is past: \(scheduleDate <= now)")
        
        // Skip past dates
        guard scheduleDate > now else { 
            logger.debug("⚠️ Skipping notification for \(scheduleDate) as it's in the past")
            return nil 
        }

        logger.debug("✅ Scheduling notification for day \(day) offset \(offset) hour \(hour) \(scheduleDate)")

        let content = UNMutableNotificationContent()
        content.title = title
        content.subtitle = subtitle
        content.body = "\(day.bins.map { $0.title }.joined(separator: "\n"))"
        content.sound = .default

        // Include minute and second to ensure precise triggering
        let scheduleDateComponents = Calendar.current.dateComponents([.year, .month, .day, .hour, .minute, .second], from: scheduleDate)
        logger.debug("Trigger components: \(scheduleDateComponents)")
        let trigger = UNCalendarNotificationTrigger(dateMatching: scheduleDateComponents, repeats: false)
        
        // Log next trigger date to confirm it's working
        if let nextTriggerDate = trigger.nextTriggerDate() {
            logger.debug("Next trigger date: \(nextTriggerDate)")
        } else {
            logger.debug("⚠️ Failed to get next trigger date")
        }

        let identifier = UUID().uuidString
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        logger.debug("Created notification request with ID: \(identifier)")
        return request
    }

    private func createDateWithSameDay(originalDate: Date, newHour: Int) -> Date? {
        let calendar = Calendar.current

        let components = calendar.dateComponents([.year, .month, .day], from: originalDate)

        // Create a new DateComponents with the same day but different hour
        var newComponents = components
        newComponents.hour = newHour
        newComponents.minute = 0
        newComponents.second = 0

        // Create and return the new date
        return calendar.date(from: newComponents)
    }
}

extension NotificationsBuilderImpl: NotificationsBuilder {
    func build(input: NotificationBuilderInput) async {
        logger.debug("NotificationsBuilderImpl running")

        cancelAllNotifications()
        let center = UNUserNotificationCenter.current()

        let count = min(5, input.days.count)

//        for day in input.days {
        for i in 0..<count {
            let day = input.days[i]
            if Task.isCancelled { break }
            let dayRequests = scheduleNotification(day: day, input: input)
            for request in dayRequests {
                if Task.isCancelled { break }
                do {
                    logger.debug("Adding notif request for day \(day)")
                    try await center.add(request)
                } catch {
                    logger.debug("Failed to add local notification request: \(error)")
                }
            }
        }

        logger.debug("Finished running")
    }

    func cancelAllNotifications() {
        logger.debug("Canceling all notifications")
        let center = UNUserNotificationCenter.current()
        center.removeAllPendingNotificationRequests()
        center.removeAllDeliveredNotifications()
    }
}
