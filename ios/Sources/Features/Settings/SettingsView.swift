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

                    NotifSetupView(
                        model: model,
                        state: state,
                        title: "Tři dny předem",
                        isOn: $state.noticationEnabledThreeDaysBefore,
                        hour: $state.selectedNotificationHourThreeDaysBefore
                    )

                    NotifSetupView(
                        model: model,
                        state: state,
                        title: "Dva dny předem",
                        isOn: $state.noticationEnabledTwoDaysBefore,
                        hour: $state.selectedNotificationHourTwoDaysBefore
                    )

                    NotifSetupView(
                        model: model,
                        state: state,
                        title: "Jeden den předem",
                        isOn: $state.noticationEnabledOneDayBefore,
                        hour: $state.selectedNotificationHourOneDayBefore
                    )

                    NotifSetupView(
                        model: model,
                        state: state,
                        title: "V den svozu",
                        isOn: $state.noticationEnabledOnDay,
                        hour: $state.selectedNotificationHourOnDay
                    )
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 10))
            }
            .background(.screenBackground)
            .frame(maxWidth: .infinity, alignment: .leading)
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
    }
}

//private struct NotifSetupView2: View {
//    let model: HomeModel
//    let day: TrashDay
//
//    var body: some View {
//        VStack(alignment: .leading) {
//            Spacer()
//
//            HStack {
//                Text(model.titleForDay(day.date))
//                    .font(.callout)
//                    .bold()
//
//                Spacer()
//                ForEach(day.bins) { bin in
//                    BinIconView(bin: bin, size: 30)
//                }
//            }
//            .padding(.leading, 20)
//            .padding(.trailing, 20)
//
//            Spacer()
//        }
//        .background(.sectionBackground)
//        .frame(maxWidth: .infinity)
//        .frame(height: 55)
//        .cornerRadius(10)
//    }
//}

private struct NotifSetupView: View {
    let model: SettingsModel
    let state: SettingsModelState
    let title: String
    @Binding var isOn: Bool
    @Binding var hour: Int

    var body: some View {
        VStack(alignment: .leading) {
            Toggle(title, isOn: $isOn)
                .onChange(of: isOn) { model.notifSettingsChanged() }
                .font(.title2)
                .bold()
                .foregroundStyle(.regularText)
                .padding(.trailing, 10)

            if isOn {
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
                    .padding(0)
                    .foregroundStyle(.regularText)
                    .onChange(of: hour) { model.notifSettingsChanged() }
                }
            }
        }
        .padding(10)
        .padding(.leading, 5)
        .background(.sectionBackground)
        .frame(maxWidth: .infinity, minHeight: 55)
        .cornerRadius(10)
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
