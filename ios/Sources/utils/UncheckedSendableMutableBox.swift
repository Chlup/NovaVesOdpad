//
//  UncheckedSendableMutableBox.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 21.05.2025.
//

import Foundation
import Combine

final class UncheckedSendableMutableBox<T>: @unchecked Sendable {
    var item: T
    init(_ item: T) { self.item = item }
}
