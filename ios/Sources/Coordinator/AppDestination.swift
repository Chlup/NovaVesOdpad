//
//  AppNavigation.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation

enum AppDestination: Hashable {
    case home
    case trashInfo([TrashInfoSection])
    case settings([TrashDay])
}

extension AppDestination: Identifiable {
    var id: String {
        switch self {
        case .home: "home"
        case .trashInfo: "trashInfo"
        case .settings: "settings"
        }
    }
}
