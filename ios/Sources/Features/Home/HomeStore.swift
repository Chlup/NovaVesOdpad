//
//  HomeStore.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import Foundation
import Dependencies
import Factory
import UIKit

@Reducer
struct Home {
    @Dependency(\.dismiss) var dismiss
    @Injected(\.logger) private var logger
    @Injected(\.daysLoader) private var daysLoader
    @Injected(\.notificationsBuilder) private var notificationsBuilder
    @Injected(\.settingsStorage) private var settingsStorage

    enum CancelID: CaseIterable {
        case scheduleNotifications
    }

    @ObservableState
    struct State: Equatable {
        @Presents var alert: AlertState<Action>?
        @Presents var destination: Destination.State?
        @Presents var daysList: DaysList.State?
        @Presents var trashInfo: TrashInfo.State?
        @Presents var settingsScreen: SettingsScreen.State?
        var isLoading = true
        var firstDay: TrashDay?
        var homeDays: [TrashDay] = []
        var allDays: [TrashDay] = []
        var titleForNextDay: String = ""
        var daysToNextTrashDayText: String = ""
        var titlesForDay: [TrashDay.ID: String] = [:]
        var shouldScheduleNotificationAfterDaysLoad = true
    }

    enum Action: BindableAction, Equatable {
        enum AppDelegateAction: Equatable {
            case didEnterBackground
            case willEnterForeground
        }

        case appDelegate(AppDelegateAction)
        case alert(PresentationAction<Action>)
        case binding(BindingAction<State>)
        case destination(PresentationAction<Destination.Action>)
        case daysList(PresentationAction<DaysList.Action>)
        case trashInfo(PresentationAction<TrashInfo.Action>)
        case settingsScreen(PresentationAction<SettingsScreen.Action>)
        case onAppear
        case loadDays
        case daysLoaded(
            _ firstDay: TrashDay?,
            _ homeDays: [TrashDay],
            _ allDays: [TrashDay],
            _ titleForNextDay: String,
            _ daysToNextTrashDayText: String,
            _ titlesForDay: [TrashDay.ID: String]
        )
        case scheduleNotifications
        case openDaysList
        case openSortingWeb
        case openTrashInfo(TrashDay.Bin)
        case openSettings
    }

    @Reducer(state: .equatable, action: .equatable)
    enum Destination {
    }

    var body: some ReducerOf<Self> {
        BindingReducer()

        Reduce { state, action in return doReduce(into: &state, action: action) }
            .ifLet(\.$destination, action: \.destination)
            .ifLet(\.$daysList, action: \.daysList) { DaysList() }
            .ifLet(\.$trashInfo, action: \.trashInfo) { TrashInfo() }
            .ifLet(\.$settingsScreen, action: \.settingsScreen) { SettingsScreen() }
    }

    private func doReduce(into state: inout State, action: Action) -> Effect<Action> {
        switch action {
        case .appDelegate(.didEnterBackground):
            return handleAppDelegateDidEnterBackground(&state)
        case .appDelegate(.willEnterForeground):
            return handleAppDelegateWillEnterForeground(&state)
        case .alert(.dismiss):
            return handleAlertDismiss(&state)
        case .onAppear:
            return handleOnAppear(&state)
        case .loadDays:
            return handleLoadDays(&state)
        case let .daysLoaded(firstDay, homeDays, allDays, titleForNextDay, daysToNextTrashDayText, titlesForDay):
            return handleDaysLoaded(&state, firstDay, homeDays, allDays, titleForNextDay, daysToNextTrashDayText, titlesForDay)
        case .scheduleNotifications:
            return handleScheduleNotifications(&state)
        case .openDaysList:
            return handleOpenDaysList(&state)
        case .openSortingWeb:
            return handleOpenSortingWeb(&state)
        case let .openTrashInfo(bin):
            return handleOpenTrashInfo(&state, bin)
        case .openSettings:
            return handleOpenSettings(&state)
        case .binding, .destination, .alert, .daysList, .trashInfo, .settingsScreen:
            return .none
        }
    }

    private func handleAppDelegateDidEnterBackground(_ state: inout State) -> Effect<Action> {
        logger.debug("applicationWillEnterForegroundNotification")
        return .none
    }

    private func handleAppDelegateWillEnterForeground(_ state: inout State) -> Effect<Action> {
        logger.debug("applicationDidEnterBackgroundNotification")
        return .concatenate(
            .send(.settingsScreen(.presented(.onAppear))),
            .cancel(id: CancelID.scheduleNotifications),
            .send(.loadDays),
            .send(.scheduleNotifications)
        )
    }

    private func handleAlertDismiss(_ state: inout State) -> Effect<Action> {
        state.alert = nil
        return .none
    }

    private func handleOnAppear(_ state: inout State) -> Effect<Action> {
        return .send(.loadDays)
    }

    private func handleLoadDays(_ state: inout State) -> Effect<Action> {
        if state.homeDays.isEmpty {
            state.isLoading = true
        }
        return .run { send in
            let days = daysLoader.load()
            let homeDays = Array(days[1...3])

            var titleForNextDay: String = ""
            var daysToNextTrashDayText: String = ""
            if let firstDay = days.first {
                let nextTrashDayFormatter = DateFormatter()
                nextTrashDayFormatter.dateFormat = "EEEE, d. MMMM"
                titleForNextDay = nextTrashDayFormatter.string(from: firstDay.date)

                switch firstDay.daysDifferenceToToday {
                case 0:
                    daysToNextTrashDayText = "dnes"
                case 1:
                    daysToNextTrashDayText = "zítra"
                case 2...4:
                    daysToNextTrashDayText = "za \(firstDay.daysDifferenceToToday) dny"
                default:
                    daysToNextTrashDayText = "za \(firstDay.daysDifferenceToToday) dnů"
                }
            }

            let dayTitleDateFormatter = DateFormatter()
            dayTitleDateFormatter.dateFormat = "d. MMMM"
            var titlesForDay: [TrashDay.ID: String] = [:]
            for day in days {
                let result = dayTitleDateFormatter.string(from: day.date)
                titlesForDay[day.id] = result.prefix(1).uppercased() + result.dropFirst()
            }

            await send(.daysLoaded(days.first, homeDays, days, titleForNextDay, daysToNextTrashDayText, titlesForDay))
        }
    }

    private func handleDaysLoaded(
        _ state: inout State,
        _ firstDay: TrashDay?,
        _ homeDays: [TrashDay],
        _ allDays: [TrashDay],
        _ titleForNextDay: String,
        _ daysToNextTrashDayText: String,
        _ titlesForDay: [TrashDay.ID: String]
    ) -> Effect<Action> {
        state.isLoading = false

        state.firstDay = firstDay
        state.homeDays = homeDays
        state.allDays = allDays
        state.titleForNextDay = titleForNextDay
        state.daysToNextTrashDayText = daysToNextTrashDayText
        state.titlesForDay = titlesForDay

        if state.shouldScheduleNotificationAfterDaysLoad {
            state.shouldScheduleNotificationAfterDaysLoad = false
            return .send(.scheduleNotifications)
        } else {
            return .none
        }
    }

    private func handleScheduleNotifications(_ state: inout State) -> Effect<Action> {
        let days = state.allDays
        return .concatenate(
            .cancel(id: CancelID.scheduleNotifications),
            .run { send in
                let days = days
                let input = NotificationBuilderInput(
                    days: days,
                    notificationEnabled: settingsStorage.notificationEnabled,
                    notificationDaysOffset: settingsStorage.notificationDaysOffset,
                    selectedNotificationHour: settingsStorage.selectedNotificationHour
                )
                await notificationsBuilder.build(input: input)
            }
            .cancellable(id: CancelID.scheduleNotifications)
        )
    }

    private func handleOpenDaysList(_ state: inout State) -> Effect<Action> {
        state.daysList = DaysList.State(allDays: state.allDays)
        return .none
    }

    private func handleOpenSortingWeb(_ state: inout State) -> Effect<Action> {
        return .run { @MainActor send in
            guard let url = URL(string: "https://www.jaktridit.cz") else { return }
            await UIApplication.shared.open(url)
        }
    }

    private func handleOpenTrashInfo(_ state: inout State, _ bin: TrashDay.Bin) -> Effect<Action> {
        state.trashInfo = TrashInfo.State(bin: bin)
        return .none
    }

    private func handleOpenSettings(_ state: inout State) -> Effect<Action> {
        state.settingsScreen = SettingsScreen.State(allDays: state.allDays)
        return .none
    }
}
