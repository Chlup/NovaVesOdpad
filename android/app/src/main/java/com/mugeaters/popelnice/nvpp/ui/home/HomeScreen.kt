package com.mugeaters.popelnice.nvpp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.mugeaters.popelnice.nvpp.model.TrashDay
import com.mugeaters.popelnice.nvpp.ui.components.BinView
import com.mugeaters.popelnice.nvpp.ui.theme.LocalAppColors
import org.koin.androidx.compose.koinViewModel

/**
 * Home screen that displays upcoming trash collection days - redesigned to match iOS version
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSettingsClick: (List<TrashDay>) -> Unit,
    onInfoClick: (String) -> Unit,
    onCalendarClick: (List<TrashDay>) -> Unit = {},
    onSortingGuideClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val appColors = LocalAppColors.current
    
    // Load data only on app launch and when returning from background
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(appColors.screenBackground)
    ) {
        if (state.isLoading) {
            LoadingView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 1.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
            // Title
            item {
                TitleView()
            }
            
            // Next trash day - prominent display
            item {
                state.firstDay?.let { firstDay ->
                    NextTrashDayView(
                        day = firstDay,
                        viewModel = viewModel
                    )
                }
            }
            
            // Action buttons row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NotificationsButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSettingsClick(state.allDays) }
                    )
                    CalendarButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onCalendarClick(state.allDays) }
                    )
                }
            }
            
            // Future collections title
            item {
                Text(
                    text = "Budoucí vývozy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = appColors.regularText,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            // Future trash days (next 3 days after the first)
            items(state.homeDays) { day ->
                DayView(
                    day = day,
                    viewModel = viewModel,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            
            // Trash bins info section
            item {
                TrashBinsInfoSection(
                    onInfoClick = onInfoClick,
                    onSortingGuideClick = onSortingGuideClick
                )
            }
            
            // Disclaimer
            item {
                Text(
                    text = "Tato aplikace nereprezentuje jakoukoli státní nebo obecní entitu. Zdrojem dat v této aplikace je oficiální komunikační kanál obce Nová Ves pod Pleší.",
                    style = MaterialTheme.typography.bodySmall,
                    color = appColors.grayText,
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
                )
            }
        }
        }
    }
}

@Composable
private fun LoadingView() {
    val appColors = LocalAppColors.current
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = appColors.regularText
            )
            Text(
                text = "Načítám...",
                style = MaterialTheme.typography.bodyLarge,
                color = appColors.regularText
            )
        }
    }
}

@Composable
private fun TitleView() {
    val appColors = LocalAppColors.current
    Text(
        text = "Popelnice",
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        color = appColors.regularText
    )
}

@Composable
private fun NextTrashDayView(
    day: TrashDay,
    viewModel: HomeViewModel
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = appColors.regularText.copy(alpha = 0.12f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Příští vývoz",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = appColors.regularText,
                modifier = Modifier.padding(bottom = 1.dp)
            )
            
            Row(
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = viewModel.titleForNextDay(day.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = appColors.regularText
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = viewModel.daysToNextTrashDayText(day.daysDifferenceToToday),
                    style = MaterialTheme.typography.bodyLarge,
                    color = appColors.grayText
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                day.bins.forEach { bin ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BinView(bin = bin, size = 35.dp)
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = bin.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = appColors.regularText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    Box(
        modifier = modifier
            .height(45.dp)
            .background(
                color = appColors.buttonDarkBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = appColors.regularText,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = appColors.buttonLightBackground,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Připomeň mi",
                color = appColors.buttonLightBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CalendarButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    Box(
        modifier = modifier
            .height(45.dp)
            .background(
                color = appColors.screenBackground,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = appColors.regularText,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = appColors.regularText,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Kalendář",
                color = appColors.regularText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DayView(
    day: TrashDay,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp),
        shape = RoundedCornerShape(14.dp),
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

@Composable
private fun TrashBinsInfoSection(
    onInfoClick: (String) -> Unit,
    onSortingGuideClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Typy popelnic",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = appColors.regularText
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Informace",
                tint = appColors.regularText,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onSortingGuideClick() }
            )
        }
        
        TrashBinsInfoView(onInfoClick = onInfoClick)
    }
}

@Composable
private fun TrashBinsInfoView(
    onInfoClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BinInfoView(
                bin = TrashDay.Bin.plastic,
                modifier = Modifier.weight(1f),
                onClick = { onInfoClick("plastic") }
            )
            BinInfoView(
                bin = TrashDay.Bin.paper,
                modifier = Modifier.weight(1f),
                onClick = { onInfoClick("paper") }
            )
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BinInfoView(
                bin = TrashDay.Bin.bio,
                modifier = Modifier.weight(1f),
                onClick = { onInfoClick("bio") }
            )
            BinInfoView(
                bin = TrashDay.Bin.mix,
                modifier = Modifier.weight(1f),
                onClick = { onInfoClick("mix") }
            )
        }
    }
}

@Composable
private fun BinInfoView(
    bin: TrashDay.Bin,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    
    Card(
        modifier = modifier
            .height(70.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appColors.sectionBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BinView(bin = bin, size = 35.dp)
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = bin.title,
                style = MaterialTheme.typography.bodyMedium,
                color = appColors.regularText,
                maxLines = 2,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}