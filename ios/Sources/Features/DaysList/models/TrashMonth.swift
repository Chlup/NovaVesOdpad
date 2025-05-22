//
//  TrashMonth.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation

struct TrashMonth: Equatable, Identifiable {
    var id: String { "\(date)" }
    let date: Date
    let days: [TrashDay]
}
