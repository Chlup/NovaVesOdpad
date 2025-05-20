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

struct TrashInfoSection: Equatable, Identifiable {
    var id: String { title }
    var title: String { bin.title }
    let bin: TrashDay.Bin
    let text: String?
    let pdfFileURLs: [URL]
}
