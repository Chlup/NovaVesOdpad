//
//  MainButton.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI

struct MainButton: View {
    let text: String
    let tapHandler: () -> Void

    var body: some View {
        Button {
            tapHandler()
        } label: {
            Text(text)
                .frame(minWidth: 80, minHeight: 50)
        }
        .buttonStyle(.borderedProminent)
    }
}
