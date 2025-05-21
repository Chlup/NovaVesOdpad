package cz.novavesodpad.ui.trashinfo

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.model.TrashInfoSection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel state for the TrashInfo screen
 */
data class TrashInfoState(
    val sections: List<TrashInfoSection> = emptyList()
)

/**
 * ViewModel for the TrashInfo screen
 */
class TrashInfoViewModel(
    private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(TrashInfoState())
    val state: StateFlow<TrashInfoState> = _state.asStateFlow()
    
    init {
        loadTrashInfoSections()
    }
    
    /**
     * Loads trash info sections with details about different waste types
     */
    private fun loadTrashInfoSections() {
        val sections = listOf(
            TrashInfoSection(
                title = "Směsný komunální odpad",
                bin = TrashDay.Bin.mix,
                text = "Do černé popelnice na směsný komunální odpad patří odpady, které zbyly po vytřídění. " +
                       "Nepatří sem nebezpečné odpady, stavební odpad, bioodpad a recyklovatelné složky odpadu."
            ),
            TrashInfoSection(
                title = "Plasty",
                bin = TrashDay.Bin.plastic,
                text = "Do žluté popelnice na plast patří fólie, sáčky, plastové tašky, sešlápnuté PET láhve, obaly od pracích, " +
                       "čistících a kosmetických přípravků, kelímky od jogurtů, mléčných výrobků, balící fólie, " +
                       "obaly od CD disků a další výrobky z plastů.",
                pdfFileUris = listOf(Uri.parse("plasty_cz_2020.pdf"))
            ),
            TrashInfoSection(
                title = "Papír",
                bin = TrashDay.Bin.paper,
                text = "Do modré popelnice na papír patří časopisy, noviny, sešity, krabice, papírové obaly, " +
                       "cokoliv z lepenky, knihy, obálky s fóliovými okýnky a papír s kancelářskými sponkami. " +
                       "Nepatří sem uhlový, mastný, promáčený nebo jakkoliv znečištěný papír.",
                pdfFileUris = listOf(Uri.parse("papir_cz_2020.pdf"))
            ),
            TrashInfoSection(
                title = "Bio odpad",
                bin = TrashDay.Bin.bio,
                text = "Do hnědé popelnice na bioodpad patří tráva, listí, větvičky, zbytky ovoce, zeleniny, " +
                       "čajové sáčky, kávová sedlina, skořápky od vajec apod. " +
                       "Nepatří sem zbytky jídel, oleje, kosti, maso, uhynulá zvířata, biologicky nerozložitelné odpady a jiné."
            )
        )
        
        _state.update { it.copy(sections = sections) }
    }
}