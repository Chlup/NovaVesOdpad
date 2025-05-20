//
//  HomeView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI

struct HomeView: View {
    let model: HomeModel

    var body: some View {
        VStack(alignment: .leading) {
            List {
                ForEach(model.days) { day in
                    Section {
                        VStack(alignment: .leading) {
                            ForEach(day.bins) { bin in
                                BinView(bin: bin)
                                    .padding(.top, 1)
                            }
                        }
                    } header: {
                        Text(model.titleForDay(day))
                            .font(.title2)
                            .bold()
                            .foregroundStyle(.black)
                    }
                    .textCase(nil)
                }
            }
            .listSectionSpacing(.compact)
            .padding(0)
        }
        .padding(0)
        .onAppear { model.loadData() }
        .setupNavigation(model)
        .setupToolbar(model)
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            model.loadData()
        }
    }
}

private extension View {
    func setupNavigation(_ model: HomeModel) -> some View {
        return self
    }

    func setupToolbar(_ model: HomeModel) -> some View {
        return self
            .navigationTitle("Přehled vývozu odpadu")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        model.coordinator.tapOnSettings()
                    } label: {
                        Image(systemName: "gearshape")
                    }
                }

                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        model.coordinator.tapOnInfo()
                    } label: {
                        Image(systemName: "info.circle")
                    }
                }
            }
    }
}


private struct BinView: View {
    let bin: TrashDay.Bin

    var body: some View {
        HStack {
            Image(systemName: "trash")
                .foregroundStyle(bin.color)

            Text(bin.title)
                .font(.body)
        }
    }
}

//#Preview {
//    HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl())))
//}
