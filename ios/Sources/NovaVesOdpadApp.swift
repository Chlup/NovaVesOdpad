//
//  ArchExampleApp.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import SwiftUI
import Factory
import NotificationCenter
import ComposableArchitecture
@preconcurrency import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate, @preconcurrency UNUserNotificationCenterDelegate {
    @Injected(\.logger) private var logger

    let homeStore = StoreOf<Home>(
        initialState: Home.State()
    ) {
        Home()
//            ._printChanges()
    }

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Register the notification delegate
        UNUserNotificationCenter.current().delegate = self
        return true
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification
    ) async -> UNNotificationPresentationOptions {
        logger.debug("userNotificationCenter willPresent notification: \(notification.request.identifier), \(notification.request.content.title)")
        return [.banner, .sound, .badge, .list]
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse
    ) async {
        logger.debug("userNotificationCenter didReceive response")
    }
}

@main
struct NovaVesOdpadApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            HomeView(
                store: appDelegate.homeStore
            )
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                appDelegate.homeStore.send(.appDelegate(.willEnterForeground))
            }
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
                appDelegate.homeStore.send(.appDelegate(.didEnterBackground))
            }
        }
    }
}
