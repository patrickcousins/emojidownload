@file:Suppress("FunctionName")

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import theme.AppTheme
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.DecodeSequenceMode
import kotlinx.serialization.json.Json
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.File

@Composable
@Preview
fun App(onCloseRequest: () -> Unit) {

    Window(onCloseRequest = onCloseRequest) {

        val scope = rememberCoroutineScope { Dispatchers.IO }
        val viewModel = remember(scope) { ViewModel(scope) }


        var downloadButtonText by remember { mutableStateOf("Download like it's 1999") }
        var downloadButtonEnabled by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var jsonFilePath by remember { mutableStateOf("<select json file path>") }
        var downloadPath by remember { mutableStateOf("<select download folder>>") }
        var isFolderChooserOpen by remember { mutableStateOf(false) }
        var isFileChooseOpen by remember { mutableStateOf(false) }
        val list = viewModel.listStateFlow.collectAsState()
        val currentEmojiState = viewModel.currentEmojiStateFlow.collectAsState()
        val currentEmojiCountState = viewModel.currentEmojiCount.collectAsState()
        val downloadProgress = viewModel.downloadProgressFlow.collectAsState()
        val density = LocalDensity.current

        val folderIcon = rememberVectorPainter(
            remember {
                useResource("folder.xml") { loadXmlImageVector(InputSource(it), density) }
            }
        )
        val downloadIcon = rememberVectorPainter(
            remember {
                useResource("file_save.xml") { loadXmlImageVector(InputSource(it), density) }
            }
        )
        val parseFileIcon = rememberVectorPainter(
            remember {
                useResource("baseline_read_more_24.xml") { loadXmlImageVector(InputSource(it), density) }
            }
        )
        val openFileIcon = rememberVectorPainter(
            remember {
                useResource("baseline_file_open_24.xml") { loadXmlImageVector(InputSource(it), density) }
            }
        )



        AppTheme {
            DirectoryPicker(isFolderChooserOpen) { path ->
                isFolderChooserOpen = false
                downloadPath = path ?: ""
            }
            FilePicker(
                isFileChooseOpen,
                fileExtensions = listOf("js", "json")
            ) { file ->
                isFileChooseOpen = false
                jsonFilePath = file?.path ?: ""
                downloadButtonEnabled = true
            }
            Surface(color = Color(red = 0x20, green = 0x22, blue = 0x21, alpha = 0xFF)) {
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Spacer(Modifier.height(8.dp))
                    Row {

                        TextField(
                            modifier = Modifier
                                .background(color = Color.LightGray).fillMaxWidth(0.5f),
                            value = jsonFilePath,
                            onValueChange = {
                                jsonFilePath = it
                            }
                        )

                        Spacer(Modifier.width(4.dp))

                        Button(
                            onClick = {
                                isFileChooseOpen = true
                            },
                            Modifier.width(200.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    openFileIcon,
                                    contentDescription = "File picker icon",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "select emoji json file",
                                    maxLines = 2
                                )
                            }

                        }

                        Spacer(Modifier.width(4.dp))

                        Button(
                            onClick = {
                                viewModel.readJson(filePath = jsonFilePath)
                            },
                            Modifier.width(200.dp),
                            //colors = ButtonDefaults.outlinedButtonColors(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    parseFileIcon,
                                    contentDescription = "File picker icon",
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "parse/read json file",
                                    maxLines = 2,
                                    minLines = 2
                                )
                            }

                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row {

                        TextField(
                            modifier = Modifier
                                .background(color = Color.LightGray).fillMaxWidth(0.5f),
                            value = downloadPath,
                            onValueChange = {
                                downloadPath = it
                            }
                        )

                        Spacer(Modifier.width(4.dp))

                        Button(
                            onClick = {
                                isFolderChooserOpen = true
                            },
                            Modifier.width(200.dp),
                            //colors = ButtonDefaults.outlinedButtonColors(),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    folderIcon,
                                    contentDescription = "Folder picker icon",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "select download folder",
                                    maxLines = 2
                                )
                            }
                        }

                        Spacer(Modifier.width(4.dp))

                        Button(

                            enabled = downloadButtonEnabled,
                            onClick = {
                                downloadButtonText = "downloading emojis..."
                                downloadButtonEnabled = false
                                isLoading = true
                                viewModel.downloadEmojis(downloadPath)
                            },
                            modifier = Modifier.width(200.dp),
                        ) {
                            Icon(downloadIcon, contentDescription = "download button")
                            Spacer(Modifier.width(8.dp))
                            Text(
                                downloadButtonText,
                                maxLines = 2,
                                minLines = 2
                            )
                        }
                    }


                    Row {
                        Card(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, start = 2.dp, end = 2.dp)) {
                            Column {
                                if (isLoading) {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Downloading emoji ${currentEmojiCountState.value} of ${list.value.size} ${currentEmojiState.value?.name}",
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = downloadProgress.value,
                                        modifier = Modifier.fillMaxWidth().padding(all = 8.dp),
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    val state = rememberLazyListState()

                    LazyColumn(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(end = 12.dp),
                        state = state
                    ) {
                        items(list.value) {
                            if (it != null) {
                                Line(it)
                            }
                        }
                    }
                    VerticalScrollbar(
                        modifier = Modifier.align(Alignment.End).fillMaxHeight(),
                        adapter = rememberScrollbarAdapter(
                            scrollState = state
                        ),

                        )
                }
            }
        }
    }

}

@Composable
fun Line(data: EmojiData) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    )
    {
        Column(modifier = Modifier.width(200.dp).padding(end = 4.dp)) {
            Card(
                modifier = Modifier
                    .padding(all = 2.dp)
                    .align(Alignment.End),
            ) {
                Text(
                    text = data.name,
                    modifier = Modifier.padding(all = 8.dp),
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
            }
        }
        Column(modifier = Modifier.width(48.dp)) {
            Card {
                AsyncImage(
                    load = { loadImageBitmap(data.url) },
                    painterFor = { remember { BitmapPainter(it) } },
                    contentDescription = "emoji preview image",
                    modifier = Modifier
                        .width(36.dp)
                        .height(36.dp)
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier.padding(all = 2.dp),
            ) {
                Text(
                    text = data.url,
                    maxLines = 1,
                    modifier = Modifier.padding(all = 8.dp),
                    color = Color.White,
                    textDecoration = TextDecoration.Underline,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }


    }
}

fun main() = application {
    App(onCloseRequest = ::exitApplication)
}


data class EmojiData(val name: String, val url: String)


class ViewModel(val coroutineScope: CoroutineScope) {

    @OptIn(ExperimentalSerializationApi::class)
    val jsonParser = Json {
        //ignoreUnknownKeys = true
        DecodeSequenceMode.WHITESPACE_SEPARATED
    }

    val listStateFlow = MutableStateFlow<List<EmojiData?>>(mutableStateListOf())
    val currentEmojiStateFlow = MutableStateFlow<EmojiData?>(null)
    val currentEmojiCount = MutableStateFlow(0)
    val downloadProgressFlow = MutableStateFlow(0f)


    @OptIn(ExperimentalSerializationApi::class)
    fun readJson(filePath: String = "") {

        coroutineScope.launch {
            val file = File(filePath)
            println("file can read: ${file.canRead()}")

            if (filePath.isNotBlank() && file.canRead()) {
                val emojiJson: EmojiJson = jsonParser.decodeFromString(file.readText())

                val emojiList = emojiJson.emojis.map { (key, value) ->
                    EmojiData(
                        name = key.trim().removeSuffix("\"").removePrefix("\"").removePrefix("\"").trim(),
                        url = value.trim().removeSuffix(",").removeSuffix("\"").removePrefix("\"")
                    )
                }

                listStateFlow.value = emojiList
            }
        }
    }


    fun downloadEmojis(downloadPath: String) {

        coroutineScope.launch {
            doDownload(downloadPath)
        }
    }

    private suspend fun doDownload(downloadPath: String) {
        //var resume = false
        listStateFlow.value.forEach {
            currentEmojiStateFlow.value = it
            currentEmojiCount.value++
            downloadProgressFlow.value = currentEmojiCount.value.toFloat() / listStateFlow.value.size.toFloat()
            yield()

            it?.let {
                getFile(it.url, it.name, downloadPath)
            }
        }
    }
}

@Composable
fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val image: T? by produceState<T?>(null) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}


suspend fun loadImageBitmap(url: String): ImageBitmap =
    urlStream(url).use(::loadImageBitmap)

//suspend fun loadSvgPainter(url: String, density: Density): Painter =
//    urlStream(url).use { loadSvgPainter(it, density) }
//
suspend fun loadXmlVector(url: String, density: Density): ImageVector =
    urlStream(url).use { loadXmlImageVector(InputSource(it), density) }

fun loadXmlFileVector(file: File, density: Density): ImageVector =
    file.inputStream().buffered().use { loadXmlImageVector(InputSource(it), density) }

private suspend fun urlStream(url: String) = HttpClient(CIO).use {
    println("loading url: $url")
    ByteArrayInputStream(it.get(url).readBytes())
}

private suspend fun getFile(url: String, filename: String, downloadFolder: String) = HttpClient(CIO).use {

    val extension = url.takeLast(3)
    val outputFile = File("$downloadFolder\\$filename.$extension")
    println("downloading [$filename] from [$url] to [$downloadFolder]")
    it.get(url).bodyAsChannel().copyAndClose(outputFile.writeChannel())

}