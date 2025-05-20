//
//  HomeView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI

struct SettingsView: View {
    let model: SettingsModel
    @Bindable var state: SettingsModelState

    var body: some View {
        List {
            Section {
                Toggle("Tři dny předem", isOn: $state.noticationEnabledThreeDaysBefore)
                    .onChange(of: state.noticationEnabledThreeDaysBefore) { model.notifSettingsChanged() }

                Toggle("Dva dny předem", isOn: $state.noticationEnabledTwoDaysBefore)
                    .onChange(of: state.noticationEnabledTwoDaysBefore) { model.notifSettingsChanged() }

                Toggle("Jeden den předem", isOn: $state.noticationEnabledOneDaysBefore)
                    .onChange(of: state.noticationEnabledOneDaysBefore) { model.notifSettingsChanged() }

                Toggle("V den svozu", isOn: $state.noticationEnabledOnDay)
                    .onChange(of: state.noticationEnabledOnDay) { model.notifSettingsChanged() }

            } header: {
                Text("Nastavení notifikací")
                    .font(.title2)
                    .bold()
                    .foregroundStyle(.black)
            }
            .textCase(nil)

            Section {
                Picker("Čas notifikace", selection: $state.selectedNotificationHour) {
                    ForEach(state.notificationHours, id: \.self) { hour in
                        if hour < 10 {
                            Text("0\(hour):00")
                                .tag(hour)
                        } else {
                            Text("\(hour):00")
                                .tag(hour)
                        }
                    }
                }
                .onChange(of: state.selectedNotificationHour) { model.notifSettingsChanged() }
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
