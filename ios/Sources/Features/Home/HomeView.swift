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
    let state: HomeModelState

    var body: some View {
        ScrollView {
            VStack(alignment: .leading) {
                TitleView()

                if let firstDay = state.firstDay {
                    NextTashDayView(model: model, day: firstDay)
                        .padding(.bottom, 15)
                }

                HStack {
                    NotificationsButtonView(model: model)
                        .padding(.trailing, 10)
                    CalendarButtonView(model: model)
                }
                .padding(.bottom, 15)

                Text("Budoucí vývozy")
                    .font(.headline)
                    .padding(.bottom, 10)

                ForEach(state.homeDays) { day in
                    DayView(model: model, day: day)
                }

                Text("Typy popelnic")
                    .font(.headline)
                    .padding(.top, 10)
                    .padding(.bottom, 10)


                TrashBinsInfoView()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 10))
        }
        .background(.screenBackground)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(0)
        .onAppear { model.loadData() }
        .setupNavigation(model)
        .setupToolbar(model, state)
        .onReceive(NotificationCenter.default.publisher(for: UIApplication.willEnterForegroundNotification)) { _ in
            model.loadData()
        }
    }
}

private extension View {
    func setupNavigation(_ model: HomeModel) -> some View {
        return self
    }

    func setupToolbar(_ model: HomeModel, _ state: HomeModelState) -> some View {
        return self
//            .navigationTitle("Popelnice")
//            .navigationBarTitleDisplayMode(.large)
//            .toolbar {
//                ToolbarItem(placement: .topBarLeading) {
//                    Button {
//                        model.coordinator.tapOnSettings(days: state.days)
//                    } label: {
//                        Image(systemName: "gearshape")
//                    }
//                }
//
//                ToolbarItem(placement: .topBarTrailing) {
//                    Button {
//                        model.coordinator.tapOnInfo()
//                    } label: {
//                        Image(systemName: "info.circle")
//                    }
//                }
//            }
    }
}

private struct TitleView: View {
    var body: some View {
        Section {
            Text("Popelnice")
                .font(.largeTitle)
                .bold()
                .foregroundStyle(.regularText)
        }
        .listRowBackground(Color.clear)
    }
}

private struct NextTashDayView: View {
    let model: HomeModel
    let day: TrashDay

    var body: some View {
        VStack(alignment: .leading) {
            VStack(alignment: .leading) {
                Text("Příští vývoz")
                    .font(.headline)
                    .bold()
                    .padding(.bottom, 8)

                HStack {
                    Text(model.titleForNextDay(day.date))
                        .font(.subheadline)
                        .bold()

                    Text(model.daysToNextTrashDayText(numberOfDays: day.daysDifferenceToToday))
                        .font(.subheadline)
                        .foregroundStyle(.grayText)
                }
                .padding(.bottom, 8)

                HStack {
                    ForEach(day.bins) { bin in
                        HStack {
                            BinIconView(bin: bin, size: 35)
                                .padding(.trailing, -3)

                            Text(bin.title)
                                .font(.callout)
                                .padding(.leading, 0)
                                .padding(.trailing, 10)
                        }
                    }
                    Spacer()
                }
            }
            .padding(20)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.sectionBackground)
        .cornerRadius(10)
        .shadow(
            // TODO: Which shadow color should be here for dark mode?
            color: Color.black.opacity(0.15),
            radius: 8,
            x: 0,
            y: 2
        )
    }
}

private struct NotificationsButtonView: View {
    let model: HomeModel

    var body: some View {
        Button {
            model.coordinator.tapOnNotificationsButton()
        } label: {
            HStack {
                Image(systemName: "square.grid.2x2")
                Text("Připomeň mi")
            }
            .padding(3)
            .foregroundStyle(.buttonLightBackground)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 45)
        .background(.buttonDarkBackground)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(.black, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .buttonStyle(.plain)
    }
}

private struct CalendarButtonView: View {
    let model: HomeModel

    var body: some View {
        Button {
            model.coordinator.tapOnCalendarButton()
        } label: {
            HStack {
                Image(systemName: "calendar")
                Text("Kalendář")
            }
            .padding(3)
            .foregroundStyle(.regularText)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 45)
        .background(.buttonLightBackground)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(.regularText, lineWidth: 1)
        )
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .buttonStyle(.plain)
    }
}

private struct DayView: View {
    let model: HomeModel
    let day: TrashDay

    var body: some View {
        VStack(alignment: .leading) {
            Spacer()

            HStack {
                Text(model.titleForDay(day.date))
                    .font(.callout)
                    .bold()

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
        .cornerRadius(10)
    }
}

private struct TrashBinsInfoView: View {
    var body: some View {
        HStack {
            BinInfoView(bin: .plastic)
            BinInfoView(bin: .paper)
        }

        HStack {
            BinInfoView(bin: .bio)
            BinInfoView(bin: .mix)
        }
    }
}

private struct BinInfoView: View {
    let bin: TrashDay.Bin

    var body: some View {
        Button {
        } label: {
            HStack {
                BinIconView(bin: bin, size: 35)

                Text(bin.title)
                    .lineLimit(nil)
                    .font(.callout)
                    .padding(.leading, 0)
                    .padding(.trailing, 10)
            }
            .padding(10)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(height: 70)
        .background(.sectionBackground)
        .cornerRadius(10)
        .buttonStyle(.plain)
    }
}

private struct BinIconView: View {
    let bin: TrashDay.Bin
    let size: CGFloat

    var body: some View {
        ZStack {
            Rectangle()
                .fill(bin.backgroundColor)
                .frame(width: size, height: size)
                .cornerRadius(size / 2)

            Image(systemName: bin.icon)
                .resizable()
                .foregroundStyle(bin.iconColor)
                .frame(width: size / 2, height: size / 2)
        }
    }
}

//#Preview {
//    HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl())))
//}
