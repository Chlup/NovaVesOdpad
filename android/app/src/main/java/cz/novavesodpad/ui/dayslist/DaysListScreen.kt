package cz.novavesodpad.ui.dayslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.model.TrashMonth
import cz.novavesodpad.ui.components.BinView
import cz.novavesodpad.ui.theme.LocalAppColors
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Days List screen - shows calendar of all trash collection days grouped by month
 * Matches iOS DaysListView design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysListScreen(
    days: List<TrashDay>,
    onBackClick: () -> Unit
) {
    val viewModel: DaysListViewModel = koinViewModel { parametersOf(days) }
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current
    
    // Load data when screen appears
    LaunchedEffect(Unit) {
        viewModel.onAppear()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        text = "Zpět",
                        color = appColors.regularText
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Zpět",
                            tint = appColors.regularText
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.screenBackground)
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Title
                item {
                    TitleView()
                }
                
                // Months and days
                items(state.months) { month ->
                    MonthSection(
                        month = month,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun TitleView() {
    val appColors = LocalAppColors.current
    Text(
        text = "Kalendář všech vývozů",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = appColors.regularText
    )
}

@Composable
private fun MonthSection(
    month: TrashMonth,
    viewModel: DaysListViewModel
) {
    val appColors = LocalAppColors.current
    
    Column {
        // Month header
        Text(
            text = viewModel.titleForMonth(month.date),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = appColors.regularText,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        
        // Days in this month
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            month.days.forEach { day ->
                DayView(
                    day = day,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun DayView(
    day: TrashDay,
    viewModel: DaysListViewModel
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = viewModel.titleForDay(day.date),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.regularText
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                day.bins.forEach { bin ->
                    BinView(bin = bin, size = 30.dp)
                }
            }
        }
    }
}