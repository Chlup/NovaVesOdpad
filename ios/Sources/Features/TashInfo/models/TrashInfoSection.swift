//
//  TrashInfoSection.swift
//  NovaVesOdpad
//
//  Created by Michal Fousek on 20.05.2025.
//

import Foundation

extension URL: @retroactive Identifiable {
    public var id: String { self.absoluteString }
}

struct TrashInfoSection: Equatable, Identifiable, Hashable {
    var id: String { title }
    let title: String
    let text: String?
    let pdfFileURL: URL?

    static func sectionsForBin(_ bin: TrashDay.Bin) -> [TrashInfoSection] {
        switch bin {
        case .mix:
            return [
                TrashInfoSection(
                    title: "Směsný odpad",
                    text: """
                    Nebo také směsný komunální odpad (SKO) tvoří většinu komunálního odpadu, a to i přesto, že je definován jako zbytek po vytřídění všech \
                    výše uvedených odpadů. Síť nádob na jeho sběr je vůbec nejrozšířenější. Jde převážně o plastové popelnice a kontejnery různých objemů \
                    převážně černé barvy. Někde je sbírán do kovových pozinkovaných popelnic a kontejnerů. Název sám evokuje, že se v tomto odpadu může \
                    objevit ledacos. Při poctivém třídění odpadu zjistíme, že sem patří například porcelán a keramika. Dále také odpady nepodléhající \
                    zpětnému odběru složené z více materiálů, které od sebe nejdou snadno oddělit a odpady silně znečištěné jinými látkami jiného než \
                    nebezpečného charakteru.
                    
                    V souvislosti s tímto odpadem se můžeme setkat také s označením zbytkový odpad, které lépe vyjadřuje, že do těchto nádob patří pouze \
                    odpady, které nelze jinam vytřídit.
                    """,
                    pdfFileURL: nil
                )
            ]

        case .plastic:
            return [
                TrashInfoSection(
                    title: "Plasty",
                    text: """
                    Patří do žlutého kontejneru. V průměrné české popelnici zabírají nejvíc místa ze všech odpadů. Důležité je proto nejenom jejich \
                    třídění, ale i sešlápnutí či zmačkání před vyhozením. V některých městech a obcích se spolu s plastovým odpadem třídí i nápojové \
                    kartony nebo kovy. Záleží na místních podmínkách a technickém vybavení dotřiďovacích linek, kam se odpad sváží. Proto je důležité \
                    sledovat nálepky na jednotlivých kontejnerech. Mimo níže uvedených značek do těchto kontejnerů můžete vyhazovat i obalové odpady \
                    označené číslem 7.
                    
                    Značky, které se využívají k označování plastových obalů a výrobků.
                    
                    Do kontejnerů na plasty patří fólie, sáčky, plastové tašky, sešlápnuté PET lahve, obaly od pracích, čistících a kosmetických \
                    přípravků, kelímky od jogurtů, mléčných výrobků, balící fólie od spotřebního zboží, obaly od CD disků a další výrobky z plastů.
                    
                    Obalový pěnový polystyren sem vhazujeme v menších kusech, vhodnější je odnášet jej do sběrných dvorů.
                    
                    Naopak sem nepatří mastné obaly se zbytky potravin nebo čistících přípravků, obaly od žíravin, barev a jiných nebezpečných látek, \
                    podlahové krytiny či novodurové trubky.
                    """,
                    pdfFileURL: Bundle.main.url(forResource: "plasty_cz_2020", withExtension: "pdf")!
                ),
                TrashInfoSection(
                    title: "Nápojové kartony",
                    text: """
                    Známé jako krabice na mléko, džusy nebo víno. Vhazují se do kontejnerů různých barev a tvarů, ale vždy označených oranžovou \
                    nálepkou, případně do oranžových pytlů. Záleží na tom, jak má obec systém sběru nápojových kartonů nastavený. Na nápojových \
                    kartonech jsou tyto značky:
                    
                    Nápojové kartony je potřeba před vytříděním řádně sešlápnout. Nemusejí se vymývat, stačí pořádně vyprázdnit, při delším skladováním \
                    v domácnosti doporučujeme vypláchnutím malým množstvím vody.. Nevadí na nich ani plastová víčka.
                    
                    Do nádob na nápojové kartony nepatří „měkké“ sáčky, například od kávy a různých potravin v prášku. Neodhazujeme sem ani nápojové \
                    kartony obsahující zbytky nápojů a potravin.
                    """,
                    pdfFileURL: Bundle.main.url(forResource: "napojovy-karton_cz_2020", withExtension: "pdf")!
                ),
                TrashInfoSection(
                    title: "Kovy",
                    text: """
                    Čím dál častěji se v ulicích objevují i šedé nádoby na třídění kovových obalů. Určeny jsou především na sběr plechovek a drobnějšího \
                    kovového odpadu. Mnohde se sbírají společně s plasty či nápojovými kartony (viz návodná samolepka na kontejneru). V některých obcích \
                    lze sbírat kovy do pytlů. Hlavním místem pro sběr většiny kovových odpadů stále zůstávají sběrné dvory a výkupny druhotných surovin, \
                    kde za ně dostaneme i peníze. Někde je sběr kovů řešen i formou mobilních svozů známých jako „železná sobota“ nebo „železná neděle“.
                    
                    Do kontejnerů na kovy patří drobnější kovový odpad, který lze skrz otvor bez problémů prostrčit. Typicky prázdné plechovky od nápojů \
                    a konzerv, kovové tuby, alobal, kovové zátky, hřebíky, šroubky, kancelářské sponky a další drobné kovové odpady.
                    
                    Do sběrných dvorů lze kromě těchto menších odpadů odvážet i další kovové odpady: trubky, roury, plechy, hrnce, vany, kola a další \
                    objemnější předměty.
                    
                    Do kontejnerů na ulici nepatří plechovky od barev a jiných nebezpečných látek, tlakové nádoby se zbytky nebezpečných látek, ani \
                    domácí spotřebiče a jiná vysloužilá zařízení složená z více materiálů. Tyto druhy odpadů se třídí ve sběrných dvorech samostatně. \
                    Nepatří do nich ani těžké nebo toxické kovy, jakou jsou olovo či rtuť. Samostatnou kapitolu pak tvoří autovraky, jež převezmou a \
                    doklad o ekologické likvidaci vystaví na vrakovištích.
                    """,
                    pdfFileURL: Bundle.main.url(forResource: "kovy_cz_2020", withExtension: "pdf")!
                )
            ]

        case .paper:
            return [
                TrashInfoSection(
                    title: "Papír",
                    text: """
                    Patří do modrého kontejneru. Ze všech tříděných odpadů právě papíru vyprodukuje průměrná česká domácnost za rok hmotnostně nejvíc. \
                    Modré kontejnery na papír bývají nejsnazším způsobem, jak se této komodity správně zbavit. Alternativu pak poskytují sběrné suroviny, \
                    které ovšem nejsou vždy dostupné. Na druhou stranu nabízejí za papír roztříděný podle druhů finanční odměnu. Velké kusy papíru je \
                    vhodné odnášet do sběrných dvorů, pokud jsou k dispozici. Stále časté jsou u nás i školní sběry a soutěže, při kterých se sbírají \
                    zejména noviny a časopisy.
                    
                    Do modrého kontejneru můžeme vhodit časopisy, noviny, sešity, krabice, papírové obaly, cokoliv z lepenky, nebo knihy. Obálky s fóliovými \
                    okénky sem můžeme také vhazovat. Nevadí ani papír s kancelářskými sponkami nebo obaly od vajec a ruličky od toaletního papíru. \
                    Zpracovatelé si s nimi umí poradit. Bublinkové obálky vhazujeme pouze bez plastového vnitřku!
                    
                    Papírové obaly a výrobky mohou být značeny těmito značkami.
                    
                    Do modrého kontejneru naopak nepatří celé svazky knih (vhazovat pouze bez pevné vazby, ve větším počtu patří do sběrného dvora), \
                    uhlový (kopírovací papír), mastný nebo jakkoliv znečištěný papír. Tyto materiály nelze už nadále recyklovat. To samé platí o termopapíru \
                    (některých účtenkách). Pozor, použité dětské pleny opravdu nepatří do kontejneru na papír, ale do nádoby na směsný odpad!
                    """,
                    pdfFileURL: Bundle.main.url(forResource: "papir_cz_2020", withExtension: "pdf")!
                )
            ]

        case .bio:
            return [
                TrashInfoSection(
                    title: "Bio odpad",
                    text: """
                    Bio popelnice se vyváží v období od 1. Dubna do 30. Listopadu.
                    
                    Jedná se o biologicky rozložitelný odpad rostlinného původu pocházející především z údržby zahrad, ale i rostlinné zbytky z kuchyní. \
                    Ke sběru se pak nejčastěji využívají hnědé popelnice a kontejnery, nebo velkokapacitní kontejnery. Případně je možné je odkládat ve \
                    sběrném dvoře. Bioodpady je také možné využít na zahradách v kompostérech nebo komunitních a obecních kompostárnách.
                    """,
                    pdfFileURL: nil
                )
            ]

        case .heavyLoad:
            return [
                TrashInfoSection(
                    title: "Kontejner na velkoobjemový odpad",
                    text: """
                    Kontejner je k dispozici od 9:00 do 11:00 hodin!!!
                    
                    - Je umístěn v prostoru bývalé skládky (směrem na Velkou Lečici)
                    - U kontejneru bude služba, která bude kontrolovat odpad
                    
                    Do kontejneru nepatří: běžný domovní odpad, sklo, větve, listí, plastové lahve, nebezpečný odpad (baterie, zářivky, autobaterie), stavební suť, dřevo a nábytek o větších rozměrech např. čalouněné soupravy (tento nábytek lze odvézt do sběrného dvoru Mníšek pod Brdy).
                    """,
                    pdfFileURL: Bundle.main.url(forResource: "heavy_load", withExtension: "pdf")!
                )
            ]
        }
    }

}
