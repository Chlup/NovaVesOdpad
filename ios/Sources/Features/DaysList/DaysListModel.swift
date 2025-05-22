//
//  DaysListModel.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class DaysListState {
    let allDays: [TrashDay]

    init(allDays: [TrashDay]) {
        self.allDays = allDays
    }
}

@MainActor protocol DaysListModel {
    var coordinator: DaysListCoordinator { get }
}

@MainActor final class DaysListModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: DaysListState
    let coordinator: DaysListCoordinator

    init(state: DaysListState, coordinator: DaysListCoordinator) {
        self.coordinator = coordinator
        self.state = state
    }
}

extension DaysListModelImpl: DaysListModel { }
