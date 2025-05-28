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
        let startOfSelf = calendar.startOfDay(for: self)
        let startOfOther = calendar.startOfDay(for: otherDate)
        
        let timeInterval = startOfOther.timeIntervalSince(startOfSelf)
        let days = Int(timeInterval / (24 * 60 * 60))
        
        return days
    }
}
