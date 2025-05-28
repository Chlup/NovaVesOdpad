//
//  DaysListView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 22.05.2025.
//

import Foundation
import SwiftUI

struct DaysListView: View {
    let model: DaysListModel
    let state: DaysListState

    var body: some View {
        NavigationStack {
            List {
                TitleView()

                ForEach(state.months) { month in
                    Section {
                        ForEach(month.days) { day in
                            DayView(model: model, day: day)
                        }
                    } header: {
                        Text(model.titleForMonth(month.date))
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
            .setupNavigation(model)
            .setupToolbar(model, state)
            .onAppear { model.onAppear() }
        }
    }
}

private extension View {
    func setupNavigation(_ model: DaysListModel) -> some View {
        return self
    }

    func setupToolbar(_ model: DaysListModel, _ state: DaysListState) -> some View {
        return self
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        model.coordinator.dismiss()
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
    let model: DaysListModel
    let day: TrashDay

    var body: some View {
        HStack {
            Text(model.titleForDay(day.date))
                .font(.callout)

            Spacer()
            
            ForEach(day.bins) { bin in
                BinIconView(bin: bin, size: 30)
            }
        }
    }
}

//#Preview {
//    let homeState = HomeModelState()
//    let homeModel = HomeModelImpl(state: homeState, coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
//
//    let state = DaysListState(allDays: homeState.allDays)
//    let model = DaysListModelImpl(state: state, coordinator: DaysListCoordinator(coordinator: GlobalCoordinatorImpl()))
//    DaysListView(model: model, state: state)
//        .onAppear {
//            homeModel.loadDays()
//            state.allDays = homeState.allDays
//            model.groupDays()
//        }
//}

