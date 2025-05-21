package cz.novavesodpad.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import cz.novavesodpad.model.TrashDay
import cz.novavesodpad.ui.components.BinView
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime

/**
 * Home screen that displays upcoming trash collection days
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    onSettingsClick: (List<TrashDay>) -> Unit,
    onInfoClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    // Load data when screen appears and when returning to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // Initial data load
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Přehled vývozu odpadu") },
                navigationIcon = {
                    IconButton(onClick = { onSettingsClick(state.days) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Nastavení")
                    }
                },
                actions = {
                    IconButton(onClick = { onInfoClick() }) {
                        Icon(Icons.Default.Info, contentDescription = "Informace")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Current date
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dnešní datum",
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    Text(
                        text = viewModel.titleForDay(LocalDateTime.now())
                    )
                }
                
                Divider()
            }
            
            // Upcoming trash days
            items(state.days) { day ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = viewModel.titleForDay(day.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Bins for this day
                    day.bins.forEach { bin ->
                        BinView(bin = bin)
                    }
                }
                
                Divider()
            }
        }
    }
}