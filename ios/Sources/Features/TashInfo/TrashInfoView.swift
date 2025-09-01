//
//  TrashInfoView.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 01.09.2025.
//

import ComposableArchitecture
import SwiftUI
import PDFKit

struct TrashInfoView: View {
    @Bindable var store: StoreOf<TrashInfo>

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading) {
                    ForEach(store.sections) { section in
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
            .setupNavigation($store)
            .setupToolbar(store)
            .onAppear { store.send(.onAppear) }
            .background(.screenBackground)
        }
    }
}

private extension View {
    func setupNavigation(_ store: Bindable<StoreOf<TrashInfo>>) -> some View {
        return self
    }

    func setupToolbar(_ store: StoreOf<TrashInfo>) -> some View {
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
