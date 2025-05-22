//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation

@MainActor struct HomeCoordinator {
    unowned let coordinator: GlobalCoordinator

    func tapOnInfo() {
        coordinator.navigate(to: .trashInfo)
    }

    func tapOnSettings(days: [TrashDay]) {
        coordinator.navigate(to: .settings(days))
    }

    func tapOnNotificationsButton() {
        
    }

    func tapOnCalendarButton() {
        
    }
}
