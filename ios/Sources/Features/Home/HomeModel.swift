//
//  HomeModel.swift
//  ArchExample
//
//  Created by Michal Fousek on 04.05.2025.
//

import Foundation
import Factory

@MainActor @Observable final class HomeModelState {
    var firstDay: TrashDay?
    var homeDays: [TrashDay] = []
    var allDays: [TrashDay] = []
}

@MainActor protocol HomeModel {
    var coordinator: HomeCoordinator { get }

    func onAppear()
    func onWillEnterForegroundNotification()
    func titleForNextDay(_ date: Date) -> String
    func titleForDay(_ date: Date) -> String
    func daysToNextTrashDayText(numberOfDays: Int) -> String

    func plasticTrashInfoSection() -> [TrashInfoSection]
    func paperTrashInfoSection() -> [TrashInfoSection]
    func bioTrashInfoSection() -> [TrashInfoSection]
    func mixTrashInfoSection() -> [TrashInfoSection]
    func heavyLoadInfoSection() -> [TrashInfoSection]
}

@MainActor final class HomeModelImpl {
    @ObservationIgnored @Injected(\.tasksManager) private var tasks
    @ObservationIgnored @Injected(\.logger) private var logger
    @ObservationIgnored @Injected(\.daysLoader) private var daysLoader
    @ObservationIgnored @Injected(\.notificationsBuilder) private var notificationsBuilder

    let state: HomeModelState
    let coordinator: HomeCoordinator

    private let loadDaysTaskID = "loadDaysTaskID"
    private let scheduleNotificationsTaskID = "scheduleNotificationsTaskIDHome"

    private let dayTitleDateFormatter: DateFormatter
    private let nextTrashDayFormatter: DateFormatter

    init(state: HomeModelState, coordinator: HomeCoordinator) {
        self.coordinator = coordinator
        self.state = state
        dayTitleDateFormatter = DateFormatter()
        dayTitleDateFormatter.dateFormat = "d. MMMM"

        nextTrashDayFormatter = DateFormatter()
        nextTrashDayFormatter.dateFormat = "EEEE, d. MMMM"
    }

    func loadDays() async {
        let days = daysLoader.load()

        self.state.allDays = days
        self.state.firstDay = days.first
        self.state.homeDays = Array(days[1...3])
    }

    func scheduleNotifications() async {
        let days = daysLoader.load()
        let state = SettingsModelState()
        let input = NotificationBuilderInput(
            days: days,
            notificationEnabled: state.notificationEnabled,
            notificationDaysOffset: state.notificationDaysOffset,
            selectedNotificationHour: state.selectedNotificationHour
        )
        await notificationsBuilder.build(input: input)
    }
}

extension HomeModelImpl: HomeModel {
    func onAppear() {
        tasks.addTask(id: loadDaysTaskID, loadDays)
    }

    func onWillEnterForegroundNotification() {
        tasks.addTask(id: loadDaysTaskID, loadDays)
        tasks.addTask(id: scheduleNotificationsTaskID, scheduleNotifications)
    }

    func titleForNextDay(_ date: Date) -> String {
        return nextTrashDayFormatter.string(from: date)
    }

    func titleForDay(_ date: Date) -> String {
        let result = dayTitleDateFormatter.string(from: date)
        return result.prefix(1).uppercased() + result.dropFirst()
    }

    func daysToNextTrashDayText(numberOfDays: Int) -> String {
        switch numberOfDays {
        case 0:
            return "dnes"
        case 1:
            return "zítra"
        case 2...4:
            return "za \(numberOfDays) dny"
        default:
            return "za \(numberOfDays) dnů"
        }
    }

    func plasticTrashInfoSection() -> [TrashInfoSection] {
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
    }

    func paperTrashInfoSection() -> [TrashInfoSection] {
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
    }

    func bioTrashInfoSection() -> [TrashInfoSection] {
        return [
            TrashInfoSection(
                title: "Bio odpad",
                text: """
                Jedná se o biologicky rozložitelný odpad rostlinného původu pocházející především z údržby zahrad, ale i rostlinné zbytky z kuchyní. \
                Ke sběru se pak nejčastěji využívají hnědé popelnice a kontejnery, nebo velkokapacitní kontejnery. Případně je možné je odkládat ve \
                sběrném dvoře. Bioodpady je také možné využít na zahradách v kompostérech nebo komunitních a obecních kompostárnách.
                """,
                pdfFileURL: nil
            )
        ]
    }

    func mixTrashInfoSection() -> [TrashInfoSection] {
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
    }

    func heavyLoadInfoSection() -> [TrashInfoSection] {
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
