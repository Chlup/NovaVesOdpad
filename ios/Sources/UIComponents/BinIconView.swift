//
//  BinIconView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation
import SwiftUI

struct BinIconView: View {
    let bin: TrashDay.Bin
    let size: CGFloat

    var body: some View {
        ZStack {
            Rectangle()
                .fill(bin.backgroundColor)
                .frame(width: size, height: size)
                .cornerRadius(size / 2)

            Image(bin.icon)
                .resizable()
                .renderingMode(.template)
                .foregroundStyle(bin.iconColor)
                .frame(width: size / 1.75, height: size / 1.75)
        }
    }
}
