package cz.novavesodpad.ui.trashinfo

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.novavesodpad.model.TrashInfoSection
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Screen that displays information about different types of waste
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashInfoScreen(
    sections: List<TrashInfoSection>,
    onBackClick: () -> Unit
) {
    val viewModel: TrashInfoViewModel = koinViewModel { parametersOf(sections) }
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            state.sections.forEach { section ->
                // Section title
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Text description if available
                section.text?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                // PDF button if available
                section.pdfFileName?.let { fileName ->
                    Button(
                        onClick = { 
                            openPdfFile(context, fileName)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "PDF dokument",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Otevřít ${fileName.substringBefore('.').replace('_', ' ')}")
                    }
                }
            }
        }
    }
}

/**
 * Opens a PDF file using an external PDF viewer or Chrome Custom Tab
 */
private fun openPdfFile(context: Context, fileName: String) {
    try {
        // Copy the asset to a temporary file that can be accessed by external apps
        val inputStream = context.assets.open(fileName)
        val tempFile = java.io.File(context.cacheDir, fileName)
        val outputStream = java.io.FileOutputStream(tempFile)
        
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        // Create a content URI using FileProvider
        val contentUri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "cz.novavesodpad.fileprovider",
            tempFile
        )
        
        // Try to open with PDF viewer first
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(contentUri, "application/pdf")
        pdfIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        
        try {
            context.startActivity(pdfIntent)
        } catch (e: Exception) {
            // Fallback to browser with Google Docs Viewer if no PDF viewer is available
            val viewerUrl = "https://docs.google.com/viewer?embedded=true&url=${Uri.encode(contentUri.toString())}"
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, Uri.parse(viewerUrl))
        }
    } catch (e: Exception) {
        // Show error toast if file can't be opened
        android.widget.Toast.makeText(
            context,
            "Nelze otevřít PDF: ${e.localizedMessage}",
            android.widget.Toast.LENGTH_LONG
        ).show()
    }
}