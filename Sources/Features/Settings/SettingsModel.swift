//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class SettingsModelState {
    let notificationHours: [Int]
    var selectedNotificationHour: Int
    var noticationEnabledThreeDaysBefore: Bool
    var noticationEnabledTwoDaysBefore: Bool
    var noticationEnabledOneDaysBefore: Bool
    var noticationEnabledOnDay: Bool

    fileprivate enum Constants {
        static let notificationEnabledThreeDaysBeforeUDKey = "noticationEnabledThreeDaysBefore"
        static let notificationEnabledTwoDaysBeforeUDKey = "noticationEnabledTwoDaysBefore"
        static let notificationEnabledOneDaysBeforeUDKey = "notificationEnabledOneDaysBefore"
        static let notificationEnabledOnDayUDKey = "notificationEnabledOnDay"
        static let selectedNotificationHourUDKey = "selectedNotificationHour"
    }

    init() {
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

@MainActor protocol SettingsModel: AnyObject {
    var coordinator: SettingsCoordinator { get }
    func notifSettingsChanged()
}

@MainActor final class SettingsModelImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: SettingsModelState
    let coordinator: SettingsCoordinator

    init(state: SettingsModelState, coordinator: SettingsCoordinator) {
        self.coordinator = coordinator
        self.state = state
    }
}

extension SettingsModelImpl: SettingsModel {
    func notifSettingsChanged() {
        let userDefault = UserDefaults.standard
        userDefault.set(state.noticationEnabledThreeDaysBefore, forKey: SettingsModelState.Constants.notificationEnabledThreeDaysBeforeUDKey)
        userDefault.set(state.noticationEnabledTwoDaysBefore, forKey: SettingsModelState.Constants.notificationEnabledTwoDaysBeforeUDKey)
        userDefault.set(state.noticationEnabledOneDaysBefore, forKey: SettingsModelState.Constants.notificationEnabledOneDaysBeforeUDKey)
        userDefault.set(state.noticationEnabledOnDay, forKey: SettingsModelState.Constants.notificationEnabledOnDayUDKey)
        userDefault.set(state.selectedNotificationHour, forKey: SettingsModelState.Constants.selectedNotificationHourUDKey)

        logger.debug("Changed")
    }
}
