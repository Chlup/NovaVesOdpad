//
//  SettingsStorage.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import Foundation
import Factory
@preconcurrency import UserNotifications

protocol SettingsStorage: Sendable {
    var notificationEnabled: Bool { get }
    var notificationDaysOffset: Int { get }
    var selectedNotificationHour: Int { get }

    func updateNotificationEnabled(_ enabled: Bool)
    func updateNotificationDaysOffset(_ daysOffset: Int)
    func updateSelectedNotificationHour(_ hour: Int)
}

extension Container {
    var settingsStorage: Factory<SettingsStorage> { self { SettingsStorageImpl() }.singleton }
}

struct SettingsStorageImpl {
    fileprivate enum Constants {
        static let notificationEnabledUDKey = "notificationEnabled"
        static let notificationDaysOffsetUDKey = "notificationDaysOffset"
        static let selectedNotificationHourUDKey = "selectedNotificationHour"
    }
}

extension SettingsStorageImpl: SettingsStorage {
    var notificationEnabled: Bool { UserDefaults.standard.bool(forKey: Constants.notificationEnabledUDKey) }
    var notificationDaysOffset: Int { UserDefaults.standard.integer(forKey: Constants.notificationDaysOffsetUDKey) }
    var selectedNotificationHour: Int {
        let selectedNotificationHour = UserDefaults.standard.integer(forKey: Constants.selectedNotificationHourUDKey)
        return selectedNotificationHour == 0 ? 8 : selectedNotificationHour
    }

    func updateNotificationEnabled(_ enabled: Bool) {
        UserDefaults.standard.set(enabled, forKey: Constants.notificationEnabledUDKey)
    }

    func updateNotificationDaysOffset(_ daysOffset: Int) {
        UserDefaults.standard.set(daysOffset, forKey: Constants.notificationDaysOffsetUDKey)
    }

    func updateSelectedNotificationHour(_ hour: Int) {
        UserDefaults.standard.set(hour, forKey: Constants.selectedNotificationHourUDKey)
    }
}
