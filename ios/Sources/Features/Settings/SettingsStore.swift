//
//  SettingsStore.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import Foundation
import Dependencies
import Factory
@preconcurrency import UserNotifications
import UIKit

@Reducer
struct SettingsScreen {
    @Dependency(\.dismiss) var dismiss
    @ObservationIgnored @Injected(\.logger) private var logger
    @ObservationIgnored @Injected(\.settingsStorage) private var settingsStorage
    @ObservationIgnored @Injected(\.notificationsBuilder) private var notificationsBuilder

    enum CancelID: CaseIterable {
        case scheduleNotifications
    }

    @ObservableState
    struct State: Equatable {
        let allDays: [TrashDay]
        var notificationsAuthorized = false
        var schedulingNotificationsInProgress = false
        var notificationHours: [Int] = []
        var notificationEnabled = false
        var notificationDaysOffset = 0
        var selectedNotificationHour = 0
    }

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case loadNotificationsAuthorizationStatus
        case notificationsAuthorizationStatusLoaded(_ notificationsAuthorized: Bool)
        case requestNotificationsAuthorization
        case scheduleNotifications
        case notificationsSchedulingDone
        case disableAllNotifications
        case openSettings
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
        case .loadNotificationsAuthorizationStatus:
            return handleLoadNotificationsAuthorizationStatus(&state)
        case let .notificationsAuthorizationStatusLoaded(notificationsAuthorized):
            return handleNotificationsAuthorizationStatusLoaded(&state, notificationsAuthorized)
        case .requestNotificationsAuthorization:
            return handleRequestNotificationsAuthorization(&state)
        case .scheduleNotifications:
            return handleScheduleNotifications(&state)
        case .notificationsSchedulingDone:
            return handleNotificationsSchedulingDone(&state)
        case .disableAllNotifications:
            return handleDisableAllNotifications(&state)
        case .openSettings:
            return handleOpenSettings(&state)
        case .dismiss:
            return handleDismiss(&state)
        case .binding(\.notificationEnabled), .binding(\.notificationDaysOffset), .binding(\.selectedNotificationHour):
            return handleNotificationInfoChange(&state)
        case .binding:
            return .none
        }
    }

    private func handleOnAppear(_ state: inout State) -> Effect<Action> {
        state.notificationHours = Array(5...23)
        state.notificationEnabled = settingsStorage.notificationEnabled
        state.notificationDaysOffset = settingsStorage.notificationDaysOffset
        state.selectedNotificationHour = settingsStorage.selectedNotificationHour

        return .concatenate(
            .send(.loadNotificationsAuthorizationStatus),
            .send(.requestNotificationsAuthorization)
        )
    }

    private func handleLoadNotificationsAuthorizationStatus(_ state: inout State) -> Effect<Action> {
        return .run { send in
            let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
            let notificationsAuthorized = notificationSettings.authorizationStatus == .authorized
            await send(.notificationsAuthorizationStatusLoaded(notificationsAuthorized))
        }
    }

    private func handleNotificationsAuthorizationStatusLoaded(_ state: inout State, _ notificationsAuthorized: Bool) -> Effect<Action> {
        state.notificationsAuthorized = notificationsAuthorized
        return .none
    }

    private func handleRequestNotificationsAuthorization(_ state: inout State) -> Effect<Action> {
        return .run { send in
            let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
            guard notificationSettings.authorizationStatus != .authorized else {
                await send(.loadNotificationsAuthorizationStatus)
                return
            }

            do {
                if try await UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) {
                    logger.debug("Notification permission granted")
                } else {
                    logger.debug("Notification permission denied")
                    await send(.disableAllNotifications)
                }
            } catch {
                logger.debug("Failed to request notifications authorization: \(error)")
            }

            await send(.loadNotificationsAuthorizationStatus)
        }
    }

    private func handleScheduleNotifications(_ state: inout State) -> Effect<Action> {
        state.schedulingNotificationsInProgress = true
        let notificationEnabled = state.notificationEnabled
        let notificationDaysOffset = state.notificationDaysOffset
        let selectedNotificationHour = state.selectedNotificationHour
        let days = state.allDays

        return .concatenate(
            .cancel(id: CancelID.scheduleNotifications),
            .run { send in
                logger.debug("Starting to schedule notifications")

                // Check and log authorization status
                let notificationSettings = await UNUserNotificationCenter.current().notificationSettings()
                logger.debug("Notification auth status: \(notificationSettings.authorizationStatus.rawValue)")

                guard notificationSettings.authorizationStatus == .authorized else {
                    logger.debug("⚠️ User didn't authorize notifications. Can't schedule.")
                    await send(.notificationsSchedulingDone)
                    return
                }

                let input = NotificationBuilderInput(
                    days: days,
                    notificationEnabled: notificationEnabled,
                    notificationDaysOffset: notificationDaysOffset,
                    selectedNotificationHour: selectedNotificationHour
                )
                await notificationsBuilder.build(input: input)

                await send(.notificationsSchedulingDone)
            }
            .cancellable(id: CancelID.scheduleNotifications)
        )

    }

    private func handleNotificationsSchedulingDone(_ state: inout State) -> Effect<Action> {
        state.schedulingNotificationsInProgress = false
        return .none
    }

    private func handleDisableAllNotifications(_ state: inout State) -> Effect<Action> {
        state.notificationEnabled = false
        settingsStorage.updateNotificationEnabled(state.notificationEnabled)
        notificationsBuilder.cancelAllNotifications()
        return .none
    }

    private func handleNotificationInfoChange(_ state: inout State) -> Effect<Action> {
        settingsStorage.updateNotificationEnabled(state.notificationEnabled)
        settingsStorage.updateNotificationDaysOffset(state.notificationDaysOffset)
        settingsStorage.updateSelectedNotificationHour(state.selectedNotificationHour)
        if state.notificationEnabled {
            return .send(.scheduleNotifications)
        } else {
            return .send(.disableAllNotifications)
        }
    }

    private func handleOpenSettings(_ state: inout State) -> Effect<Action> {
        return .run { @MainActor _ in
            if let url = URL(string: UIApplication.openSettingsURLString) {
                UIApplication.shared.open(url)
            }
        }
    }

    private func handleDismiss(_ state: inout State) -> Effect<Action> {
        return .run { _ in await dismiss() }
    }
}

