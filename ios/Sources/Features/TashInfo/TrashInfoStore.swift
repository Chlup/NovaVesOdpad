//
//  TrashInfoStore.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import Foundation
import Dependencies
import Factory

@Reducer
struct TrashInfo {
    @Dependency(\.dismiss) var dismiss
    @Injected(\.logger) private var logger

    @ObservableState
    struct State: Equatable {
        let bin: TrashDay.Bin
        var sections: [TrashInfoSection] = []
    }

    enum Action: BindableAction, Equatable {
        case binding(BindingAction<State>)
        case onAppear
        case dismiss
    }

    var body: some ReducerOf<Self> {
        BindingReducer()
        Reduce { state, action in return doReduce(into: &state, action: action) }
    }

    private func doReduce(into state: inout State, action: Action) -> Effect<Action> {
        switch action {
        case .onAppear:
            return handleOnAppear(&state)
        case .dismiss:
            return handleDismiss(&state)
        case .binding:
            return .none
        }
    }

    private func handleOnAppear(_ state: inout State) -> Effect<Action> {
        state.sections = TrashInfoSection.sectionsForBin(state.bin)
        return .none
    }

    private func handleDismiss(_ state: inout State) -> Effect<Action> {
        return .run { _ in await dismiss() }
    }
}

