//
//  HomeView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI

struct SettingsView: View {
    @Bindable var model: SettingsModelImpl

    var body: some View {
        List {
            Section {
                Toggle("Tři dny předem", isOn: $model.noticationEnabledThreeDaysBefore)
                    .onChange(of: model.noticationEnabledThreeDaysBefore) { model.notifSettingsChanged() }

                Toggle("Dva dny předem", isOn: $model.noticationEnabledTwoDaysBefore)
                    .onChange(of: model.noticationEnabledTwoDaysBefore) { model.notifSettingsChanged() }

                Toggle("Jeden den předem", isOn: $model.noticationEnabledOneDaysBefore)
                    .onChange(of: model.noticationEnabledOneDaysBefore) { model.notifSettingsChanged() }

                Toggle("V den svozu", isOn: $model.noticationEnabledOnDay)
                    .onChange(of: model.noticationEnabledOnDay) { model.notifSettingsChanged() }

            } header: {
                Text("Nastavení notifikací")
                    .font(.title2)
                    .bold()
                    .foregroundStyle(.black)
            }
            .textCase(nil)

            Section {
                Picker("Čas notifikace", selection: $model.selectedNotificationHour) {
                    ForEach(model.notificationHours, id: \.self) { hour in
                        if hour < 10 {
                            Text("0\(hour):00")
                                .tag(hour)
                        } else {
                            Text("\(hour):00")
                                .tag(hour)
                        }
                    }
                }
                .onChange(of: model.selectedNotificationHour) { model.notifSettingsChanged() }
            }

        }
        .listSectionSpacing(.compact)
        .padding(0)
        .setupNavigation(model)
        .setupToolbar(model)
    }
}

private extension View {
    func setupNavigation(_ model: SettingsModel) -> some View {
        return self
    }

    func setupToolbar(_ model: SettingsModel) -> some View {
        return self
            .navigationTitle("Nastavení")
            .navigationBarTitleDisplayMode(.inline)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button {
                        model.coordinator.back()
                    } label: {
                        Image(systemName: "chevron.backward")
                    }
                }
            }
    }
}

//#Preview {
//    HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl())))
//}
