package com.mugeaters.popelnice.nvpp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.model.TrashInfoSection
import com.mugeaters.popelnice.nvpp.util.Logger
import com.mugeaters.popelnice.nvpp.util.TasksManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * ViewModel state for the Home screen
 */
data class HomeState(
    val allDays: List<TrashDay> = emptyList(),
    val firstDay: TrashDay? = null,
    val homeDays: List<TrashDay> = emptyList()
)

/**
 * ViewModel for the Home screen
 */
class HomeViewModel(
    private val tasksManager: TasksManager,
    private val logger: Logger
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()
    
    private val dayTitleFormatter = DateTimeFormatter.ofPattern("d. MMMM", Locale.getDefault())
    private val nextDayTitleFormatter = DateTimeFormatter.ofPattern("EEEE, d. MMMM", Locale.getDefault())
    private val loadDaysTaskId = "load_days_task"
    
    /**
     * Loads trash days data using programmatic generation (like iOS app)
     */
    fun loadData() {
        logger.debug("🏠 HomeViewModel.loadData() called")
        tasksManager.addTask(loadDaysTaskId) {
            try {
                logger.debug("🏠 Generating trash days...")
                val days = generateTrashDays()
                // Update state with all days, first day, and home days (like iOS)
                val firstDay = days.firstOrNull()
                val homeDays = if (days.size > 3) {
                    days.subList(1, 4)  // indices 1, 2, 3 (next 3 days after first)
                } else if (days.size > 1) {
                    days.subList(1, days.size)  // remaining days after first
                } else {
                    emptyList()
                }
                
                _state.update { 
                    it.copy(
                        allDays = days,
                        firstDay = firstDay,
                        homeDays = homeDays
                    ) 
                }
            } catch (e: Exception) {
                logger.error("Failed to generate trash days", e)
            }
        }
    }
    
    /**
     * Generates trash collection days programmatically
     * Algorithm matches iOS implementation - every Wednesday, alternating bin types based on week number
     */
    private fun generateTrashDays(): List<TrashDay> {
        val now = LocalDateTime.now()
        val nextWednesday = getNextWednesday(now)
        
        val days = mutableListOf<TrashDay>()
        
        // Generate 53 weeks of trash days (1 year + 1 week buffer)
        for (i in 0..52) {
            val dayDate = nextWednesday.plusWeeks(i.toLong())
            val weekOfYear = dayDate.get(WeekFields.ISO.weekOfWeekBasedYear())
            
            val bins = if (weekOfYear % 2 == 0) {
                // Even weeks: paper, bio, mix
                listOf(TrashDay.Bin.paper, TrashDay.Bin.bio, TrashDay.Bin.mix)
            } else {
                // Odd weeks: plastic, mix
                listOf(TrashDay.Bin.plastic, TrashDay.Bin.mix)
            }
            
            val day = TrashDay(date = dayDate, bins = bins)
            days.add(day)
        }
        
        return days
    }
    
    /**
     * Finds the next Wednesday from the given date
     * If today is Wednesday, returns today
     */
    private fun getNextWednesday(from: LocalDateTime): LocalDateTime {
        val dayOfWeek = from.dayOfWeek.value // Monday=1, Tuesday=2, ..., Sunday=7
        val wednesdayValue = 3 // Wednesday
        
        return if (dayOfWeek == wednesdayValue) {
            // If today is Wednesday, return today
            from.toLocalDate().atStartOfDay()
        } else {
            // Calculate days until next Wednesday
            val daysUntilWednesday = (wednesdayValue - dayOfWeek + 7) % 7
            from.toLocalDate().plusDays(daysUntilWednesday.toLong()).atStartOfDay()
        }
    }
    
    /**
     * Formats a date as a title string
     */
    fun titleForDay(date: LocalDateTime): String {
        val formatted = dayTitleFormatter.format(date)
        return formatted.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Formats a date as a title string for the next day display
     */
    fun titleForNextDay(date: LocalDateTime): String {
        val formatted = nextDayTitleFormatter.format(date)
        return formatted.replaceFirstChar { it.uppercase() }
    }
    
    /**
     * Returns a text describing how many days until the next trash day
     * Matches iOS implementation exactly
     */
    fun daysToNextTrashDayText(daysDifference: Int): String {
        return when (daysDifference) {
            0 -> "dnes"
            1 -> "zítra"
            2, 3, 4 -> "za $daysDifference dny"
            else -> "za $daysDifference dnů"
        }
    }
    
    /**
     * Returns plastic trash info sections (matches iOS implementation)
     */
    fun plasticTrashInfoSection(): List<TrashInfoSection> {
        return listOf(
            TrashInfoSection(
                title = "Plasty",
                text = """Patří do žlutého kontejneru. V průměrné české popelnici zabírají nejvíc místa ze všech odpadů. Důležité je proto nejenom jejich třídění, ale i sešlápnutí či zmačkání před vyhozením. V některých městech a obcích se spolu s plastovým odpadem třídí i nápojové kartony nebo kovy. Záleží na místních podmínkách a technickém vybavení dotřiďovacích linek, kam se odpad sváží. Proto je důležité sledovat nálepky na jednotlivých kontejnerech. Mimo níže uvedených značek do těchto kontejnerů můžete vyhazovat i obalové odpady označené číslem 7.

Značky, které se využívají k označování plastových obalů a výrobků.

Do kontejnerů na plasty patří fólie, sáčky, plastové tašky, sešlápnuté PET lahve, obaly od pracích, čistících a kosmetických přípravků, kelímky od jogurtů, mléčných výrobků, balící fólie od spotřebního zboží, obaly od CD disků a další výrobky z plastů.

Obalový pěnový polystyren sem vhazujeme v menších kusech, vhodnější je odnášet jej do sběrných dvorů.

Naopak sem nepatří mastné obaly se zbytky potravin nebo čistících přípravků, obaly od žíravin, barev a jiných nebezpečných látek, podlahové krytiny či novodurové trubky.""",
                pdfFileName = "plasty_cz_2020.pdf"
            ),
            TrashInfoSection(
                title = "Nápojové kartony",
                text = """Známé jako krabice na mléko, džusy nebo víno. Vhazují se do kontejnerů různých barev a tvarů, ale vždy označených oranžovou nálepkou, případně do oranžových pytlů. Záleží na tom, jak má obec systém sběru nápojových kartonů nastavený. Na nápojových kartonech jsou tyto značky:

Nápojové kartony je potřeba před vytříděním řádně sešlápnout. Nemusejí se vymývat, stačí pořádně vyprázdnit, při delším skladováním v domácnosti doporučujeme vypláchnutím malým množstvím vody.. Nevadí na nich ani plastová víčka.

Do nádob na nápojové kartony nepatří „měkké" sáčky, například od kávy a různých potravin v prášku. Neodhazujeme sem ani nápojové kartony obsahující zbytky nápojů a potravin.""",
                pdfFileName = "napojovy-karton_cz_2020.pdf"
            ),
            TrashInfoSection(
                title = "Kovy",
                text = """Čím dál častěji se v ulicích objevují i šedé nádoby na třídění kovových obalů. Určeny jsou především na sběr plechovek a drobnějšího kovového odpadu. Mnohde se sbírají společně s plasty či nápojovými kartony (viz návodná samolepka na kontejneru). V některých obcích lze sbírat kovy do pytlů. Hlavním místem pro sběr většiny kovových odpadů stále zůstávají sběrné dvory a výkupny druhotných surovin, kde za ně dostaneme i peníze. Někde je sběr kovů řešen i formou mobilních svozů známých jako „železná sobota" nebo „železná neděle".

Do kontejnerů na kovy patří drobnější kovový odpad, který lze skrz otvor bez problémů prostrčit. Typicky prázdné plechovky od nápojů a konzerv, kovové tuby, alobal, kovové zátky, hřebíky, šroubky, kancelářské sponky a další drobné kovové odpady.

Do sběrných dvorů lze kromě těchto menších odpadů odvážet i další kovové odpady: trubky, roury, plechy, hrnce, vany, kola a další objemnější předměty.

Do kontejnerů na ulici nepatří plechovky od barev a jiných nebezpečných látek, tlakové nádoby se zbytky nebezpečných látek, ani domácí spotřebiče a jiná vysloužilá zařízení složená z více materiálů. Tyto druhy odpadů se třídí ve sběrných dvorech samostatně. Nepatří do nich ani těžké nebo toxické kovy, jakou jsou olovo či rtuť. Samostatnou kapitolu pak tvoří autovraky, jež převezmou a doklad o ekologické likvidaci vystaví na vrakovištích.""",
                pdfFileName = "kovy_cz_2020.pdf"
            )
        )
    }

    /**
     * Returns paper trash info sections (matches iOS implementation)
     */
    fun paperTrashInfoSection(): List<TrashInfoSection> {
        return listOf(
            TrashInfoSection(
                title = "Papír",
                text = """Patří do modrého kontejneru. Ze všech tříděných odpadů právě papíru vyprodukuje průměrná česká domácnost za rok hmotnostně nejvíc. Modré kontejnery na papír bývají nejsnazším způsobem, jak se této komodity správně zbavit. Alternativu pak poskytují sběrné suroviny, které ovšem nejsou vždy dostupné. Na druhou stranu nabízejí za papír roztříděný podle druhů finanční odměnu. Velké kusy papíru je vhodné odnášet do sběrných dvorů, pokud jsou k dispozici. Stále časté jsou u nás i školní sběry a soutěže, při kterých se sbírají zejména noviny a časopisy.

Do modrého kontejneru můžeme vhodit časopisy, noviny, sešity, krabice, papírové obaly, cokoliv z lepenky, nebo knihy. Obálky s fóliovými okénky sem můžeme také vhazovat. Nevadí ani papír s kancelářskými sponkami nebo obaly od vajec a ruličky od toaletního papíru. Zpracovatelé si s nimi umí poradit. Bublinkové obálky vhazujeme pouze bez plastového vnitřku!

Papírové obaly a výrobky mohou být značeny těmito značkami.

Do modrého kontejneru naopak nepatří celé svazky knih (vhazovat pouze bez pevné vazby, ve větším počtu patří do sběrného dvora), uhlový (kopírovací papír), mastný nebo jakkoliv znečištěný papír. Tyto materiály nelze už nadále recyklovat. To samé platí o termopapíru (některých účtenkách). Pozor, použité dětské pleny opravdu nepatří do kontejneru na papír, ale do nádoby na směsný odpad!""",
                pdfFileName = "papir_cz_2020.pdf"
            )
        )
    }

    /**
     * Returns bio trash info sections (matches iOS implementation)
     */
    fun bioTrashInfoSection(): List<TrashInfoSection> {
        return listOf(
            TrashInfoSection(
                title = "Bio odpad",
                text = """Jedná se o biologicky rozložitelný odpad rostlinného původu pocházející především z údržby zahrad, ale i rostlinné zbytky z kuchyní. Ke sběru se pak nejčastěji využívají hnědé popelnice a kontejnery, nebo velkokapacitní kontejnery. Případně je možné je odkládat ve sběrném dvoře. Bioodpady je také možné využít na zahradách v kompostérech nebo komunitních a obecních kompostárnách.""",
                pdfFileName = null
            )
        )
    }

    /**
     * Returns mix trash info sections (matches iOS implementation)
     */
    fun mixTrashInfoSection(): List<TrashInfoSection> {
        return listOf(
            TrashInfoSection(
                title = "Směsný odpad",
                text = """Nebo také směsný komunální odpad (SKO) tvoří většinu komunálního odpadu, a to i přesto, že je definován jako zbytek po vytřídění všech výše uvedených odpadů. Síť nádob na jeho sběr je vůbec nejrozšířenější. Jde převážně o plastové popelnice a kontejnery různých objemů převážně černé barvy. Někde je sbírán do kovových pozinkovaných popelnic a kontejnerů. Název sám evokuje, že se v tomto odpadu může objevit ledacos. Při poctivém třídění odpadu zjistíme, že sem patří například porcelán a keramika. Dále také odpady nepodléhající zpětnému odběru složené z více materiálů, které od sebe nejdou snadno oddělit a odpady silně znečištěné jinými látkami jiného než nebezpečného charakteru.

V souvislosti s tímto odpadem se můžeme setkat také s označením zbytkový odpad, které lépe vyjadřuje, že do těchto nádob patří pouze odpady, které nelze jinam vytřídit.""",
                pdfFileName = null
            )
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        tasksManager.cancelTask(loadDaysTaskId)
    }
}