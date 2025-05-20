//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation

@MainActor struct SettingsCoordinator {
    unowned let coordinator: GlobalCoordinator

    func back() {
        coordinator.dismiss()
    }
}
