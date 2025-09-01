//
//  RootModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

//import Foundation
//import Factory
//
//@MainActor protocol RootModel {
//    var coordinator: GlobalCoordinator { get set }
//    func applicationWillEnterForegroundNotification()
//    func applicationDidEnterBackgroundNotification()
//}
//
//@MainActor @Observable final class RootModelImpl {
//    @ObservationIgnored @Injected(\.tasksManager) private var tasks
//    @ObservationIgnored @Injected(\.logger) private var logger
//
//    unowned var coordinator: GlobalCoordinator
//
//    init(coordinator: GlobalCoordinator) {
//        self.coordinator = coordinator
//    }
//}
//
//extension RootModelImpl: RootModel {
//    func applicationWillEnterForegroundNotification() {
//        logger.debug("applicationWillEnterForegroundNotification")
//    }
//
//    func applicationDidEnterBackgroundNotification() {
//        logger.debug("applicationDidEnterBackgroundNotification")
//    }
//}
