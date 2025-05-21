package cz.novavesodpad.ui.trashinfo

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel

/**
 * Screen that displays information about different types of waste
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashInfoScreen(
    viewModel: TrashInfoViewModel = koinViewModel(),
    onBackClick: () -> Unit,
    onWebClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Co kam patří") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Zpět")
                    }
                },
                actions = {
                    IconButton(onClick = onWebClick) {
                        Icon(Icons.Default.Share, contentDescription = "Webové stránky")
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
                // Section header with bin icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = section.title,
                        tint = section.bin.color
                    )
                }
                
                // Text description if available
                section.text?.let { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // PDF files as buttons
                section.pdfFileUris.forEach { uri ->
                    Button(
                        onClick = { 
                            openPdfFile(context, uri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "PDF dokument",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(text = "Zobrazit ${uri.lastPathSegment?.substringBefore('.') ?: "PDF"}")
                    }
                }
            }
        }
    }
}

/**
 * Opens a PDF file using a Chrome Custom Tab or other PDF viewer app
 */
private fun openPdfFile(context: Context, uri: Uri) {
    try {
        // Copy the asset to a temporary file that can be accessed by external apps
        val fileName = uri.toString()
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
        
        // Use Google Docs Viewer as fallback
        val viewerUrl = "https://docs.google.com/viewer?embedded=true&url=${contentUri}"
        
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()
        
        // Try to open with PDF viewer first
        val pdfIntent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(contentUri, "application/pdf")
        pdfIntent.flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
        
        try {
            context.startActivity(pdfIntent)
        } catch (e: Exception) {
            // Fallback to browser if no PDF viewer is available
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