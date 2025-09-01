//
//  DaysListView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import SwiftUI

struct DaysListView: View {
    @Bindable var store: StoreOf<DaysList>

    var body: some View {
        NavigationStack {
            List {
                TitleView()

                ForEach(store.months) { month in
                    Section {
                        ForEach(month.days) { day in
                            DayView(store: store, day: day)
                        }
                    } header: {
                        Text(store.titlesForMonth[month.id] ?? "")
                            .font(.title3)
                            .bold()
                            .foregroundStyle(.regularText)
                    }
                    .textCase(nil)
                    .listRowBackground(Color.sectionBackground)
                    .frame(minHeight: 40)
                    .cornerRadius(14)
                }
            }
            .padding(.vertical, 1)
            .padding(.horizontal, 4)
            .listRowInsets(EdgeInsets())
            .listSectionSpacing(.compact)
            .scrollContentBackground(.hidden)
            .background(.screenBackground)
            .setupNavigation($store)
            .setupToolbar(store)
            .onAppear { store.send(.onAppear) }
        }
    }
}

private extension View {
    func setupNavigation(_ store: Bindable<StoreOf<DaysList>>) -> some View {
        return self
    }

    func setupToolbar(_ store: StoreOf<DaysList>) -> some View {
        return self
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        store.send(.dismiss)
                    } label: {
                        Text("Zpět")
                            .foregroundStyle(.regularText)
                    }
                }
            }
    }
}

private struct TitleView: View {
    var body: some View {
        Section {
            Text("Kalendář všech vývozů")
                .font(.largeTitle)
                .bold()
                .foregroundStyle(.regularText)
        }
        .listRowBackground(Color.clear)
    }
}

private struct DayView: View {
    let store: StoreOf<DaysList>
    let day: TrashDay

    var body: some View {
        HStack {
            Text(store.titlesForDay[day.id] ?? "")
                .font(.callout)

            Spacer()

            ForEach(day.bins) { bin in
                BinIconView(bin: bin, size: 30)
            }
        }
    }
}
