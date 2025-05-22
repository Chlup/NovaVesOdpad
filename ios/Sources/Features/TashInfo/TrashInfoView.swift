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
        ScrollView {
            VStack(alignment: .leading) {
                ForEach(state.sections) { section in
                    HStack {
                        Text(section.title)
                            .font(.title2)
                            .bold()

                        Image(systemName: "trash")
                            .foregroundStyle(section.bin.backgroundColor)
                    }
                    .padding(.top, 10)

                    if let text = section.text {
                        Text(text)
                            .foregroundStyle(.black)
                            .padding(.top, 1)
                    }

                    ForEach(section.pdfFileURLs) { url in
                        PDFKitView(url: url)
                            .frame(height: 500)
                            .padding(.top, 5)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(EdgeInsets(top: 10, leading: 10, bottom: 10, trailing: 10))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(0)
        .setupNavigation(model)
        .setupToolbar(model)
    }
}

private extension View {
    func setupNavigation(_ model: TrashInfoModel) -> some View {
        return self
    }

    func setupToolbar(_ model: TrashInfoModel) -> some View {
        return self
            .navigationTitle("Co kam patří")
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

                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        model.coordinator.openWeb()
                    } label: {
                        Image(systemName: "square.and.arrow.up")
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

//#Preview {
//    HomeView(model: HomeModelImpl(coordinator: HomeCoordinator(coordinator: GlobalCoordinatorImpl())))
//}
