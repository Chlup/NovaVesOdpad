//
//  SendableCurrentValueSubject.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 21.05.2025.
//

import Foundation
import Combine

final class SendableCurrentValueSubject<T: Sendable, E: Error>: @unchecked Sendable {
    private let subject: CurrentValueSubject<T, E>
    private let lock = NSLock()

    var value: T {
        lock.lock()
        defer { lock.unlock() }
        return subject.value
    }

    init(_ value: T) {
        subject = CurrentValueSubject(value)
    }

    func send(_ value: T) {
        lock.lock()
        defer { lock.unlock() }
        subject.send(value)
    }

    func eraseToAnyPublisher() -> AnyPublisher<T, E> {
        lock.lock()
        defer { lock.unlock() }
        return subject.eraseToAnyPublisher()
    }
}
