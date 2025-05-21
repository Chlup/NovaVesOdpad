//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory
@preconcurrency import UserNotifications

@MainActor @Observable final class SettingsModelState {
    var notificationsAuthorized: Bool = false
    let notificationHours: [Int]
    var noticationEnabledThreeDaysBefore: Bool
    var selectedNotificationHourThreeDaysBefore: Int
    var noticationEnabledTwoDaysBefore: Bool
    var selectedNotificationHourTwoDaysBefore: Int
    var noticationEnabledOneDayBefore: Bool
    var selectedNotificationHourOneDayBefore: Int
    var noticationEnabledOnDay: Bool
    var selectedNotificationHourOnDay: Int

    var noticiationsEnabledForAnyDay: Bool {
        return
            noticationEnabledThreeDaysBefore ||
            noticationEnabledTwoDaysBefore ||
            noticationEnabledOneDayBefore ||
            noticationEnabledOnDay
    }

    fileprivate enum Constants {
        static let notificationEnabledThreeDaysBeforeUDKey = "noticationEnabledThreeDaysBefore"
        static let notificationEnabledTwoDaysBeforeUDKey = "noticationEnabledTwoDaysBefore"
        static let notificationEnabledOneDayBeforeUDKey = "notificationEnabledOneDayBefore"
        static let notificationEnabledOnDayUDKey = "notificationEnabledOnDay"
        static let selectedNotificationHourThreeDaysBeforeUDKey = "selectedNotificationHourThreeDaysBefore"
        static let selectedNotificationHourTwoDaysBeforeUDKey = "selectedNotificationHourTwoDaysBefore"
        static let selectedNotificationHourOneDayBeforeUDKey = "selectedNotificationHourOneDayBefore"
        static let selectedNotificationHourOnDayUDKey = "selectedNotificationHourOnDay"
    }

    init() {
        let userDefault = UserDefaults.standard
        noticationEnabledThreeDaysBefore = userDefault.bool(forKey: Constants.notificationEnabledThreeDaysBeforeUDKey)
        noticationEnabledTwoDaysBefore = userDefault.bool(forKey: Constants.notificationEnabledTwoDaysBeforeUDKey)
        noticationEnabledOneDayBefore = userDefault.bool(forKey: Constants.notificationEnabledOneDayBeforeUDKey)
        noticationEnabledOnDay = userDefault.bool(forKey: Constants.notificationEnabledOnDayUDKey)

        var selectedNotificationHourThreeDaysBefore = userDefault.integer(forKey: Constants.selectedNotificationHourThreeDaysBeforeUDKey)
        if selectedNotificationHourThreeDaysBefore == 0 {
            selectedNotificationHourThreeDaysBefore = 8
        }
        self.selectedNotificationHourThreeDaysBefore = selectedNotificationHourThreeDaysBefore

        var selectedNotificationHourTwoDaysBefore = userDefault.integer(forKey: Constants.selectedNotificationHourTwoDaysBeforeUDKey)
        if selectedNotificationHourTwoDaysBefore == 0 {
            selectedNotificationHourTwoDaysBefore = 8
        }
        self.selectedNotificationHourTwoDaysBefore = selectedNotificationHourTwoDaysBefore

        var selectedNotificationHourOneDayBefore = userDefault.integer(forKey: Constants.selectedNotificationHourOneDayBeforeUDKey)
        if selectedNotificationHourOneDayBefore == 0 {
            selectedNotificationHourOneDayBefore = 8
        }
        self.selectedNotificationHourOneDayBefore = selectedNotificationHourOneDayBefore

        var selectedNotificationHourOnDay = userDefault.integer(forKey: Constants.selectedNotificationHourOnDayUDKey)
        if selectedNotificationHourOnDay == 0 {
            selectedNotificationHourOnDay = 8
        }
        self.selectedNotificationHourOnDay = selectedNotificationHourOnDay

        notificationHours = Array(5...23)
    }
}

@MainActor protocol SettingsModel: AnyObject {
    var coordinator: SettingsCoordinator { get }
    
    func onAppear()
    func onDisappear()
    func notifSettingsChanged()
}

@MainActor final class SettingsModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger
    @ObservationIgnored @Injected(\.notificationsBuilder) private var notificationsBuilder

    private let scheduleNotificationsTaskID = UUID().uuidString

    let state: SettingsModelState
    let coordinator: SettingsCoordinator
    let days: [TrashDay]

    init(state: SettingsModelState, coordinator: SettingsCoordinator, days: [TrashDay]) {
        self.coordinator = coordinator
        self.state = state
        self.days = days
    }

    private func loadNotificationsAuthorizationStatus() async {
        let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
        state.notificationsAuthorized =
            notificationSettings.authorizationStatus == .authorized ||
            notificationSettings.authorizationStatus == .notDetermined
    }

    private func requestNotificationAuthorization() async {
        do {
            if try await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) {
                logger.debug("Notification permission granted")
            } else {
                logger.debug("Notification permission denied")
            }
        } catch {
            logger.debug("Failed to request notifications authorization: \(error)")
        }

        await loadNotificationsAuthorizationStatus()
    }

    private func scheduleNotifications() async {
        logger.debug("Starting to schedule notifications")
        
        // Check and log authorization status
        let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
        logger.debug("Notification auth status: \(notificationSettings.authorizationStatus.rawValue)")

        guard notificationSettings.authorizationStatus == .authorized else {
            logger.debug("⚠️ User didn't authorize notifications. Can't schedule.")
            return
        }

        let input = NotificationBuilderInput(
            days: days,
            noticationEnabledThreeDaysBefore: state.noticationEnabledThreeDaysBefore,
            selectedNotificationHourThreeDaysBefore: state.selectedNotificationHourThreeDaysBefore,
            noticationEnabledTwoDaysBefore: state.noticationEnabledTwoDaysBefore,
            selectedNotificationHourTwoDaysBefore: state.selectedNotificationHourTwoDaysBefore,
            noticationEnabledOneDayBefore: state.noticationEnabledOneDayBefore,
            selectedNotificationHourOneDayBefore: state.selectedNotificationHourOneDayBefore,
            noticationEnabledOnDay: state.noticationEnabledOnDay,
            selectedNotificationHourOnDay: state.selectedNotificationHourOnDay
        )
        await notificationsBuilder.build(input: input)
    }
}

extension SettingsModelImpl: SettingsModel {
    func onAppear() {
        tasks.addTask(id: "loadAuth", loadNotificationsAuthorizationStatus)
    }

    func onDisappear() {
        tasks.cancelTask(id: scheduleNotificationsTaskID)
        tasks.addTask(id: scheduleNotificationsTaskID, scheduleNotifications)
    }

    func notifSettingsChanged() {
        let userDefault = UserDefaults.standard
        userDefault.set(state.noticationEnabledThreeDaysBefore, forKey: SettingsModelState.Constants.notificationEnabledThreeDaysBeforeUDKey)
        userDefault.set(state.noticationEnabledTwoDaysBefore, forKey: SettingsModelState.Constants.notificationEnabledTwoDaysBeforeUDKey)
        userDefault.set(state.noticationEnabledOneDayBefore, forKey: SettingsModelState.Constants.notificationEnabledOneDayBeforeUDKey)
        userDefault.set(state.noticationEnabledOnDay, forKey: SettingsModelState.Constants.notificationEnabledOnDayUDKey)
        userDefault.set(
            state.selectedNotificationHourThreeDaysBefore,
            forKey: SettingsModelState.Constants.selectedNotificationHourThreeDaysBeforeUDKey
        )
        userDefault.set(
            state.selectedNotificationHourTwoDaysBefore,
            forKey: SettingsModelState.Constants.selectedNotificationHourTwoDaysBeforeUDKey
        )
        userDefault.set(
            state.selectedNotificationHourOneDayBefore,
            forKey: SettingsModelState.Constants.selectedNotificationHourOneDayBeforeUDKey
        )
        userDefault.set(
            state.selectedNotificationHourOnDay,
            forKey: SettingsModelState.Constants.selectedNotificationHourOnDayUDKey
        )

        if state.noticiationsEnabledForAnyDay {
            tasks.addTask(id: "notifAuth", requestNotificationAuthorization)
        }

        logger.debug("Changed")
    }
}
