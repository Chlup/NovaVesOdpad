//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class TrashInfoModelState {
    var sections: [TrashInfoSection] = []
}

@MainActor protocol TrashInfoModel {
    var coordinator: TrashInfoCoordinator { get }
}

@MainActor final class TrashInfoModelImpl {
    @ObservationIgnored @Injected(\.logger) private var logger

    let state: TrashInfoModelState
    let coordinator: TrashInfoCoordinator

    init(state: TrashInfoModelState, coordinator: TrashInfoCoordinator) {
        self.coordinator = coordinator
        self.state = state
        loadSections()
    }
}

extension TrashInfoModelImpl: TrashInfoModel {
    func loadSections() {
        var sections: [TrashInfoSection] = []
        let bio = TrashInfoSection(
            bin: .bio,
            text: """
            Jedná se o biologicky rozložitelný odpad rostlinného původu pocházející především z údržby zahrad, ale i rostlinné zbytky z kuchyní. \
            Ke sběru se pak nejčastěji využívají hnědé popelnice a kontejnery, nebo velkokapacitní kontejnery. Případně je možné je odkládat ve \
            sběrném dvoře. Bioodpady je také možné využít na zahradách v kompostérech nebo komunitních a obecních kompostárnách.
            """,
            pdfFileURLs: []
        )
        sections.append(bio)
        
        let paper = TrashInfoSection(
            bin: .paper,
            text: nil,
            pdfFileURLs: [Bundle.main.url(forResource: "papir_cz_2020", withExtension: "pdf")!]
        )
        sections.append(paper)
        
        let plastic = TrashInfoSection(
            bin: .plastic,
            text: nil,
            pdfFileURLs: [
                Bundle.main.url(forResource: "plasty_cz_2020", withExtension: "pdf")!,
                Bundle.main.url(forResource: "napojovy-karton_cz_2020", withExtension: "pdf")!,
                Bundle.main.url(forResource: "kovy_cz_2020", withExtension: "pdf")!
            ]
        )
        sections.append(plastic)

        state.sections = sections
    }
}
