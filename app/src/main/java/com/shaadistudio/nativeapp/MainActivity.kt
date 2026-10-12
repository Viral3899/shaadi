package com.shaadistudio.nativeapp

import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Wedding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Blush = Color(0xFF9A5367)
private val Ink = Color(0xFF33252A)
private val Canvas = Color(0xFFFFF8F5)
private val Muted = Color(0xFF806F75)
private val themeNames = listOf("Blush Floral", "Royal", "Minimal", "Garden", "Midnight", "Playful")
private val themeColors = listOf(
    listOf(Color(0xFFFFF0F2), Color(0xFF9A5367), Color(0xFFD9A0AD)),
    listOf(Color(0xFFFFF5DF), Color(0xFF8B6B2E), Color(0xFFD6B66E)),
    listOf(Color(0xFFF7F7F5), Color(0xFF333333), Color(0xFFB8B8B3)),
    listOf(Color(0xFFEEF6EC), Color(0xFF3F7048), Color(0xFF9ABF8D)),
    listOf(Color(0xFF211E35), Color(0xFFFFDFA2), Color(0xFF6E6599)),
    listOf(Color(0xFFFFF0D9), Color(0xFFB25A42), Color(0xFFFFB56B))
)
private val tabNames = listOf("Design", "Details", "Functions", "Tradition", "Language", "Gallery", "RSVP", "Saved", "Canvas Studio", "AI Guide")
private val traditions = listOf("Hindu", "Muslim", "Sikh", "Christian", "Jain", "Buddhist", "Parsi / Zoroastrian", "Jewish", "Interfaith / Multicultural", "Custom")
private val languages = listOf("English", "हिन्दी", "ગુજરાતી", "ਪੰਜਾਬੀ", "বাংলা", "मराठी", "தமிழ்", "తెలుగు", "اردو", "العربية")

data class WeddingEvent(
    val id: Long = System.nanoTime(),
    var name: String = "Wedding Ceremony",
    var date: String = "2026-12-12",
    var time: String = "7:00 PM",
    var venue: String = "The Royal Garden, Rajkot",
    var note: String = "The beginning of forever"
) {
    fun json() = JSONObject().put("id", id).put("name", name).put("date", date).put("time", time).put("venue", venue).put("note", note)
    companion object {
        fun from(j: JSONObject) = WeddingEvent(j.optLong("id", System.nanoTime()), j.optString("name"), j.optString("date"), j.optString("time"), j.optString("venue"), j.optString("note"))
    }
}

data class Invitation(
    var groom: String = "Vijay",
    var bride: String = "Rashima",
    var date: String = "2026-12-12",
    var time: String = "7:00 PM",
    var venue: String = "The Royal Garden, Rajkot, Gujarat",
    var family: String = "Together with our families",
    var message: String = "Two hearts, one beautiful journey. We would love for you to be part of our forever.",
    var story: String = "",
    var deadline: String = "",
    var hashtag: String = "#VijayWedsRashima",
    var travel: String = "",
    var theme: Int = 0,
    var tradition: String = "Custom",
    var language: String = "English",
    var events: MutableList<WeddingEvent> = mutableListOf(
        WeddingEvent(name = "Haldi", date = "2026-12-10", time = "10:00 AM", venue = "Family Home", note = "Sunshine, marigolds & happy hearts"),
        WeddingEvent(name = "Mehendi", date = "2026-12-10", time = "5:00 PM", venue = "The Royal Garden", note = "Henna, music & memories"),
        WeddingEvent(name = "Sangeet", date = "2026-12-11", time = "7:00 PM", venue = "The Royal Garden", note = "A night of music and dance"),
        WeddingEvent(name = "Wedding Ceremony", date = "2026-12-12", time = "7:00 PM", venue = "The Royal Garden", note = "The beginning of forever"),
        WeddingEvent(name = "Reception", date = "2026-12-13", time = "7:30 PM", venue = "The Royal Garden", note = "Dinner, blessings & celebration")
    )
) {
    fun json(): JSONObject = JSONObject().apply {
        put("groom", groom); put("bride", bride); put("date", date); put("time", time); put("venue", venue)
        put("family", family); put("message", message); put("story", story); put("deadline", deadline)
        put("hashtag", hashtag); put("travel", travel); put("theme", theme); put("tradition", tradition); put("language", language)
        put("events", JSONArray().apply { events.forEach { put(it.json()) } })
    }
    companion object {
        fun from(j: JSONObject): Invitation {
            val ev = mutableListOf<WeddingEvent>()
            val a = j.optJSONArray("events") ?: JSONArray()
            for (i in 0 until a.length()) ev.add(WeddingEvent.from(a.getJSONObject(i)))
            return Invitation(
                groom = j.optString("groom", "Vijay"), bride = j.optString("bride", "Rashima"),
                date = j.optString("date", "2026-12-12"), time = j.optString("time", "7:00 PM"),
                venue = j.optString("venue", ""), family = j.optString("family", ""),
                message = j.optString("message", ""), story = j.optString("story", ""),
                deadline = j.optString("deadline", ""), hashtag = j.optString("hashtag", ""),
                travel = j.optString("travel", ""), theme = j.optInt("theme", 0).coerceIn(0, themeNames.lastIndex),
                tradition = j.optString("tradition", "Custom"), language = j.optString("language", "English"),
                events = if (ev.isEmpty()) mutableListOf() else ev
            )
        }
    }
}

data class Guest(val id: Long = System.nanoTime(), val name: String, val phone: String, val response: String, val count: Int, val note: String) {
    fun json() = JSONObject().put("id", id).put("name", name).put("phone", phone).put("response", response).put("count", count).put("note", note)
    companion object { fun from(j: JSONObject) = Guest(j.optLong("id"), j.optString("name"), j.optString("phone"), j.optString("response"), j.optInt("count", 1), j.optString("note")) }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ShaadiStudioApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShaadiStudioApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("shaadi_studio_native", Context.MODE_PRIVATE) }
    var invitation by remember { mutableStateOf(loadInvitation(prefs)) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var notice by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf(loadGuests(prefs)) }
    var drafts by remember { mutableStateOf(loadDrafts(prefs)) }
    var gallery by remember { mutableStateOf(loadGallery(prefs)) }
    var guestName by remember { mutableStateOf("") }
    var guestPhone by remember { mutableStateOf("") }
    var guestResponse by remember { mutableStateOf("Accept") }
    var guestCount by remember { mutableStateOf("2") }
    var guestNote by remember { mutableStateOf("") }

    fun persist() { prefs.edit().putString("current", invitation.json().toString()).apply() }
    fun change(block: (Invitation) -> Invitation) { invitation = block(invitation); persist() }

    val galleryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (!uris.isNullOrEmpty()) {
            uris.forEach { uri -> try { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {} }
            gallery = (gallery + uris.map { it.toString() }).distinct()
            prefs.edit().putString("gallery", JSONArray(gallery).toString()).apply()
        }
    }
    val csvCreator = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) try {
            context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { w ->
                w.write("Name,Phone,Response,Guests,Note\n")
                guests.forEach { g -> w.write(listOf(g.name, g.phone, g.response, g.count.toString(), g.note).joinToString(",") { "\"${it.replace("\"", "\"\"")}\"" } + "\n") }
            }
            toast(context, "Guest list exported.")
        } catch (e: Exception) { toast(context, "Could not export CSV: ${e.message}") }
    }
    val pdfCreator = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) createInvitationPdf(context, uri, invitation)
    }

    MaterialTheme(
        colorScheme = lightColorScheme(primary = Blush, onPrimary = Color.White, background = Canvas, surface = Color.White, onSurface = Ink, secondary = Color(0xFFD9A0AD))
    ) {
        Scaffold(containerColor = Canvas, topBar = {
            Column(Modifier.fillMaxWidth().background(Canvas).padding(horizontal = 18.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF3DDE2)), contentAlignment = Alignment.Center) {
                        Text("श्री", color = Blush, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Shaadi Studio", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text("YOUR STORY, BEAUTIFULLY TOLD", fontSize = 9.sp, letterSpacing = 1.5.sp, color = Muted)
                    }
                    IconButton(onClick = { drafts = listOf(invitation.copy(events = invitation.events.toMutableList())) + drafts.filterNot { it.groom == invitation.groom && it.bride == invitation.bride }; prefs.edit().putString("drafts", JSONArray().apply { drafts.forEach { put(it.json()) } }.toString()).apply(); notice = "Invitation saved" }) {
                        Icon(Icons.Default.Save, contentDescription = "Save invitation", tint = Blush)
                    }
                    IconButton(onClick = { shareInvitation(context, invitation) }) { Icon(Icons.Default.IosShare, contentDescription = "Share invitation", tint = Blush) }
                }
                Spacer(Modifier.height(12.dp))
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tabNames.forEachIndexed { index, title ->
                        FilterChip(selected = selectedTab == index, onClick = { selectedTab = index }, label = { Text(title) }, leadingIcon = if (selectedTab == index) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null)
                    }
                }
            }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp, top = 6.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (notice.isNotBlank()) item { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF5E9))) { Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Text(notice, Modifier.weight(1f), color = Color(0xFF34643C)); TextButton(onClick = { notice = "" }) { Text("Dismiss") } } } }
                when (selectedTab) {
                    0 -> {
                        item { SectionTitle("DESIGN YOUR INVITATION", "Choose a style") }
                        item { InvitationPreview(invitation) }
                        item {
                            Text("Visual themes", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink)
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                themeNames.forEachIndexed { index, name ->
                                    val colors = themeColors[index]
                                    Card(onClick = { change { it.copy(theme = index) } }, border = if (invitation.theme == index) BorderStroke(2.dp, colors[1]) else null, colors = CardDefaults.cardColors(containerColor = colors[0]), shape = RoundedCornerShape(18.dp)) {
                                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(54.dp).clip(RoundedCornerShape(14.dp)).background(colors[2]), contentAlignment = Alignment.Center) { Text("❀", color = colors[1], fontSize = 28.sp) }
                                            Spacer(Modifier.width(12.dp))
                                            Column(Modifier.weight(1f)) { Text(name, fontWeight = FontWeight.Bold, color = colors[1]); Text("Live invitation style", color = colors[1].copy(alpha = .8f), fontSize = 12.sp) }
                                            if (invitation.theme == index) Icon(Icons.Default.Check, null, tint = colors[1])
                                        }
                                    }
                                }
                            }
                        }
                        item { Button(onClick = { pdfCreator.launch("shaadi-invitation-${invitation.bride}.pdf") }, modifier = Modifier.fillMaxWidth()) { Text("Download invitation as PDF") } }
                    }
                    1 -> item {
                        SectionTitle("PERSONALIZE", "Wedding details")
                        FormCard {
                            Field("Groom's name", invitation.groom) { value -> change { current -> current.copy(groom = itSafe(value, 80)) } }
                            Field("Bride's name", invitation.bride) { value -> change { current -> current.copy(bride = itSafe(value, 80)) } }
                            Field("Wedding date (YYYY-MM-DD)", invitation.date) { value -> change { current -> current.copy(date = itSafe(value, 40)) } }
                            Field("Time", invitation.time) { value -> change { current -> current.copy(time = itSafe(value, 60)) } }
                            Field("Venue", invitation.venue) { value -> change { current -> current.copy(venue = itSafe(value, 200)) } }
                            Field("Family line", invitation.family) { value -> change { current -> current.copy(family = itSafe(value, 200)) } }
                            Field("Invitation message", invitation.message, true) { value -> change { current -> current.copy(message = itSafe(value, 800)) } }
                            Field("Love story", invitation.story, true) { value -> change { current -> current.copy(story = itSafe(value, 1200)) } }
                            Field("RSVP deadline", invitation.deadline) { value -> change { current -> current.copy(deadline = itSafe(value, 40)) } }
                            Field("Wedding hashtag", invitation.hashtag) { value -> change { current -> current.copy(hashtag = itSafe(value, 100)) } }
                            Field("Travel / accommodation / FAQ", invitation.travel, true) { value -> change { current -> current.copy(travel = itSafe(value, 1200)) } }
                        }
                        item { Button(onClick = { persist(); notice = "Details saved on this device." }, modifier = Modifier.fillMaxWidth()) { Text("Save details") } }
                    }
                    2 -> {
                        item { SectionTitle("YOUR WEDDING TIMELINE", "Wedding functions") }
                        items(invitation.events, key = { it.id }) { ev ->
                            EventEditor(ev, onUpdate = { changed -> change { old -> old.copy(events = old.events.map { if (it.id == ev.id) changed else it }.toMutableList()) } }, onDelete = { change { old -> old.copy(events = old.events.filterNot { it.id == ev.id }.toMutableList()) } })
                        }
                        item { OutlinedButton(onClick = { change { it.copy(events = (it.events + WeddingEvent(name = "New function", date = it.date, venue = it.venue)).toMutableList()) } }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(6.dp)); Text("Add wedding function") } }
                    }
                    3 -> item {
                        SectionTitle("MAKE IT YOURS", "Tradition & ceremony")
                        Text("Choose a tradition preset. No sacred text or ritual is inserted automatically.", color = Muted, fontSize = 13.sp)
                        traditions.forEach { tr ->
                            SelectRow(tr, invitation.tradition == tr) { change { it.copy(tradition = tr) } }
                        }
                    }
                    4 -> item {
                        SectionTitle("SPEAK FROM THE HEART", "Invitation language")
                        Text("UI labels are in English; your invitation text can be written in any language.", color = Muted, fontSize = 13.sp)
                        languages.forEach { lang -> SelectRow(lang, invitation.language == lang) { change { current -> current.copy(language = lang) } } }
                    }
                    5 -> item {
                        SectionTitle("YOUR LOVE STORY", "Photo & video gallery")
                        Text("Select media from your phone. Gallery selections are stored locally; nothing is uploaded.", color = Muted, fontSize = 13.sp)
                        OutlinedButton(onClick = { galleryPicker.launch(arrayOf("image/*", "video/*")) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(8.dp)); Text("Choose photos or videos") }
                        gallery.forEach { uri -> Card { Column(Modifier.padding(12.dp)) { Text(uri.substringAfterLast('/'), maxLines = 1); TextButton(onClick = { gallery = gallery.filterNot { it == uri }; prefs.edit().putString("gallery", JSONArray(gallery).toString()).apply() }) { Text("Remove") } } } }
                    }
                    6 -> item {
                        SectionTitle("GUEST MANAGEMENT", "RSVP responses")
                        val accepted = guests.filter { it.response == "Accept" }.sumOf { it.count }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatCard("Responses", guests.size.toString(), Modifier.weight(1f))
                            StatCard("Accepted guests", accepted.toString(), Modifier.weight(1f))
                        }
                        FormCard {
                            Field("Guest name", guestName) { guestName = it }
                            Field("Phone (optional)", guestPhone, keyboard = KeyboardType.Phone) { guestPhone = it }
                            Text("Response", fontWeight = FontWeight.SemiBold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("Accept", "Decline", "Maybe").forEach { FilterChip(selected = guestResponse == it, onClick = { guestResponse = it }, label = { Text(it) }) } }
                            Field("Number of guests", guestCount, keyboard = KeyboardType.Number) { guestCount = it.filter(Char::isDigit).take(3) }
                            Field("Notes", guestNote) { guestNote = it }
                        }
                        Button(onClick = {
                            if (guestName.isBlank()) notice = "Enter a guest name first."
                            else {
                                guests = listOf(Guest(name = guestName.trim(), phone = guestPhone, response = guestResponse, count = guestCount.toIntOrNull()?.coerceAtLeast(1) ?: 1, note = guestNote)) + guests
                                prefs.edit().putString("guests", JSONArray().apply { guests.forEach { put(it.json()) } }.toString()).apply()
                                guestName = ""; guestPhone = ""; guestCount = "2"; guestNote = ""; notice = "RSVP saved on this device."
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Add RSVP") }
                        OutlinedButton(onClick = { csvCreator.launch("shaadi-studio-guests.csv") }, modifier = Modifier.fillMaxWidth()) { Text("Export guest list (CSV)") }
                        guests.forEach { guest ->
                            Card {
                                Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) { Text(guest.name, fontWeight = FontWeight.Bold); Text("${guest.response} · ${guest.count} guest(s)", color = Muted, fontSize = 12.sp); if (guest.phone.isNotBlank()) Text(guest.phone, color = Muted, fontSize = 12.sp) }
                                    IconButton(onClick = { guests = guests.filterNot { it.id == guest.id }; prefs.edit().putString("guests", JSONArray().apply { guests.forEach { put(it.json()) } }.toString()).apply() }) { Icon(Icons.Default.Delete, null) }
                                }
                            }
                        }
                    }
                    7 -> item {
                        SectionTitle("YOUR SAVED WORK", "Saved invitations")
                        if (drafts.isEmpty()) Text("No saved invitations yet. Tap the save icon above.", color = Muted)
                        drafts.forEachIndexed { idx, draft ->
                            Card {
                                Column(Modifier.padding(14.dp)) {
                                    InvitationPreview(draft)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { invitation = draft; persist(); selectedTab = 0; notice = "Invitation loaded." }, modifier = Modifier.weight(1f)) { Text("Open") }
                                        OutlinedButton(onClick = { drafts = drafts.filterIndexed { i, _ -> i != idx }; prefs.edit().putString("drafts", JSONArray().apply { drafts.forEach { put(it.json()) } }.toString()).apply() }, modifier = Modifier.weight(1f)) { Text("Delete") }
                                    }
                                }
                            }
                        }
                    }
                    8 -> item { NativeCanvasEditor() }
                    9 -> item { AiCharacterGuide() }
                }
                item { Text("Design. Invite. Celebrate.  •  Stored privately on this device", modifier = Modifier.fillMaxWidth().padding(top = 10.dp), color = Muted, fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun SectionTitle(kicker: String, title: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(kicker, color = Blush, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Bold, color = Ink)
    }
}
@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp), content = content)
    }
}
@Composable
private fun Field(label: String, value: String, multiline: Boolean = false, keyboard: KeyboardType = KeyboardType.Text, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Ink)
        OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), minLines = if (multiline) 3 else 1, maxLines = if (multiline) 6 else 1, keyboardOptions = KeyboardOptions(keyboardType = keyboard), shape = RoundedCornerShape(13.dp), singleLine = !multiline)
    }
}
@Composable
private fun InvitationPreview(inv: Invitation) {
    val c = themeColors[inv.theme.coerceIn(0, themeColors.lastIndex)]
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = c[0]), border = BorderStroke(1.dp, c[2])) {
        Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text("✧  ❀  ✧", color = c[1], fontSize = 22.sp)
            Text(inv.family.ifBlank { "Together with our families" }, color = c[1], fontSize = 12.sp)
            Text("REQUEST THE PLEASURE OF YOUR COMPANY", color = c[1], fontSize = 9.sp, letterSpacing = 1.2.sp)
            Text(inv.groom.ifBlank { "Groom" }, fontSize = 28.sp, color = c[1], fontWeight = FontWeight.SemiBold)
            Text("&", fontSize = 22.sp, color = c[1])
            Text(inv.bride.ifBlank { "Bride" }, fontSize = 28.sp, color = c[1], fontWeight = FontWeight.SemiBold)
            HorizontalDivider(color = c[2])
            Text(inv.date.ifBlank { "Date to be announced" }, color = c[1], fontWeight = FontWeight.Bold)
            Text(inv.time, color = c[1])
            Text(inv.venue, color = c[1], fontSize = 13.sp)
            if (inv.message.isNotBlank()) Text(inv.message, color = c[1], fontSize = 13.sp)
            if (inv.hashtag.isNotBlank()) Text(inv.hashtag, color = c[1], fontWeight = FontWeight.Bold)
            Text("♥", color = c[1], fontSize = 18.sp)
        }
    }
}
@Composable
private fun EventEditor(event: WeddingEvent, onUpdate: (WeddingEvent) -> Unit, onDelete: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = Blush)
                Spacer(Modifier.width(8.dp))
                Text(event.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null) }
            }
            Field("Function name", event.name) { onUpdate(event.copy(name = it)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f)) { Field("Date", event.date) { onUpdate(event.copy(date = it)) } }
                Box(Modifier.weight(1f)) { Field("Time", event.time) { onUpdate(event.copy(time = it)) } }
            }
            Field("Venue", event.venue) { onUpdate(event.copy(venue = it)) }
            Field("Note", event.note, true) { onUpdate(event.copy(note = it)) }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectRow(title: String, selected: Boolean, onClick: () -> Unit) {
    Card(onClick = onClick, shape = RoundedCornerShape(14.dp), border = if (selected) BorderStroke(2.dp, Blush) else null) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, Modifier.weight(1f), fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
            if (selected) Icon(Icons.Default.Check, null, tint = Blush)
        }
    }
}
@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color(0xFFF5E5E8))) {
        Column(Modifier.padding(14.dp)) { Text(label, color = Muted, fontSize = 11.sp); Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Blush) }
    }
}
private fun itSafe(value: String, max: Int) = value.take(max)
private fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
private fun loadInvitation(prefs: android.content.SharedPreferences): Invitation = try { prefs.getString("current", null)?.let { Invitation.from(JSONObject(it)) } ?: Invitation() } catch (_: Exception) { Invitation() }
private fun loadGuests(prefs: android.content.SharedPreferences): List<Guest> = try { val a = JSONArray(prefs.getString("guests", "[]")); (0 until a.length()).map { Guest.from(a.getJSONObject(it)) } } catch (_: Exception) { emptyList() }
private fun loadDrafts(prefs: android.content.SharedPreferences): List<Invitation> = try { val a = JSONArray(prefs.getString("drafts", "[]")); (0 until a.length()).map { Invitation.from(a.getJSONObject(it)) } } catch (_: Exception) { emptyList() }
private fun loadGallery(prefs: android.content.SharedPreferences): List<String> = try { val a = JSONArray(prefs.getString("gallery", "[]")); (0 until a.length()).map { a.getString(it) } } catch (_: Exception) { emptyList() }

private fun shareInvitation(context: Context, inv: Invitation) {
    val events = inv.events.joinToString("\n") { "${it.name}: ${it.date}, ${it.time} — ${it.venue}" }
    val body = "${inv.groom} & ${inv.bride}\n${inv.family}\n${inv.date} · ${inv.time}\n${inv.venue}\n\n${inv.message}\n${inv.story}\n${inv.hashtag}\nRSVP by: ${inv.deadline}\n${inv.travel}\n\n$events"
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(android.content.Intent.EXTRA_SUBJECT, "${inv.groom} & ${inv.bride}"); putExtra(android.content.Intent.EXTRA_TEXT, body) }
    context.startActivity(android.content.Intent.createChooser(intent, "Share wedding invitation"))
}
private fun createInvitationPdf(context: Context, uri: Uri, inv: Invitation) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas
    val colors = themeColors[inv.theme.coerceIn(0, themeColors.lastIndex)]
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.rgb((colors[1].red*255).toInt(), (colors[1].green*255).toInt(), (colors[1].blue*255).toInt()) }
    paint.textAlign = Paint.Align.CENTER
    paint.typeface = Typeface.create("serif", Typeface.NORMAL)
    paint.textSize = 24f
    canvas.drawText("✧  ❀  ✧", 297f, 105f, paint)
    paint.textSize = 15f
    canvas.drawText(inv.family.take(65), 297f, 155f, paint)
    paint.textSize = 10f
    canvas.drawText("REQUEST THE PLEASURE OF YOUR COMPANY", 297f, 190f, paint)
    paint.textSize = 36f
    canvas.drawText(inv.groom.take(25), 297f, 260f, paint)
    canvas.drawText("&", 297f, 310f, paint)
    canvas.drawText(inv.bride.take(25), 297f, 360f, paint)
    paint.textSize = 18f
    canvas.drawText(inv.date.take(40), 297f, 420f, paint)
    canvas.drawText(inv.time.take(40), 297f, 452f, paint)
    paint.textSize = 14f
    wrap(inv.venue, 48).take(3).forEachIndexed { i, line -> canvas.drawText(line, 297f, 490f + i*20, paint) }
    paint.textSize = 12f
    wrap(inv.message, 70).take(5).forEachIndexed { i, line -> canvas.drawText(line, 297f, 570f + i*18, paint) }
    paint.textSize = 13f
    canvas.drawText(inv.hashtag.take(50), 297f, 690f, paint)
    paint.textSize = 10f
    canvas.drawText("Made with Shaadi Studio", 297f, 790f, paint)
    doc.finishPage(page)
    try { context.contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }; toast(context, "Invitation PDF saved.") }
    catch (e: Exception) { toast(context, "PDF export failed: ${e.message}") }
    finally { doc.close() }
}
private fun wrap(s: String, max: Int): List<String> = s.split(Regex("\\s+")).fold(mutableListOf("")) { lines, word ->
    val last = lines.last()
    if ((last + " " + word).trim().length > max) lines.add(word) else lines[lines.lastIndex] = (last + " " + word).trim()
    lines
}
