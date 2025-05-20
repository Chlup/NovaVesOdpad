//
//  RootView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import SwiftUI

struct RootView: View {
    @State var model: RootModel

    var body: some View {
        NavigationStack(path: $model.coordinator.navigationPath) {
            makeHomeView()
                .navigationDestination(for: AppDestination.self) { destination in
                    destinationView(for: destination)
                }
        }
//        .sheet(item: $model.coordinator.presentedSheet) { destination in
//            model.coordinator.destinationView(for: destination)
//        }
//        .fullScreenCover(item: $viewModel.coordinator.presentedFullScreenCover) { destination in
//            model.coordinator.destinationView(for: destination)
//        }
    }

    @ViewBuilder func makeHomeView() -> some View {
        let state = HomeModelState()
        HomeView(model: HomeModelImpl(state: state, coordinator: HomeCoordinator(coordinator: model.coordinator)), state: state)
    }

    @ViewBuilder func destinationView(for destination: AppDestination) -> some View {
        switch destination {
        case .home:
            makeHomeView()
        case .trashInfo:
            let state = TrashInfoModelState()
            TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: model.coordinator)), state: state)
        case .settings:
            let state = SettingsModelState()
            SettingsView(model: SettingsModelImpl(state: state, coordinator: SettingsCoordinator(coordinator: model.coordinator)), state: state)
        }
    }
}

