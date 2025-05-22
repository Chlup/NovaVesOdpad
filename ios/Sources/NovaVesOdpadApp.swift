//
//  ArchExampleApp.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import SwiftUI
@preconcurrency import UserNotifications
import Factory
import NotificationCenter
import UserNotificationsUI

final class AppDelegate: NSObject, UIApplicationDelegate, @preconcurrency UNUserNotificationCenterDelegate {
    @ObservationIgnored @Injected(\.logger) private var logger

    let coordinator: GlobalCoordinator
    let rootModel: RootModel
    let homeState: HomeModelState

    override init() {
        let coordinator = GlobalCoordinatorImpl()
        self.coordinator = coordinator
        rootModel = RootModelImpl(coordinator: coordinator)
        homeState = HomeModelState()
        super.init()
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
            RootView(model: appDelegate.rootModel, homeState: appDelegate.homeState)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                    appDelegate.rootModel.applicationWillEnterForegroundNotification()
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
                    appDelegate.rootModel.applicationDidEnterBackgroundNotification()
                }
        }
        
    }
}
