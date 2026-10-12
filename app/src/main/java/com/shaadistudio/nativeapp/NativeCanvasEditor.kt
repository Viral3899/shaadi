package com.shaadistudio.nativeapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Build
import android.os.SystemClock
import java.nio.ByteBuffer
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val canvasStickerEmojis = listOf("💖", "💍", "💐", "🌸", "🌹", "✨", "🕊️", "🪷", "🪔", "🎉", "🥂", "🦚", "🌿", "💌", "🎶", "🧿", "❤️", "💕", "💫", "🌙", "⭐", "🦋")
private val avatarStickers = listOf("👰🏻", "🤵🏻", "👰🏽", "🤵🏽", "👰🏿", "🤵🏿", "💑", "🧑‍🤝‍🧑")
private val cartoonStickers = listOf("👸🏻🤴🏻", "👰🏻‍♀️🤵🏻‍♂️", "💃🕺", "🧚🏻‍♀️💫", "🐻💗", "🐼💕", "🦢💞", "🦚🌸")
private val canvasBackgrounds = listOf(
    Color(0xFFFFF0F2), Color(0xFFFFF5DF), Color(0xFFF7F7F5), Color(0xFFEEF6EC),
    Color(0xFF211E35), Color(0xFFFFF0D9), Color(0xFFEAF3FF), Color(0xFFF4EAFE), Color(0xFFFFFFFF)
)
private val canvasAccentColors = listOf(
    Color(0xFF9A5367), Color(0xFF8B6B2E), Color(0xFF333333), Color(0xFF3F7048),
    Color(0xFFFFDFA2), Color(0xFFB25A42), Color(0xFF345C9C), Color(0xFF7A4AA3), Color(0xFF111111)
)
private val animationNames = listOf("None", "Float", "Pulse", "Sparkle", "Rotate")

private data class CanvasElement(
    val id: Long = System.nanoTime(), val kind: String = "TEXT", val content: String = "Your text",
    val x: Float = .18f, val y: Float = .35f, val size: Float = 24f, val colorIndex: Int = 0,
    val animation: String = "None", val bold: Boolean = false, val rotation: Float = 0f, val scale: Float = 1f
) {
    fun json() = JSONObject().put("id", id).put("kind", kind).put("content", content)
        .put("x", x.toDouble()).put("y", y.toDouble()).put("size", size.toDouble())
        .put("colorIndex", colorIndex).put("animation", animation).put("bold", bold)
        .put("rotation", rotation.toDouble()).put("scale", scale.toDouble())
    companion object {
        fun from(j: JSONObject) = CanvasElement(
            id = j.optLong("id", System.nanoTime()), kind = j.optString("kind", "TEXT"),
            content = j.optString("content", "Text"), x = j.optDouble("x", .18).toFloat(),
            y = j.optDouble("y", .35).toFloat(), size = j.optDouble("size", 24.0).toFloat(),
            colorIndex = j.optInt("colorIndex", 0).coerceIn(0, canvasAccentColors.lastIndex),
            animation = j.optString("animation", "None"), bold = j.optBoolean("bold", false),
            rotation = j.optDouble("rotation", 0.0).toFloat(), scale = j.optDouble("scale", 1.0).toFloat().coerceIn(.25f, 3f)
        )
    }
}
private data class CanvasPage(
    val id: Long = System.nanoTime(), val title: String = "Cover", val background: Int = 0,
    val elements: List<CanvasElement> = listOf(
        CanvasElement(kind = "TEXT", content = "Together with our families", x = .08f, y = .12f, size = 15f, colorIndex = 0),
        CanvasElement(kind = "TEXT", content = "Vijay", x = .20f, y = .29f, size = 34f, colorIndex = 0, bold = true),
        CanvasElement(kind = "TEXT", content = "&", x = .46f, y = .40f, size = 24f, colorIndex = 0),
        CanvasElement(kind = "TEXT", content = "Rashima", x = .16f, y = .48f, size = 34f, colorIndex = 0, bold = true),
        CanvasElement(kind = "TEXT", content = "12 December 2026", x = .18f, y = .67f, size = 17f, colorIndex = 0),
        CanvasElement(kind = "EMOJI", content = "💖", x = .42f, y = .77f, size = 38f, animation = "Float")
    )
) {
    fun json() = JSONObject().put("id", id).put("title", title).put("background", background)
        .put("elements", JSONArray().apply { elements.forEach { put(it.json()) } })
    companion object {
        fun from(j: JSONObject): CanvasPage {
            val a = j.optJSONArray("elements") ?: JSONArray()
            return CanvasPage(j.optLong("id", System.nanoTime()), j.optString("title", "Page"),
                j.optInt("background", 0).coerceIn(0, canvasBackgrounds.lastIndex),
                (0 until a.length()).map { CanvasElement.from(a.getJSONObject(it)) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NativeCanvasEditor() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("shaadi_studio_native", Context.MODE_PRIVATE) }
    var pages by remember {
        mutableStateOf(try {
            val arr = JSONArray(prefs.getString("canvas_pages", "[]"))
            if (arr.length() == 0) listOf(CanvasPage()) else (0 until arr.length()).map { CanvasPage.from(arr.getJSONObject(it)) }
        } catch (_: Exception) { listOf(CanvasPage()) })
    }
    var pageIndex by remember { mutableIntStateOf(0) }
    var selectedId by remember { mutableStateOf<Long?>(pages.firstOrNull()?.elements?.firstOrNull()?.id) }
    var newText by remember { mutableStateOf("Your text") }
    var showTextDialog by remember { mutableStateOf(false) }
    var notice by remember { mutableStateOf("") }

    val page = pages.getOrElse(pageIndex) { pages.first() }
    val selected = page.elements.firstOrNull { it.id == selectedId }

    fun save(next: List<CanvasPage>) {
        pages = next
        prefs.edit().putString("canvas_pages", JSONArray().apply { next.forEach { put(it.json()) } }.toString()).apply()
    }
    fun updatePage(transform: (CanvasPage) -> CanvasPage) {
        save(pages.mapIndexed { i, p -> if (i == pageIndex) transform(p) else p })
    }
    fun updateElement(id: Long, transform: (CanvasElement) -> CanvasElement) {
        updatePage { p -> p.copy(elements = p.elements.map { if (it.id == id) transform(it) else it }) }
    }
    fun addElement(kind: String, content: String, size: Float = 34f) {
        val el = CanvasElement(kind = kind, content = content, x = .25f, y = .40f, size = size,
            animation = if (kind in listOf("EMOJI", "CARTOON", "AVATAR", "IMAGE")) "Float" else "None")
        updatePage { it.copy(elements = it.elements + el) }
        selectedId = el.id
        notice = "$kind added. Drag it on the canvas or adjust its controls below."
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            addElement("IMAGE", uri.toString(), 110f)
        }
    }
    val pdfExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) try { exportPagesPdf(context, uri, pages); notice = "All ${pages.size} pages exported to PDF."; toastCanvas(context, notice) }
        catch (e: Exception) { notice = "PDF export failed: ${e.message}"; toastCanvas(context, notice) }
    }
    val gifExporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/gif")) { uri ->
        if (uri != null) try { exportPagesGif(context, uri, pages); notice = "Animated invitation GIF exported."; toastCanvas(context, notice) }
        catch (e: Exception) { notice = "GIF export failed: ${e.message}"; toastCanvas(context, notice) }
    }
    val mp4Exporter = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("video/mp4")) { uri ->
        if (uri != null) try { exportPagesMp4(context, uri, pages); notice = "Silent animated invitation MP4 exported."; toastCanvas(context, notice) }
        catch (e: Exception) { notice = "MP4 export failed on this device: ${e.message}"; toastCanvas(context, notice) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("CANVA-STYLE EDITOR", color = Blush, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text("Build every page your way", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
        Text("Every element can be edited, rotated, scaled, layered and animated. Add your own photos, PNG cartoon art or stickers from your phone.", color = Muted, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { if (pageIndex > 0) { pageIndex--; selectedId = pages[pageIndex].elements.firstOrNull()?.id } }, enabled = pageIndex > 0) { Icon(Icons.Default.KeyboardArrowLeft, null); Text("Previous") }
            Text("Page ${pageIndex + 1} of ${pages.size}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
            OutlinedButton(onClick = { if (pageIndex < pages.lastIndex) { pageIndex++; selectedId = pages[pageIndex].elements.firstOrNull()?.id } }, enabled = pageIndex < pages.lastIndex) { Text("Next"); Icon(Icons.Default.KeyboardArrowRight, null) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val newPage = CanvasPage(title = "Page ${pages.size + 1}", background = page.background, elements = listOf(
                    CanvasElement(kind = "TEXT", content = "Our Wedding", x = .15f, y = .15f, size = 30f, colorIndex = 0, bold = true),
                    CanvasElement(kind = "EMOJI", content = "✨", x = .43f, y = .42f, size = 40f, animation = "Sparkle")
                ))
                val index = pages.size; save(pages + newPage); pageIndex = index; selectedId = newPage.elements.firstOrNull()?.id
            }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Add, null); Text("Add page") }
            OutlinedButton(onClick = {
                val newPage = page.copy(id = System.nanoTime(), title = "${page.title} copy", elements = page.elements.map { it.copy(id = System.nanoTime()) })
                val index = pages.size; save(pages + newPage); pageIndex = index; selectedId = newPage.elements.firstOrNull()?.id; notice = "Page duplicated."
            }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.FileCopy, null); Text("Duplicate") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { if (pages.size > 1) { val next = pages.filterIndexed { i, _ -> i != pageIndex }; save(next); pageIndex = (pageIndex - 1).coerceAtLeast(0).coerceAtMost(next.lastIndex); selectedId = next[pageIndex].elements.firstOrNull()?.id; notice = "Page deleted." } else notice = "Keep at least one page." }, modifier = Modifier.weight(1f), enabled = pages.size > 1) { Icon(Icons.Default.Delete, null); Text("Delete page") }
            OutlinedButton(onClick = { pdfExporter.launch("shaadi-studio-multipage.pdf") }, modifier = Modifier.weight(1f)) { Text("Export all pages PDF") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { gifExporter.launch("shaadi-studio-animated.gif") }, modifier = Modifier.weight(1f)) { Text("Export GIF") }
            OutlinedButton(onClick = { mp4Exporter.launch("shaadi-studio-animated.mp4") }, modifier = Modifier.weight(1f)) { Text("Export MP4") }
        }

        BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(.70f).clip(RoundedCornerShape(22.dp)).background(canvasBackgrounds[page.background]), contentAlignment = Alignment.TopStart) {
            val canvasW = constraints.maxWidth.toFloat().coerceAtLeast(1f)
            val canvasH = constraints.maxHeight.toFloat().coerceAtLeast(1f)
            page.elements.forEach { element ->
                val transition = rememberInfiniteTransition(label = "element-${element.id}")
                val animated by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(1300, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "motion-${element.id}")
                val verticalOffset = if (element.animation == "Float") (animated * 9f).roundToInt() else 0
                val pulse = if (element.animation == "Pulse") 1f + animated * .12f else 1f
                val rotate = element.rotation + if (element.animation == "Rotate") animated * 18f else 0f
                val sparkle = if (element.animation == "Sparkle" && animated > .5f) "✦ " else ""
                val isSelected = selectedId == element.id
                Box(
                    Modifier.offset { IntOffset((element.x.coerceIn(0f, .88f) * canvasW).roundToInt(), (element.y.coerceIn(0f, .93f) * canvasH).roundToInt() + verticalOffset) }
                        .graphicsLayer { rotationZ = rotate; scaleX = element.scale * pulse; scaleY = element.scale * pulse }
                        .pointerInput(element.id, pageIndex) {
                            detectDragGestures(onDragStart = { selectedId = element.id }) { change, drag ->
                                change.consume()
                                val dx = (drag.x / canvasW).coerceIn(-.15f, .15f); val dy = (drag.y / canvasH).coerceIn(-.15f, .15f)
                                updateElement(element.id) { it.copy(x = (it.x + dx).coerceIn(0f, .88f), y = (it.y + dy).coerceIn(0f, .93f)) }
                            }
                        }
                        .clip(RoundedCornerShape(8.dp)).background(if (isSelected) Color.White.copy(alpha = .78f) else Color.Transparent)
                        .then(if (isSelected) Modifier.padding(3.dp) else Modifier)
                ) {
                    if (element.kind == "IMAGE") {
                        AsyncImage(model = element.content, contentDescription = "Invitation image sticker", modifier = Modifier.size((element.size * .85f).dp).clip(RoundedCornerShape(8.dp)))
                    } else {
                        Text(sparkle + element.content, color = canvasAccentColors[element.colorIndex.coerceIn(0, canvasAccentColors.lastIndex)], fontSize = (element.size * pulse).sp, fontWeight = if (element.bold) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(2.dp))
                    }
                }
            }
        }
        Text("Drag elements to move them. Select a layer below to edit it.", color = Muted, fontSize = 12.sp)
        Text("ADD ELEMENT", color = Blush, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { newText = "Your text"; showTextDialog = true }) { Icon(Icons.Default.Add, null); Text("Text") }
            OutlinedButton(onClick = { addElement("TEXT", "Save the Date", 24f) }) { Text("Heading") }
            OutlinedButton(onClick = { addElement("TEXT", "Join us for our wedding", 16f) }) { Text("Body text") }
            OutlinedButton(onClick = { imagePicker.launch(arrayOf("image/*")) }) { Icon(Icons.Default.Image, null); Text("Photo / PNG") }
        }
        Text("Emoji stickers", fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { canvasStickerEmojis.forEach { emoji -> OutlinedButton(onClick = { addElement("EMOJI", emoji, 32f) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Text(emoji, fontSize = 21.sp) } } }
        Text("Couple avatars", fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { avatarStickers.forEach { sticker -> OutlinedButton(onClick = { addElement("AVATAR", sticker, 42f) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Text(sticker, fontSize = 23.sp) } } }
        Text("Cartoon sticker presets", fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { cartoonStickers.forEach { sticker -> OutlinedButton(onClick = { addElement("CARTOON", sticker, 42f) }, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) { Text(sticker, fontSize = 21.sp) } } }
        Text("Page background", fontWeight = FontWeight.Bold, color = Ink)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { canvasBackgrounds.forEachIndexed { i, bg ->
            Surface(modifier = Modifier.size(36.dp).clip(CircleShape), color = bg, border = BorderStroke(if (page.background == i) 3.dp else 1.dp, if (page.background == i) Blush else Color.LightGray), onClick = { updatePage { it.copy(background = i) } }) { }
        } }
        selected?.let { el ->
            FormCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Selected element", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { updatePage { it.copy(elements = it.elements.filterNot { item -> item.id == el.id }) }; selectedId = null }) { Icon(Icons.Default.Delete, "Delete selected element") }
                }
                Field(if (el.kind == "IMAGE") "Image URI (or replace with text for a text sticker)" else "Text / emoji / sticker", el.content, multiline = true) { value -> updateElement(el.id) { it.copy(content = value.take(500)) } }
                Text("Size: ${el.size.roundToInt()}", fontWeight = FontWeight.Medium)
                Slider(value = el.size, onValueChange = { value -> updateElement(el.id) { it.copy(size = value) } }, valueRange = 10f..140f)
                Text("Scale: ${"%.2f".format(el.scale)}×", fontWeight = FontWeight.Medium)
                Slider(value = el.scale, onValueChange = { value -> updateElement(el.id) { it.copy(scale = value) } }, valueRange = .25f..3f)
                Text("Rotation: ${el.rotation.roundToInt()}°", fontWeight = FontWeight.Medium)
                Slider(value = el.rotation, onValueChange = { value -> updateElement(el.id) { it.copy(rotation = value) } }, valueRange = -180f..180f)
                Text("Text / sticker color", fontWeight = FontWeight.Medium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) { canvasAccentColors.forEachIndexed { i, color ->
                    Surface(modifier = Modifier.size(32.dp).clip(CircleShape), color = color, border = BorderStroke(if (el.colorIndex == i) 3.dp else 1.dp, if (el.colorIndex == i) Color.Black else Color.LightGray), onClick = { updateElement(el.id) { it.copy(colorIndex = i) } }) { }
                } }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = el.bold, onCheckedChange = { value -> updateElement(el.id) { it.copy(bold = value) } }); Text("Bold") }
                Text("Animation", fontWeight = FontWeight.Medium)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) { animationNames.forEach { anim -> FilterChip(selected = el.animation == anim, onClick = { updateElement(el.id) { it.copy(animation = anim) } }, label = { Text(anim) }) } }
                Text("Position", fontWeight = FontWeight.Medium)
                Text("Horizontal", color = Muted, fontSize = 12.sp); Slider(value = el.x, onValueChange = { value -> updateElement(el.id) { it.copy(x = value) } }, valueRange = 0f..0.88f)
                Text("Vertical", color = Muted, fontSize = 12.sp); Slider(value = el.y, onValueChange = { value -> updateElement(el.id) { it.copy(y = value) } }, valueRange = 0f..0.93f)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { updatePage { p -> val list = p.elements.toMutableList(); val i = list.indexOfFirst { it.id == el.id }; if (i > 0) { val a = list[i-1]; list[i-1] = list[i]; list[i] = a }; p.copy(elements = list) } }, modifier = Modifier.weight(1f)) { Text("Send backward") }
                    OutlinedButton(onClick = { updatePage { p -> val list = p.elements.toMutableList(); val i = list.indexOfFirst { it.id == el.id }; if (i >= 0 && i < list.lastIndex) { val a = list[i+1]; list[i+1] = list[i]; list[i] = a }; p.copy(elements = list) } }, modifier = Modifier.weight(1f)) { Text("Bring forward") }
                }
            }
        }
        Text("LAYERS", color = Blush, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        page.elements.forEach { el -> Card(onClick = { selectedId = el.id; newText = el.content }, border = if (selectedId == el.id) BorderStroke(2.dp, Blush) else null) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (el.kind == "IMAGE") "🖼 ${el.content.substringAfterLast('/').take(20)}" else el.content.take(26), Modifier.weight(1f), maxLines = 1)
                Text(el.kind, color = Muted, fontSize = 10.sp); if (selectedId == el.id) Icon(Icons.Default.Check, null, tint = Blush)
            }
        } }
        if (notice.isNotBlank()) Text(notice, color = Blush, fontSize = 12.sp)
        OutlinedButton(onClick = { save(pages); notice = "Canvas project saved privately on this device." }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save canvas project") }
        Text("PDF exports every page. GIF and silent MP4 exports animate all pages using Android's built-in media APIs; MP4 support depends on your phone's hardware encoder. AI-generated character creation is not included offline—import your own cartoon PNGs using Photo / PNG.", color = Muted, fontSize = 11.sp)
    }
    if (showTextDialog) AlertDialog(
        onDismissRequest = { showTextDialog = false }, title = { Text("Add text element") },
        text = { OutlinedTextField(value = newText, onValueChange = { newText = it }, label = { Text("Text") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)) },
        confirmButton = { TextButton(onClick = { if (newText.isNotBlank()) addElement("TEXT", newText.take(180), 24f); showTextDialog = false }) { Text("Add") } },
        dismissButton = { TextButton(onClick = { showTextDialog = false }) { Text("Cancel") } }
    )
}

private fun toastCanvas(context: Context, message: String) = Toast.makeText(context, message, Toast.LENGTH_LONG).show()
private fun androidColor(color: Color): Int = android.graphics.Color.argb((color.alpha*255).toInt(), (color.red*255).toInt(), (color.green*255).toInt(), (color.blue*255).toInt())

private fun renderPage(context: Context, page: CanvasPage, width: Int, height: Int, frame: Int = 0): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(androidColor(canvasBackgrounds[page.background.coerceIn(0, canvasBackgrounds.lastIndex)]))
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    page.elements.forEach { element ->
        val x = element.x.coerceIn(0f, .98f) * width
        val y = element.y.coerceIn(0f, .98f) * height
        val pulse = if (element.animation == "Pulse") 1f + (frame % 3) * .06f else 1f
        val floatOffset = if (element.animation == "Float") sin(frame * 1.2).toFloat() * 12f else 0f
        val rotate = element.rotation + if (element.animation == "Rotate") frame * 12f else 0f
        canvas.save()
        canvas.rotate(rotate, x, y)
        if (element.kind == "IMAGE") {
            try {
                val img = context.contentResolver.openInputStream(Uri.parse(element.content))?.use { BitmapFactory.decodeStream(it) }
                if (img != null) {
                    val size = (element.size * 2.2f * element.scale).coerceIn(30f, width * .65f)
                    val dest = RectF(x, y + floatOffset, x + size, y + size)
                    canvas.drawBitmap(img, null, dest, paint); img.recycle()
                }
            } catch (_: Exception) { }
        } else {
            paint.color = androidColor(canvasAccentColors[element.colorIndex.coerceIn(0, canvasAccentColors.lastIndex)])
            paint.textSize = (element.size * 1.7f * element.scale * pulse).coerceIn(12f, 180f)
            paint.typeface = if (element.bold) Typeface.create("serif", Typeface.BOLD) else Typeface.create("serif", Typeface.NORMAL)
            val content = (if (element.animation == "Sparkle" && frame % 2 == 1) "✦ " else "") + element.content
            canvas.drawText(content.take(300), x, y + floatOffset, paint)
        }
        canvas.restore()
    }
    return bitmap
}

private fun exportPagesPdf(context: Context, uri: Uri, pages: List<CanvasPage>) {
    val pdf = PdfDocument()
    try {
        pages.forEachIndexed { index, page ->
            val info = PdfDocument.PageInfo.Builder(595, 842, index + 1).create()
            val outPage = pdf.startPage(info)
            val bmp = renderPage(context, page, 595, 842)
            outPage.canvas.drawBitmap(bmp, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            bmp.recycle()
            pdf.finishPage(outPage)
        }
        context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) } ?: error("Unable to open output file")
    } finally { pdf.close() }
}

private fun exportPagesGif(context: Context, uri: Uri, pages: List<CanvasPage>) {
    require(pages.isNotEmpty()) { "Add at least one page first" }
    context.contentResolver.openOutputStream(uri)?.use { output ->
        val width = 360; val height = 504
        val frames = mutableListOf<Bitmap>()
        try {
            pages.forEach { page ->
                // A few motion frames per page make sticker animation visible in the exported GIF.
                repeat(3) { frame -> frames.add(renderPage(context, page, width, height, frame)) }
            }
            SimpleGifEncoder.write(output, frames, width, height, delayHundredths = 45)
        } finally { frames.forEach { it.recycle() } }
    } ?: error("Unable to open output file")
}

private fun exportPagesMp4(context: Context, uri: Uri, pages: List<CanvasPage>) {
    require(pages.isNotEmpty()) { "Add at least one page first" }
    val width = 352 // 16-pixel aligned for broad H.264 encoder compatibility
    val height = 512
    val fps = 12
    val mime = MediaFormat.MIMETYPE_VIDEO_AVC
    val encoderInfo = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos.firstOrNull { info ->
        info.isEncoder && info.supportedTypes.any { it.equals(mime, ignoreCase = true) } &&
            info.getCapabilitiesForType(mime).colorFormats.any { it == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar || it == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar }
    } ?: error("This phone does not expose a compatible H.264 encoder")
    val caps = encoderInfo.getCapabilitiesForType(mime)
    val colorFormat = when {
        caps.colorFormats.contains(MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) -> MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
        else -> MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar
    }
    val format = MediaFormat.createVideoFormat(mime, width, height).apply {
        setInteger(MediaFormat.KEY_COLOR_FORMAT, colorFormat)
        setInteger(MediaFormat.KEY_BIT_RATE, 1_500_000)
        setInteger(MediaFormat.KEY_FRAME_RATE, fps)
        setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }
    val codec = MediaCodec.createByCodecName(encoderInfo.name)
    val descriptor = context.contentResolver.openFileDescriptor(uri, "rw") ?: error("Cannot open output file")
    var muxer: MediaMuxer? = null
    var muxerStarted = false
    var trackIndex = -1
    val info = MediaCodec.BufferInfo()
    try {
        muxer = MediaMuxer(descriptor.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        var frameNumber = 0
        var outputDone = false
        fun drain(endOfStream: Boolean) {
            var idleLoops = 0
            while (true) {
                val outIndex = codec.dequeueOutputBuffer(info, if (endOfStream) 10_000 else 0)
                when {
                    outIndex == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        if (!endOfStream || idleLoops++ > 100) return
                    }
                    outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        check(!muxerStarted) { "Encoder changed output format more than once" }
                        trackIndex = muxer!!.addTrack(codec.outputFormat)
                        muxer.start(); muxerStarted = true
                    }
                    outIndex >= 0 -> {
                        val buffer = codec.getOutputBuffer(outIndex)
                        if (buffer != null && info.size > 0 && muxerStarted) {
                            buffer.position(info.offset); buffer.limit(info.offset + info.size)
                            muxer!!.writeSampleData(trackIndex, buffer, info)
                        }
                        val eos = (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0
                        codec.releaseOutputBuffer(outIndex, false)
                        if (eos) { outputDone = true; return }
                    }
                }
            }
        }
        pages.forEach { page ->
            repeat(3) { motionFrame ->
                val inputIndex = codec.dequeueInputBuffer(10_000)
                check(inputIndex >= 0) { "Encoder input buffer timed out" }
                val input = codec.getInputBuffer(inputIndex) ?: error("Missing encoder input buffer")
                input.clear()
                val bitmap = renderPage(context, page, width, height, motionFrame)
                val yuv = bitmapToYuv420(bitmap, colorFormat)
                bitmap.recycle()
                check(yuv.size <= input.capacity()) { "Frame is larger than the encoder input buffer" }
                input.put(yuv)
                val pts = frameNumber * 1_000_000L / fps
                codec.queueInputBuffer(inputIndex, 0, yuv.size, pts, 0)
                frameNumber++
                drain(false)
            }
        }
        val eosIndex = codec.dequeueInputBuffer(10_000)
        check(eosIndex >= 0) { "Could not finish MP4 stream" }
        codec.queueInputBuffer(eosIndex, 0, 0, frameNumber * 1_000_000L / fps, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
        drain(true)
        check(muxerStarted && outputDone) { "Video encoder did not finish correctly" }
    } finally {
        try { codec.stop() } catch (_: Exception) { }
        codec.release()
        try { if (muxerStarted) muxer?.stop() } catch (_: Exception) { }
        muxer?.release()
        descriptor.close()
    }
}

private fun bitmapToYuv420(bitmap: Bitmap, colorFormat: Int): ByteArray {
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    val frameSize = width * height
    val out = ByteArray(frameSize + frameSize / 2)
    var yIndex = 0
    var uIndex = frameSize
    var vIndex = frameSize + frameSize / 4
    val semiPlanar = colorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar
    for (row in 0 until height) {
        for (col in 0 until width) {
            val c = pixels[row * width + col]
            val r = android.graphics.Color.red(c); val g = android.graphics.Color.green(c); val b = android.graphics.Color.blue(c)
            val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
            out[yIndex++] = y.coerceIn(0, 255).toByte()
            if (row % 2 == 0 && col % 2 == 0) {
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                if (semiPlanar) {
                    out[uIndex++] = u.coerceIn(0, 255).toByte()
                    out[uIndex++] = v.coerceIn(0, 255).toByte()
                } else {
                    out[uIndex++] = u.coerceIn(0, 255).toByte()
                    out[vIndex++] = v.coerceIn(0, 255).toByte()
                }
            }
        }
    }
    return out
}

/** Small dependency-free GIF89a encoder. Palette uses a deterministic 3-3-2 RGB quantizer. */
private object SimpleGifEncoder {
    fun write(output: OutputStream, frames: List<Bitmap>, width: Int, height: Int, delayHundredths: Int) {
        output.write("GIF89a".toByteArray(Charsets.US_ASCII))
        le16(output, width); le16(output, height)
        output.write(0xF7); output.write(0); output.write(0)
        for (i in 0..255) {
            val r = ((i shr 5) and 7) * 255 / 7
            val g = ((i shr 2) and 7) * 255 / 7
            val b = (i and 3) * 255 / 3
            output.write(r); output.write(g); output.write(b)
        }
        output.write(byteArrayOf(0x21, 0xFF.toByte(), 0x0B, 0x4E, 0x45, 0x54, 0x53, 0x43, 0x41, 0x50, 0x45, 0x32, 0x2E, 0x30, 0x03, 0x01, 0x00, 0x00, 0x00))
        frames.forEach { bitmap ->
            val scaled = if (bitmap.width == width && bitmap.height == height) bitmap else Bitmap.createScaledBitmap(bitmap, width, height, true)
            val indices = IntArray(width * height)
            val pixels = IntArray(width * height)
            scaled.getPixels(pixels, 0, width, 0, 0, width, height)
            for (i in pixels.indices) {
                val c = pixels[i]; indices[i] = ((android.graphics.Color.red(c) shr 5) shl 5) or ((android.graphics.Color.green(c) shr 5) shl 2) or (android.graphics.Color.blue(c) shr 6)
            }
            output.write(byteArrayOf(0x21, 0xF9.toByte(), 0x04, 0x04))
            le16(output, delayHundredths); output.write(0); output.write(0)
            output.write(0x2C); le16(output, 0); le16(output, 0); le16(output, width); le16(output, height); output.write(0)
            output.write(8)
            val compressed = lzw(indices)
            var offset = 0
            while (offset < compressed.size) {
                val count = minOf(255, compressed.size - offset); output.write(count); output.write(compressed, offset, count); offset += count
            }
            output.write(0)
            if (scaled !== bitmap) scaled.recycle()
        }
        output.write(0x3B); output.flush()
    }
    private fun le16(out: OutputStream, n: Int) { out.write(n and 255); out.write((n shr 8) and 255) }
    private fun lzw(data: IntArray): ByteArray {
        val clear = 256; val end = 257
        var codeSize = 9; var nextCode = 258
        var dict = HashMap<Int, Int>(4096)
        val out = ByteArrayOutputStream()
        var accumulator = 0; var bits = 0
        fun emit(code: Int) {
            accumulator = accumulator or (code shl bits); bits += codeSize
            while (bits >= 8) { out.write(accumulator and 255); accumulator = accumulator ushr 8; bits -= 8 }
        }
        emit(clear)
        if (data.isNotEmpty()) {
            var prefix = data[0]
            for (i in 1 until data.size) {
                val symbol = data[i]; val key = (prefix shl 8) or symbol
                val found = dict[key]
                if (found != null) prefix = found else {
                    emit(prefix)
                    if (nextCode < 4096) {
                        dict[key] = nextCode++
                        if (nextCode > (1 shl codeSize) && codeSize < 12) codeSize++
                    } else {
                        emit(clear); dict = HashMap(4096); codeSize = 9; nextCode = 258
                    }
                    prefix = symbol
                }
            }
            emit(prefix)
        }
        emit(end)
        if (bits > 0) out.write(accumulator and 255)
        return out.toByteArray()
    }
}
