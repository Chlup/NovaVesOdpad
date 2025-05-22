//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation

@MainActor struct HomeCoordinator {
    unowned let coordinator: GlobalCoordinator

    func tapOnNotificationsButton(allDays: [TrashDay]) {
        coordinator.presentSheet(.settings(allDays))
    }

    func tapOnCalendarButton() {
        coordinator.presentSheet(.trashInfo)
    }
}
