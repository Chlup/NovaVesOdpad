//
//  RootView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import SwiftUI

struct RootView: View {
    @State var model: RootModel
    let homeState: HomeModelState

    var body: some View {
        NavigationStack(path: $model.coordinator.navigationPath) {
            makeHomeView()
                .navigationDestination(for: AppDestination.self) { destination in
                    destinationView(for: destination)
                }
        }
        .sheet(item: $model.coordinator.presentedSheet) { destination in
            destinationView(for: destination)
        }
//        .fullScreenCover(item: $viewModel.coordinator.presentedFullScreenCover) { destination in
//            model.coordinator.destinationView(for: destination)
//        }
    }

    @ViewBuilder func makeHomeView() -> some View {
        HomeView(model: HomeModelImpl(state: homeState, coordinator: HomeCoordinator(coordinator: model.coordinator)), state: homeState)
    }

    @ViewBuilder func destinationView(for destination: AppDestination) -> some View {
        switch destination {
        case .home:
            makeHomeView()

        case let .trashInfo(sections):
            let state = TrashInfoModelState(sections: sections)
            TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: model.coordinator)), state: state)

        case let .settings(days):
            let state = SettingsModelState()
            let model =  SettingsModelImpl(
                state: state,
                coordinator: SettingsCoordinator(coordinator: model.coordinator),
                days: days
            )
            SettingsView(model: model, state: state)

        case let .daysList(days):
            let state = DaysListState(allDays: days)
            let model = DaysListModelImpl(state: state, coordinator: DaysListCoordinator(coordinator: model.coordinator))
            DaysListView(model: model, state: state)
        }
    }
}

