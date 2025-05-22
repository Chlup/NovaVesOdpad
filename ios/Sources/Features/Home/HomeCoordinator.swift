//
//  HomeCoordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import UIKit

@MainActor struct HomeCoordinator {
    unowned let coordinator: GlobalCoordinator

    func tapOnNotificationsButton(allDays: [TrashDay]) {
        coordinator.presentSheet(.settings(allDays))
    }

    func tapOnCalendarButton(allDays: [TrashDay]) {
        coordinator.presentSheet(.daysList(allDays))
    }

    func tapOnBinInfo(_ trashInfoSections: [TrashInfoSection]) {
        coordinator.presentSheet(.trashInfo(trashInfoSections))
    }

    func openSortingWeb() {
        guard let url = URL(string: "https://www.jaktridit.cz") else { return }
        UIApplication.shared.open(url)
    }
}
