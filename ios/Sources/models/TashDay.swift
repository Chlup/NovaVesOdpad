//
//  TashDay.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 20.05.2025.
//

import Foundation
import SwiftUI

struct TrashDay: Codable, Equatable, Identifiable, Hashable {
    enum Bin: String, Codable, Identifiable {
        case mix
        case plastic
        case paper
        case bio

        var id: String { "\(self)" }

        var title: String {
            switch self {
            case .mix:
                return "Směsný komunální odpad"
            case .plastic:
                return "Plasty"
            case .paper:
                return "Papír"
            case .bio:
                return "Bio odpad"
            }
        }

        var color: Color {
            switch self {
            case .mix:
                return .black
            case .plastic:
                return .yellow
            case .paper:
                return .blue
            case .bio:
                return .brown
            }
        }
    }

    var id: String { "\(date)" }
    let date: Date
    let bins: [Bin]
}
