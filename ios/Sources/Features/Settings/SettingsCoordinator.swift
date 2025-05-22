//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import UIKit

@MainActor struct SettingsCoordinator {
    unowned let coordinator: GlobalCoordinator

    func dismiss() {
        coordinator.dismiss()
    }

    func goToSettings() {
        if let url = URL(string: UIApplication.openSettingsURLString) {
            UIApplication.shared.open(url)
        }
    }
}
