//
//  SettingsView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import SwiftUI

struct SettingsView: View {
    @Bindable var store: StoreOf<SettingsScreen>

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading) {
                    TitleView()

                    if !store.notificationsAuthorized {
                        NotificationsNotEnabledView(store: store)
                    }

                    if store.schedulingNotificationsInProgress {
                        SchedulingNotificationsView()
                    }

                    NotifSetupView(store: store, title: "Zapnutá notifikace")
                }
                .padding(.horizontal, 24)
            }
            .background(.screenBackground)
            .setupNavigation($store)
            .setupToolbar(store)
            .onAppear { store.send(.onAppear) }
        }
    }
}

private extension View {
    func setupNavigation(_ store: Bindable<StoreOf<SettingsScreen>>) -> some View {
        return self
    }

    func setupToolbar(_ store: StoreOf<SettingsScreen>) -> some View {
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
    @Bindable var store: StoreOf<SettingsScreen>
    let title: String

    var body: some View {
        VStack(alignment: .leading) {
            Toggle(title, isOn: $store.notificationEnabled)
                .font(.headline)
                .foregroundStyle(.regularText)
                .padding(.trailing, 10)
                .disabled(!store.notificationsAuthorized)

            if store.notificationEnabled {
                HStack {
                    Text("Den notifikace")
                        .font(.body)
                        .foregroundStyle(.regularText)

                    Spacer()

                    Picker("", selection: $store.notificationDaysOffset) {
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
                }

                HStack {
                    Text("Čas notifikace")
                        .font(.body)
                        .foregroundStyle(.regularText)

                    Spacer()

                    Picker("", selection: $store.selectedNotificationHour) {
                        ForEach(store.notificationHours, id: \.self) { hour in
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
    let store: StoreOf<SettingsScreen>

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
                    store.send(.openSettings)
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

//#Preview {
//    let state = SettingsModelState()
//    let model = SettingsModelImpl(
//        state: state,
//        coordinator: SettingsCoordinator(coordinator: GlobalCoordinatorImpl()),
//        days: []
//    )
//    SettingsView(model: model, state: state)
//}
