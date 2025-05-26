//
//  HomeView.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import SwiftUI
import PDFKit

struct TrashInfoView: View {
    let model: TrashInfoModel
    let state: TrashInfoModelState

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading) {
                    ForEach(state.sections) { section in
                        Text(section.title)
                            .font(.title)
                            .foregroundStyle(.regularText)
                            .bold()

                        if let text = section.text {
                            Text(text)
                                .foregroundStyle(.regularText)
                                .padding(.top, 1)
                        }

                        if let url = section.pdfFileURL {
                            PDFKitView(url: url)
                                .frame(height: 500)
                                .padding(.top, 5)
                        }
                    }
                }
            }
            .padding(.top, 20)
            .padding(.horizontal, 24)
            .setupNavigation(model)
            .setupToolbar(model)
            .background(.screenBackground)
        }
    }
}

private extension View {
    func setupNavigation(_ model: TrashInfoModel) -> some View {
        return self
    }

    func setupToolbar(_ model: TrashInfoModel) -> some View {
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

private struct PDFKitView: UIViewRepresentable {
    let url: URL

    func makeUIView(context: Context) -> PDFView {
        let pdfView = PDFView()
        pdfView.translatesAutoresizingMaskIntoConstraints = false

        pdfView.document = PDFDocument(url: url)
        pdfView.autoScales = true
        pdfView.displayMode = .singlePageContinuous
        pdfView.displayDirection = .vertical
        pdfView.usePageViewController(true)
        pdfView.pageBreakMargins = UIEdgeInsets(top: 0, left: 0, bottom: 0, right: 0)
        pdfView.backgroundColor = .systemBackground
        
        pdfView.enableDataDetectors = true
        pdfView.isUserInteractionEnabled = true
        
        return pdfView
    }

    func updateUIView(_ uiView: PDFView, context: Context) {
    }
}

#Preview("Bio") {
    let homeModel = HomeModelImpl(state: HomeModelState(), coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
    let state = TrashInfoModelState(sections: homeModel.bioTrashInfoSection())
    TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: GlobalCoordinatorImpl())), state: state)
}

#Preview("Paper") {
    let homeModel = HomeModelImpl(state: HomeModelState(), coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
    let state = TrashInfoModelState(sections: homeModel.paperTrashInfoSection())
    TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: GlobalCoordinatorImpl())), state: state)
}

#Preview("Plastic") {
    let homeModel = HomeModelImpl(state: HomeModelState(), coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
    let state = TrashInfoModelState(sections: homeModel.plasticTrashInfoSection())
    TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: GlobalCoordinatorImpl())), state: state)
}

#Preview("Mix") {
    let homeModel = HomeModelImpl(state: HomeModelState(), coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl()))
    let state = TrashInfoModelState(sections: homeModel.mixTrashInfoSection())
    TrashInfoView(model: TrashInfoModelImpl(state: state, coordinator: TrashInfoCoordinator(coordinator: GlobalCoordinatorImpl())), state: state)
}
