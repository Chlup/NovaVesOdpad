//
//  SendablePassthroughSubject.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 21.05.2025.
//

import Foundation
import Combine

final class SendablePassthroughSubject<T: Sendable, E: Error>: @unchecked Sendable {
    private let subject = PassthroughSubject<T, E>()
    private let lock = NSLock()

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
