//
//  TashInfoSection.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 20.05.2025.
//

import Foundation

extension URL: @retroactive Identifiable {
    public var id: String { self.absoluteString }
}

struct TrashInfoSection: Equatable, Identifiable, Hashable {
    var id: String { title }
    let title: String
    let text: String?
    let pdfFileURL: URL?
}
