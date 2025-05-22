//
//  DaysListCoordinator.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation

@MainActor struct DaysListCoordinator {
    unowned let coordinator: GlobalCoordinator

    func dismiss() {
        coordinator.dismiss()
    }
}

