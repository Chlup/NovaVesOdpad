//
//  HomeView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import SwiftUI

struct HomeView: View {
    @Bindable var store: StoreOf<Home>

    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                TitleView()

                if let firstDay = store.firstDay {
                    NextTrashDayView(store: store, day: firstDay)
                        .padding(.bottom, 18)
                }

                HStack {
                    NotificationsButtonView(store: store)
                        .padding(.trailing, 10)
                    CalendarButtonView(store: store)
                }
                .padding(.bottom, 18)

                Text("Budoucí vývozy")
                    .font(.title3)
                    .bold()
                    .padding(.bottom, 4)

                ForEach(store.homeDays) { day in
                    DayView(store: store, day: day)
                        .padding(.bottom, 4)
                }

                HStack {
                    Text("Typy popelnic")
                        .font(.title3)
                        .bold()

                    Spacer()

                    Button {
                        store.send(.openSortingWeb)
                    } label: {
                        Image(systemName: "info.circle")
                            .resizable()
                            .foregroundStyle(.regularText)
                            .frame(width: 20, height: 20)
                    }
                }
                .padding(.top, 18)
                .padding(.bottom, 4)

                TrashBinsInfoView(store: store)
                    .padding(.bottom, 24)
            }
            .padding(.horizontal, 24)
        }
        .padding(.vertical, 1)
        .background(.screenBackground)
        .onAppear { store.send(.onAppear) }
        .setupNavigation($store)
        .setupToolbar(store)
        .alert($store.scope(state: \.alert, action: \.alert))
    }
}

private extension View {
    func setupNavigation(_ store: Bindable<StoreOf<Home>>) -> some View {
        return self
            .sheet(
                item: store.scope(state: \.daysList, action: \.daysList),
                content: { store in DaysListView(store: store) }
            )
            .sheet(
                item: store.scope(state: \.trashInfo, action: \.trashInfo),
                content: { store in TrashInfoView(store: store) }
            )
            .sheet(
                item: store.scope(state: \.settingsScreen, action: \.settingsScreen),
                content: { store in SettingsView(store: store) }
            )
    }

    func setupToolbar(_ store: StoreOf<Home>) -> some View {
        return self
            .toolbar(.hidden, for: .navigationBar)
    }
}

private struct TitleView: View {
    var body: some View {
        Text("Popelnice")
            .font(.largeTitle)
            .bold()
            .foregroundStyle(.regularText)
    }
}

private struct NextTrashDayView: View {
    let store: StoreOf<Home>
    let day: TrashDay

    var body: some View {
        VStack(alignment: .leading) {
            VStack(alignment: .leading) {
                Text("Příští vývoz")
                    .font(.title2)
                    .bold()
                    .padding(.bottom, 1)

                HStack {
                    Text(store.titleForNextDay)
                        .font(.callout)

                    Text(store.daysToNextTrashDayText)
                        .font(.subheadline)
                        .foregroundStyle(.grayText)
                }
                .padding(.bottom, 12)

                VStack {
                    HStack {
                        ForEach(day.bins) { bin in
                            if bin != .heavyLoad {
                                HStack {
                                    BinIconView(bin: bin, size: 35)
                                        .padding(.trailing, 0)

                                    Text(bin.title)
                                        .font(.callout)
                                        .padding(.trailing, 10)
                                        .minimumScaleFactor(0.6)
                                        .fixedSize()

                                    Spacer()
                                }
                                .frame(maxWidth: .infinity)
                            }
                        }

                        Spacer()
                    }

                    ForEach(day.bins) { bin in
                        if bin == .heavyLoad {
                            HStack {
                                BinIconView(bin: bin, size: 35)
                                    .padding(.trailing, 0)

                                Text(bin.title)
                                    .font(.callout)
                                    .padding(.trailing, 10)
                                    .minimumScaleFactor(0.6)
                                    .fixedSize()

                                Spacer()
                            }
                            .frame(maxWidth: .infinity)
                        }
                    }
                }
            }
            .padding(20)
        }
        .background(.sectionBackground)
        .cornerRadius(20)
        .shadow(
            color: Color.black.opacity(0.12),
            radius: 5,
            x: 0,
            y: 4
        )
        .shadow(
            color: Color.black.opacity(0.01),
            radius: 5,
            x: 0,
            y: -4
        )
    }
}

private struct NotificationsButtonView: View {
    let store: StoreOf<Home>

    var body: some View {
        Button {
            store.send(.openSettings)
        } label: {
            HStack {
                Image(systemName: "square.grid.2x2")
                Text("Připomeň mi")
            }
            .padding(3)
            .foregroundStyle(.buttonLightBackground)
            .frame(maxWidth: .infinity)
            .frame(height: 45)
            .background(.buttonDarkBackground)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(.black, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}

private struct CalendarButtonView: View {
    let store: StoreOf<Home>

    var body: some View {
        Button {
            store.send(.openDaysList)
        } label: {
            HStack {
                Image(systemName: "calendar")

                Text("Kalendář")
            }
            .padding(3)
            .foregroundStyle(.regularText)
            .frame(maxWidth: .infinity)
            .frame(height: 45)
            .background(.screenBackground)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(.regularText, lineWidth: 2)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }
}

private struct DayView: View {
    let store: StoreOf<Home>
    let day: TrashDay

    var body: some View {
        VStack(alignment: .leading) {
            Spacer()

            HStack {
                Text(store.titlesForDay[day.id] ?? "")
                    .font(.callout)

                Spacer()

                ForEach(day.bins) { bin in
                    BinIconView(bin: bin, size: 30)
                }
            }
            .padding(.leading, 20)
            .padding(.trailing, 20)

            Spacer()
        }
        .background(.sectionBackground)
        .frame(maxWidth: .infinity)
        .frame(height: 55)
        .cornerRadius(12)
    }
}

private struct TrashBinsInfoView: View {
    let store: StoreOf<Home>

    var body: some View {
        VStack(spacing: 14) {
            HStack(spacing: 14) {
                BinInfoView(store: store, bin: .plastic)
                BinInfoView(store: store, bin: .paper)
            }

            HStack(spacing: 14) {
                BinInfoView(store: store, bin: .bio)
                BinInfoView(store: store, bin: .mix)
            }

            BinInfoView(store: store, bin: .heavyLoad)
        }
    }
}

private struct BinInfoView: View {
    let store: StoreOf<Home>
    let bin: TrashDay.Bin

    var body: some View {
        Button {
            store.send(.openTrashInfo(bin))
        } label: {
            HStack {
                BinIconView(bin: bin, size: 35)

                Text(bin.title)
                    .lineLimit(nil)
                    .font(.callout)
            }
            .padding(14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .frame(height: 70)
            .background(.sectionBackground)
            .cornerRadius(12)
        }
        .buttonStyle(.plain)
    }
}

//#Preview {
//    let state = HomeModelState()
//    let model = HomeModelImpl(state: state, coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
//    HomeView(model: model, state: state)
//}
