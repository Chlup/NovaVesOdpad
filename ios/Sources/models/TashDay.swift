//
//  TrashDay.swift
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
                return "Směs"
            case .plastic:
                return "Plast, kov, kartony"
            case .paper:
                return "Papír"
            case .bio:
                return "Bio"
            }
        }

        var backgroundColor: Color {
            switch self {
            case .mix:
                return .binBlack
            case .plastic:
                return .binYellow
            case .paper:
                return .binBlue
            case .bio:
                return .binBrown
            }
        }

        var iconColor: Color {
            switch self {
            case .plastic:
                return .black
            case .bio, .paper, .mix:
                return .white
            }
        }

        var icon: String {
            switch self {
            case .mix:
                return "bin"
            case .plastic:
                return "recycle"
            case .paper:
                return "paper"
            case .bio:
                return "bio"
            }
        }
    }

    var id: String { "\(date)" }
    let date: Date
    let daysDifferenceToToday: Int
    let bins: [Bin]

    var isToday: Bool { daysDifferenceToToday == 0 }
}
