//
//  Logger.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

extension Container { var logger: Factory<Logger> { self { LoggerImpl() }.singleton } }

protocol Logger: Sendable {
    func debug(_ message: String)
}

struct LoggerImpl { }

extension LoggerImpl: Logger {
    func debug(_ message: String) {
        #if DEBUG
        print(message)
        #endif
    }
}
