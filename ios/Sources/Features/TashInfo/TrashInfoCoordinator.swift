//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation

@MainActor struct TrashInfoCoordinator {
    unowned let coordinator: GlobalCoordinator

    func dismiss() {
        coordinator.dismiss()
    }
}

