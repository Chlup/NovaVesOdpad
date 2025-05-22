//
//  Date+Extensions.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation

extension Date {
    /// Calculate days difference between two dates
    /// - Parameter otherDate: The date to compare with
    /// - Returns: Number of days difference (positive if otherDate is in future, negative if in past)
    func daysDifference(to otherDate: Date) -> Int {
        let calendar = Calendar.current
        let components = calendar.dateComponents([.day], from: self, to: otherDate)
        // Why the fuck this always returns what I expect - 1 ?
        return (components.day ?? 0) + 1
    }
}
