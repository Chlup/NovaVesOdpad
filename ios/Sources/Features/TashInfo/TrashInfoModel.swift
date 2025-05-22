//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class TrashInfoModelState {
    let sections: [TrashInfoSection]

    init(sections: [TrashInfoSection]) {
        self.sections = sections
    }
}

@MainActor protocol TrashInfoModel {
    var coordinator: TrashInfoCoordinator { get }
}

@MainActor final class TrashInfoModelImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: TrashInfoModelState
    let coordinator: TrashInfoCoordinator

    init(state: TrashInfoModelState, coordinator: TrashInfoCoordinator) {
        self.coordinator = coordinator
        self.state = state
    }
}

extension TrashInfoModelImpl: TrashInfoModel { }
