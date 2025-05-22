//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import UIKit

@MainActor struct TrashInfoCoordinator {
    unowned let coordinator: GlobalCoordinator

    func back() {
        coordinator.dismiss()
    }

    func openWeb() {
        guard let url = URL(string: "https://www.jaktridit.cz") else { return }
        UIApplication.shared.open(url)
    }
}

