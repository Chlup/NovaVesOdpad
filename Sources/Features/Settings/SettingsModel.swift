//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor protocol SettingsModel: Observable {
    var coordinator: SettingsCoordinator { get }
    var noticationEnabledThreeDaysBefore: Bool { get nonmutating set }
    var noticationEnabledTwoDaysBefore: Bool { get nonmutating set }
    var noticationEnabledOneDaysBefore: Bool { get nonmutating set }
    var noticationEnabledOnDay: Bool { get nonmutating set }

    func notifSettingsChanged()
}

@MainActor @Observable final class SettingsModelImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    private enum Constants {
        static let notificationEnabledThreeDaysBeforeUDKey = "noticationEnabledThreeDaysBefore"
        static let notificationEnabledTwoDaysBeforeUDKey = "noticationEnabledTwoDaysBefore"
        static let notificationEnabledOneDaysBeforeUDKey = "notificationEnabledOneDaysBefore"
        static let notificationEnabledOnDayUDKey = "notificationEnabledOnDay"
        static let selectedNotificationHourUDKey = "selectedNotificationHour"
    }

    let notificationHours: [Int]
    var selectedNotificationHour: Int
    var noticationEnabledThreeDaysBefore: Bool
    var noticationEnabledTwoDaysBefore: Bool
    var noticationEnabledOneDaysBefore: Bool
    var noticationEnabledOnDay: Bool

    let coordinator: SettingsCoordinator

    init(coordinator: SettingsCoordinator) {
        self.coordinator = coordinator

        let userDefault = UserDefaults.standard
        noticationEnabledThreeDaysBefore = userDefault.bool(forKey: Constants.notificationEnabledThreeDaysBeforeUDKey)
        noticationEnabledTwoDaysBefore = userDefault.bool(forKey: Constants.notificationEnabledTwoDaysBeforeUDKey)
        noticationEnabledOneDaysBefore = userDefault.bool(forKey: Constants.notificationEnabledOneDaysBeforeUDKey)
        noticationEnabledOnDay = userDefault.bool(forKey: Constants.notificationEnabledOnDayUDKey)

        var selectedNotificationHour = userDefault.integer(forKey: Constants.selectedNotificationHourUDKey)
        if selectedNotificationHour == 0 {
            selectedNotificationHour = 8
        }
        self.selectedNotificationHour = selectedNotificationHour
        notificationHours = [5, 6, 7, 8, 9, 10]
    }
}

extension SettingsModelImpl: SettingsModel {
    func notifSettingsChanged() {
        let userDefault = UserDefaults.standard
        userDefault.set(noticationEnabledThreeDaysBefore, forKey: Constants.notificationEnabledThreeDaysBeforeUDKey)
        userDefault.set(noticationEnabledTwoDaysBefore, forKey: Constants.notificationEnabledTwoDaysBeforeUDKey)
        userDefault.set(noticationEnabledOneDaysBefore, forKey: Constants.notificationEnabledOneDaysBeforeUDKey)
        userDefault.set(noticationEnabledOnDay, forKey: Constants.notificationEnabledOnDayUDKey)
        userDefault.set(selectedNotificationHour, forKey: Constants.selectedNotificationHourUDKey)

        logger.debug("Changed")
    }
}
