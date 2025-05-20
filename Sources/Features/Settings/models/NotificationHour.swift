//
//  NotificationHour.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 20.05.2025.
//

import Foundation

struct NotificationHour: Equatable, Identifiable, Hashable, Codable {
    var id: String { title }
    let title: String
    let hour: Int
}
