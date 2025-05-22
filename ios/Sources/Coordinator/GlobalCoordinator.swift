//
//  Coordinator.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI

@MainActor protocol GlobalCoordinator: AnyObject {
    var navigationPath: NavigationPath { get set }
    var presentedSheet: AppDestination? { get set }

    func navigate(to destination: AppDestination)
    func presentSheet(_ destination: AppDestination)
    func dismiss()
    func navigateToRoot()
}

@MainActor @Observable final class GlobalCoordinatorImpl: GlobalCoordinator {
    var navigationPath = NavigationPath()
    var presentedSheet: AppDestination?
//    var presentedFullScreenCover: AppDestination?

    // Navigate to a new screen by pushing onto the stack
    func navigate(to destination: AppDestination) {
        navigationPath.append(destination)
    }

    // Go back one screen
    func dismiss() {
        if presentedSheet != nil {
            self.presentedSheet = nil
        } else if !navigationPath.isEmpty {
            navigationPath.removeLast()
        }
    }

    // Go back to root
    func navigateToRoot() {
        navigationPath = NavigationPath()
    }

    // Present a sheet
    func presentSheet(_ destination: AppDestination) {
        presentedSheet = destination
    }
//
//    // Dismiss sheet
//    func dismissSheet() {
//        presentedSheet = nil
//    }
//
//    // Present full screen cover
//    func presentFullScreenCover(_ destination: AppDestination) {
//        presentedFullScreenCover = destination
//    }
//
//    // Dismiss full screen cover
//    func dismissFullScreenCover() {
//        presentedFullScreenCover = nil
//    }
}
