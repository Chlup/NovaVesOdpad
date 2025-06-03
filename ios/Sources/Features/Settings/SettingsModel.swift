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
    var notificationsAuthorized = true
    var schedulingNotificationsInProgress = false
    let notificationHours: [Int]
    var notificationEnabled: Bool
    var notificationDaysOffset: Int
    var selectedNotificationHour: Int

    fileprivate enum Constants {
        static let notificationEnabledUDKey = "notificationEnabled"
        static let notificationDaysOffsetUDKey = "notificationDaysOffset"
        static let selectedNotificationHourUDKey = "selectedNotificationHour"
    }

    init() {
        let userDefault = UserDefaults.standard
        notificationEnabled = userDefault.bool(forKey: Constants.notificationEnabledUDKey)
        notificationDaysOffset = userDefault.integer(forKey: Constants.notificationDaysOffsetUDKey)

        var selectedNotificationHour = userDefault.integer(forKey: Constants.selectedNotificationHourUDKey)
        if selectedNotificationHour == 0 {
            selectedNotificationHour = 8
        }
        self.selectedNotificationHour = selectedNotificationHour

        notificationHours = Array(5...23)
    }

    func disableAllNotifications() {
        notificationEnabled = false
        UserDefaults.standard.set(notificationEnabled, forKey: SettingsModelState.Constants.notificationEnabledUDKey)
    }
}

@MainActor protocol SettingsModel: AnyObject {
    var coordinator: SettingsCoordinator { get }
    
    func onAppear()
    func notifSettingsChanged()
}

@MainActor final class SettingsModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger
    @ObservationIgnored @Injected(\.notificationsBuilder) private var notificationsBuilder

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
        state.notificationsAuthorized = notificationSettings.authorizationStatus == .authorized
    }

    private func requestNotificationAuthorization() async {
        let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
        guard notificationSettings.authorizationStatus != .authorized else {
            await loadNotificationsAuthorizationStatus()
            return
        }

        do {
            if try await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) {
                logger.debug("Notification permission granted")
            } else {
                logger.debug("Notification permission denied")
                state.disableAllNotifications()
                notificationsBuilder.cancelAllNotifications()
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
            state.schedulingNotificationsInProgress = false
            return
        }

        state.schedulingNotificationsInProgress = true

        let input = NotificationBuilderInput(
            days: days,
            notificationEnabled: state.notificationEnabled,
            notificationDaysOffset: state.notificationDaysOffset,
            selectedNotificationHour: state.selectedNotificationHour
        )
        await notificationsBuilder.build(input: input)

        state.schedulingNotificationsInProgress = false
    }
}

extension SettingsModelImpl: SettingsModel {
    func onAppear() {
        tasks.addTask(id: "notifAuth", requestNotificationAuthorization)
    }

    func notifSettingsChanged() {
        let userDefault = UserDefaults.standard
        userDefault.set(state.notificationEnabled, forKey: SettingsModelState.Constants.notificationEnabledUDKey)
        userDefault.set(state.notificationDaysOffset, forKey: SettingsModelState.Constants.notificationDaysOffsetUDKey)
        userDefault.set(state.selectedNotificationHour, forKey: SettingsModelState.Constants.selectedNotificationHourUDKey)

        if state.notificationEnabled {
            tasks.addTask(id: "runSchedule-change-\(UUID().uuidString)", scheduleNotifications)
        } else {
            notificationsBuilder.cancelAllNotifications()
        }
    }
}
