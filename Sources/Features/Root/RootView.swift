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
            HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: model.coordinator)))
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

    @ViewBuilder func destinationView(for destination: AppDestination) -> some View {
        switch destination {
        case .home:
            HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: model.coordinator)))
        case .trashInfo:
            TrashInfoView(model: TrashInfoModelImpl(coordinator: TrashInfoCoordinator(coordinator: model.coordinator)))
        case .settings:
            SettingsView(model: SettingsModelImpl(coordinator: SettingsCoordinator(coordinator: model.coordinator)))
        }
    }
}

