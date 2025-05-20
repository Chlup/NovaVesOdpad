//
//  ArchExampleApp.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import SwiftUI

final class AppDelegate: NSObject, UIApplicationDelegate {
    let coordinator: GlobalCoordinator
    let rootModel: RootModel

    override init() {
        let coordinator = GlobalCoordinatorImpl()
        self.coordinator = coordinator
        rootModel = RootModelImpl(coordinator: coordinator)
        super.init()
    }
}

@main
struct NovaVesOdpadApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            RootView(model: appDelegate.rootModel)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                    appDelegate.rootModel.applicationWillEnterForegroundNotification()
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
                    appDelegate.rootModel.applicationDidEnterBackgroundNotification()
                }
        }
        
    }
}
