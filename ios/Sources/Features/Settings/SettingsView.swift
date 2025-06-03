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
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading) {
                    TitleView()

                    if !state.notificationsAuthorized {
                        NotificationsNotEnabledView(model: model)
                    }

                    if state.schedulingNotificationsInProgress {
                        SchedulingNotificationsView()
                    }

                    NotifSetupView(
                        model: model,
                        state: state,
                        title: "Zapnutá notifikace",
                        isOn: $state.notificationEnabled,
                        dayOffset: $state.notificationDaysOffset,
                        hour: $state.selectedNotificationHour
                    )
                }
                .padding(.horizontal, 24)
            }
            .background(.screenBackground)
            .setupNavigation(model)
            .setupToolbar(model)
            .onAppear { model.onAppear() }
            .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
                model.onAppear()
            }
        }
    }
}

private extension View {
    func setupNavigation(_ model: SettingsModel) -> some View {
        return self
    }

    func setupToolbar(_ model: SettingsModel) -> some View {
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
            Text("Nastavení notifikací")
                .font(.largeTitle)
                .bold()
                .foregroundStyle(.regularText)
        }
        .listRowBackground(Color.clear)
        .padding(.top, 20)
    }
}

private struct NotifSetupView: View {
    let model: SettingsModel
    let state: SettingsModelState
    let title: String
    @Binding var isOn: Bool
    @Binding var dayOffset: Int
    @Binding var hour: Int

    var body: some View {
        VStack(alignment: .leading) {
            Toggle(title, isOn: $isOn)
                .onChange(of: isOn) { model.notifSettingsChanged() }
                .font(.headline)
                .foregroundStyle(.regularText)
                .padding(.trailing, 10)
                .disabled(!state.notificationsAuthorized)

            if isOn {
                HStack {
                    Text("Den notifikace")
                        .font(.body)
                        .foregroundStyle(.regularText)

                    Spacer()

                    Picker("", selection: $dayOffset) {
                        Text("Tři dny před svozem")
                            .tag(3)

                        Text("Dva dny před svozem")
                            .tag(2)

                        Text("Jeden den před svozem")
                            .tag(1)

                        Text("V den svozu")
                            .tag(0)
                    }
                    .tint(.regularText)
                    .onChange(of: dayOffset) { model.notifSettingsChanged() }
                }

                HStack {
                    Text("Čas notifikace")
                        .font(.body)
                        .foregroundStyle(.regularText)

                    Spacer()

                    Picker("", selection: $hour) {
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
                    .tint(.regularText)
                    .onChange(of: hour) { model.notifSettingsChanged() }
                }
            }
        }
        .padding(10)
        .padding(.leading, 5)
        .background(.sectionBackground)
        .frame(maxWidth: .infinity, minHeight: 55)
        .cornerRadius(14)
    }
}

private struct NotificationsNotEnabledView: View {
    let model: SettingsModel

    var body: some View {
        Section {
            VStack(alignment: .center) {
                Text("""
                Notifikace nejsou povolené pro tuto aplikace, proto nebudou fungovat. Prosím povolte použití notifikací v nastavení vašeho \
                zařízení.
                """
                )
                .foregroundStyle(.notificationsNotEnabled)
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
            }
        }
        .frame(alignment: .center)
        .listRowBackground(Color.clear)
        .listRowInsets(EdgeInsets())
    }
}

private struct SchedulingNotificationsView: View {
    var body: some View {
        Section {
            HStack {
                Spacer()
                Text("""
                Plánuji notifikace...
                """
                )
                .foregroundStyle(.regularText)

                ProgressView()
                Spacer()
            }
            .frame(alignment: .center)
        }
        .frame(alignment: .center)
        .listRowBackground(Color.clear)
        .listRowInsets(EdgeInsets())
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
