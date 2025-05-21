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
            if !state.notificationsAuthorized && state.noticiationsEnabledForAnyDay {
                Section {
                    VStack(alignment: .center) {
                        Text("""
                        Notifikace nejsou povolené pro tuto aplikace, proto nebudou fungovat. Prosím povolte použití notifikací v nastavení vašeho \
                        zařízení.
                        """
                        )
                        .foregroundStyle(.red)
                        .padding(10)

                        Button {
                            model.coordinator.goToSettings()
                        } label: {
                            Text("Jít do nastavení")
                                .foregroundStyle(.white)
                                .frame(maxWidth: .infinity)
                                .frame(height: 40)
                        }
                        .buttonStyle(.borderedProminent)
                        .padding(0)
                    }
                    .padding(0)
                }
                .frame(alignment: .center)
                .listRowBackground(Color.clear)
                .listRowInsets(EdgeInsets())
            }

            Section {
                NotifSetupView(
                    model: model,
                    state: state,
                    title: "Tři dny předem",
                    isOn: $state.noticationEnabledThreeDaysBefore,
                    hour: $state.selectedNotificationHourThreeDaysBefore
                )
            } header: {
                Text("Nastavení notifikací")
                    .font(.title2)
                    .bold()
                    .foregroundStyle(.black)
            }
            .textCase(nil)

            Section {
                NotifSetupView(
                    model: model,
                    state: state,
                    title: "Dva dny předem",
                    isOn: $state.noticationEnabledTwoDaysBefore,
                    hour: $state.selectedNotificationHourTwoDaysBefore
                )
            }

            Section {
                NotifSetupView(
                    model: model,
                    state: state,
                    title: "Jeden den předem",
                    isOn: $state.noticationEnabledOneDayBefore,
                    hour: $state.selectedNotificationHourOneDayBefore
                )
            }

            Section {
                NotifSetupView(
                    model: model,
                    state: state,
                    title: "V den svozu",
                    isOn: $state.noticationEnabledOnDay,
                    hour: $state.selectedNotificationHourOnDay
                )
            }
        }
        .listSectionSpacing(.compact)
        .padding(0)
        .setupNavigation(model)
        .setupToolbar(model)
        .onAppear { model.onAppear() }
        .onDisappear { model.onDisappear() }
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            model.onAppear()
        }
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

private struct NotifSetupView: View {
    let model: SettingsModel
    let state: SettingsModelState
    let title: String
    @Binding var isOn: Bool
    @Binding var hour: Int

    var body: some View {
        Toggle(title, isOn: $isOn)
            .onChange(of: isOn) { model.notifSettingsChanged() }

        if isOn {
            Picker("Čas notifikace", selection: $hour) {
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
            .onChange(of: hour) { model.notifSettingsChanged() }
        }
    }
}

#Preview {
    let state = SettingsModelState()
    let model = SettingsModelImpl(
        state: state,
        coordinator: SettingsCoordinator(coordinator: GlobalCoordinatorImpl()),
        days: []
    )
    SettingsView(model: model, state: state)
}
