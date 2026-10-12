package com.shaadistudio.nativeapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val guideTraditions = listOf(
    "Hindu", "Muslim", "Sikh", "Christian", "Jain", "Buddhist",
    "Parsi / Zoroastrian", "Jewish", "Interfaith / Multicultural", "Custom"
)
private val guideEvents = listOf(
    "Engagement", "Save the Date", "Haldi", "Mehendi", "Sangeet",
    "Wedding Ceremony", "Reception", "Couple Portrait", "Thank-you Page", "Custom"
)
private val guideStyles = listOf("3D Cartoon", "Cute Illustration", "Anime", "Storybook", "Realistic Cartoon")
private val guideTools = listOf("ComfyUI (advanced, local)", "Fooocus (simpler workflow)", "External tool + import")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiCharacterGuide() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("shaadi_studio_native", Context.MODE_PRIVATE) }
    val clipboard = LocalClipboardManager.current

    var bridePhoto by remember { mutableStateOf(prefs.getString("guide_bride_uri", "") ?: "") }
    var groomPhoto by remember { mutableStateOf(prefs.getString("guide_groom_uri", "") ?: "") }
    var couplePhoto by remember { mutableStateOf(prefs.getString("guide_couple_uri", "") ?: "") }
    var generatedPhoto by remember { mutableStateOf(prefs.getString("guide_generated_uri", "") ?: "") }
    var tradition by remember { mutableStateOf(prefs.getString("guide_tradition", "Hindu") ?: "Hindu") }
    var event by remember { mutableStateOf(prefs.getString("guide_event", "Wedding Ceremony") ?: "Wedding Ceremony") }
    var style by remember { mutableStateOf(prefs.getString("guide_style", "3D Cartoon") ?: "3D Cartoon") }
    var tool by remember { mutableStateOf(prefs.getString("guide_tool", guideTools[0]) ?: guideTools[0]) }
    var region by remember { mutableStateOf(prefs.getString("guide_region", "") ?: "") }
    var brideOutfit by remember { mutableStateOf(prefs.getString("guide_bride_outfit", "") ?: "") }
    var groomOutfit by remember { mutableStateOf(prefs.getString("guide_groom_outfit", "") ?: "") }
    var pose by remember { mutableStateOf(prefs.getString("guide_pose", "Standing together, warm natural expressions") ?: "") }
    var accessories by remember { mutableStateOf(prefs.getString("guide_accessories", "") ?: "") }
    var background by remember { mutableStateOf(prefs.getString("guide_background", "Elegant floral wedding decor") ?: "") }
    var output by remember { mutableStateOf(prefs.getString("guide_output", "Transparent PNG if supported; otherwise simple background") ?: "") }
    var approved by remember { mutableStateOf(prefs.getBoolean("guide_approved", false)) }
    var step by remember { mutableIntStateOf(prefs.getInt("guide_step", 0).coerceIn(0, 3)) }
    var notice by remember { mutableStateOf("") }

    val bridePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            bridePhoto = it.toString(); approved = false
            prefs.edit().putString("guide_bride_uri", bridePhoto).putBoolean("guide_approved", false).apply()
        }
    }
    val groomPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            groomPhoto = it.toString(); approved = false
            prefs.edit().putString("guide_groom_uri", groomPhoto).putBoolean("guide_approved", false).apply()
        }
    }
    val couplePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            couplePhoto = it.toString(); approved = false
            prefs.edit().putString("guide_couple_uri", couplePhoto).putBoolean("guide_approved", false).apply()
        }
    }
    val generatedPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try { context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION) } catch (_: Exception) {}
            generatedPhoto = it.toString(); approved = false
            prefs.edit().putString("guide_generated_uri", generatedPhoto).putBoolean("guide_approved", false).apply()
            notice = "Generated image imported. Compare it with the reference before approving."
        }
    }

    fun saveAll() {
        prefs.edit()
            .putString("guide_bride_uri", bridePhoto).putString("guide_groom_uri", groomPhoto)
            .putString("guide_couple_uri", couplePhoto).putString("guide_generated_uri", generatedPhoto)
            .putString("guide_tradition", tradition).putString("guide_event", event).putString("guide_style", style)
            .putString("guide_tool", tool).putString("guide_region", region).putString("guide_bride_outfit", brideOutfit)
            .putString("guide_groom_outfit", groomOutfit).putString("guide_pose", pose).putString("guide_accessories", accessories)
            .putString("guide_background", background).putString("guide_output", output)
            .putBoolean("guide_approved", approved).putInt("guide_step", step).apply()
        notice = "Guide progress saved on this device."
    }

    val masterPrompt = remember(bridePhoto, groomPhoto, couplePhoto, tradition, region, style) {
        """MASTER COUPLE CHARACTER REFERENCE PROMPT

Use the uploaded reference image(s) as the primary identity reference for the same bride and groom. Create a reusable $style character design for wedding invitations.

Preserve each person's recognizable facial features, face shape, eyes, eyebrows, nose, lips, skin tone, hairstyle, facial hair when present, apparent age, and body proportions. Do not redesign either person or invent distinctive features. Treat bride and groom as separate identities.

Create a clean character reference sheet with front view, three-quarter view, side view, and a full-body couple pose. Keep rendering style, anatomy, lighting, and proportions consistent. Use a simple background. No text, watermark, logos, extra people, or confusing collage layout.

Wedding tradition: $tradition
Region/community (only if supplied by user): ${region.ifBlank { "Not specified — ask before choosing culturally specific details" }}

Never infer religion or community from appearance. Use only the tradition and details selected by the user. Save the approved result as the master identity reference for every future event. Change only clothing, pose, expression, accessories, activity and background."""
    }
    val eventPrompt = remember(masterPrompt, event, brideOutfit, groomOutfit, pose, accessories, background, output, style) {
        """EVENT-SPECIFIC WEDDING CARTOON PROMPT

Use the approved master couple reference and preserve the exact same bride and groom identities. Do not redesign faces.

Event: $event
Tradition: $tradition
Region/community: ${region.ifBlank { "Not specified" }}
Cartoon style: $style
Bride outfit: ${brideOutfit.ifBlank { "Ask user to specify event-appropriate attire" }}
Groom outfit: ${groomOutfit.ifBlank { "Ask user to specify event-appropriate attire" }}
Pose and expression: $pose
Accessories: ${accessories.ifBlank { "Only details appropriate to the selected tradition and approved by the user" }}
Background: $background
Output: $output

Keep face shape, eyes, nose, lips, skin tone, hairstyles, facial hair, apparent age, and body proportions consistent with the master reference. Only change the event-specific outfit, pose, expression, accessories and background. Both faces must be clearly visible. Use natural anatomy and hands, attractive composition, and space around the couple for invitation layout. Do not add text, watermarks, unrelated religious symbols, duplicate people, distorted hands or jewellery."""
    }
    val repairPrompt = remember(event, tradition, style) {
        """FACE-CONSISTENCY REPAIR PROMPT

Edit the current $style illustration using the original approved bride and groom reference images. The current image is for the event: $event. Tradition selected by the user: $tradition.

Correct facial drift only: restore the original face shape, eye shape, eyebrows, nose, lips, skin tone, hairstyle, facial hair, apparent age and body proportions of each person. Keep the existing event outfits, pose, expression intent, accessories, composition and background unless they directly obstruct identity preservation. Do not swap identities or blend the two faces. Do not change the chosen tradition or add new religious details. Keep the illustration style consistent with the approved master reference."""
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionTitle("IN-APP WALKTHROUGH", "Consistent cartoon character guide")
        Text("Create one approved bride-and-groom reference, then reuse it for each wedding function. This guide saves your choices locally and creates prompts; image generation happens only when a compatible generator is configured or you use an external tool.", color = Muted, fontSize = 13.sp)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("1. Photos", "2. Wedding", "3. Prompts", "4. Approve").forEachIndexed { i, label ->
                FilterChip(selected = step == i, onClick = { step = i; saveAll() }, label = { Text(label) })
            }
        }
        when (step) {
            0 -> {
                SectionTitle("STEP 1", "Choose reference images")
                Text("Use clear, well-lit photos. Front-facing images with unobstructed faces usually help. You can provide separate photos or one photo of the couple.", color = Muted, fontSize = 13.sp)
                GuidePhotoRow("Bride reference", bridePhoto) { bridePicker.launch(arrayOf("image/*")) }
                GuidePhotoRow("Groom reference", groomPhoto) { groomPicker.launch(arrayOf("image/*")) }
                GuidePhotoRow("Couple / master cartoon reference", couplePhoto) { couplePicker.launch(arrayOf("image/*")) }
                OutlinedButton(onClick = { bridePhoto = ""; groomPhoto = ""; couplePhoto = ""; approved = false; prefs.edit().remove("guide_bride_uri").remove("guide_groom_uri").remove("guide_couple_uri").putBoolean("guide_approved", false).apply(); notice = "Reference selections cleared." }, modifier = Modifier.fillMaxWidth()) { Text("Clear reference selections") }
                Button(onClick = { step = 1; saveAll() }, modifier = Modifier.fillMaxWidth()) { Text("Continue to wedding details") }
            }
            1 -> {
                SectionTitle("STEP 2", "Select event and style")
                GuideDropdown("Wedding tradition", tradition, guideTraditions) { tradition = it; approved = false }
                GuideField("Region or community (optional)", region) { region = it; approved = false }
                GuideDropdown("Wedding function", event, guideEvents) { event = it; approved = false }
                GuideDropdown("Cartoon style", style, guideStyles) { style = it; approved = false }
                GuideDropdown("Preferred free workflow", tool, guideTools) { tool = it }
                GuideField("Bride's outfit", brideOutfit) { brideOutfit = it; approved = false }
                GuideField("Groom's outfit", groomOutfit) { groomOutfit = it; approved = false }
                GuideField("Pose and expression", pose, true) { pose = it; approved = false }
                GuideField("Accessories / jewellery (optional)", accessories) { accessories = it; approved = false }
                GuideField("Background / decor", background) { background = it; approved = false }
                GuideField("Output format", output) { output = it; approved = false }
                Text("Choose cultural and religious details yourself. The app does not infer religion from appearance.", color = Muted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { step = 0; saveAll() }, modifier = Modifier.weight(1f)) { Text("Back") }
                    Button(onClick = { step = 2; saveAll() }, modifier = Modifier.weight(1f)) { Text("Create prompts") }
                }
            }
            2 -> {
                SectionTitle("STEP 3", "Copy a prompt")
                Text("Recommended tool: $tool", fontWeight = FontWeight.SemiBold)
                when {
                    tool.startsWith("ComfyUI") -> Text("Use ComfyUI with a compatible image model and reference-conditioning workflow such as IP-Adapter FaceID or PuLID. Support depends on the selected model/workflow. This app does not connect to a ComfyUI server automatically.", color = Muted, fontSize = 12.sp)
                    tool.startsWith("Fooocus") -> Text("Use a Fooocus workflow only if its current version and selected model support the image-reference behavior you need. Do not assume it provides the same identity controls as every ComfyUI workflow.", color = Muted, fontSize = 12.sp)
                    else -> Text("Copy the prompt into a suitable image editor/generator, then import the generated image in the next step. Free-service quotas and features can change.", color = Muted, fontSize = 12.sp)
                }
                PromptCard("Master character prompt", masterPrompt) { clipboard.setText(AnnotatedString(masterPrompt)); notice = "Master prompt copied." }
                PromptCard("Event-specific prompt", eventPrompt) { clipboard.setText(AnnotatedString(eventPrompt)); notice = "Event prompt copied." }
                PromptCard("Face-consistency repair prompt", repairPrompt) { clipboard.setText(AnnotatedString(repairPrompt)); notice = "Repair prompt copied." }
                Text("Free tools: ComfyUI — https://github.com/comfyanonymous/ComfyUI", color = Blush, fontSize = 12.sp)
                Text("Fooocus — https://github.com/lllyasviel/Fooocus", color = Blush, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { step = 1; saveAll() }, modifier = Modifier.weight(1f)) { Text("Back") }
                    Button(onClick = { step = 3; saveAll() }, modifier = Modifier.weight(1f)) { Text("Import result") }
                }
            }
            else -> {
                SectionTitle("STEP 4", "Import and approve")
                Text("Generate the illustration using your chosen workflow, then import the saved image. Compare it with the reference before approving it.", color = Muted, fontSize = 13.sp)
                GuidePhotoRow("Generated illustration", generatedPhoto) { generatedPicker.launch(arrayOf("image/*")) }
                Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFFFF1DF))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Identity review checklist", fontWeight = FontWeight.Bold)
                        Text("□ Bride face shape and eyes match\n□ Groom face shape and eyes match\n□ Skin tones and hairstyles match\n□ Facial hair and proportions match\n□ Outfits fit the selected function\n□ Tradition details match your choices", fontSize = 13.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { step = 2; saveAll() }, modifier = Modifier.weight(1f)) { Text("Back to prompts") }
                    Button(onClick = {
                        if (generatedPhoto.isBlank()) notice = "Import a generated image before approving."
                        else {
                            approved = true
                            prefs.edit().putBoolean("guide_approved", true).putString("approved_character_uri", generatedPhoto).apply()
                            notice = "Approved reference saved locally. Reuse the same reference for future functions."
                        }
                        saveAll()
                    }, modifier = Modifier.weight(1f)) { Text(if (approved) "Approved ✓" else "Approve reference") }
                }
                if (approved) Text("Approved master reference is saved on this device. Select a new event in Step 2 to create another prompt.", color = Blush, fontWeight = FontWeight.SemiBold)
            }
        }
        if (notice.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFEAF5E9))) {
                Text(notice, Modifier.padding(12.dp), color = androidx.compose.ui.graphics.Color(0xFF34643C), fontSize = 13.sp)
            }
        }
        OutlinedButton(onClick = { saveAll() }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Save guide progress") }
        Text("Privacy: selected image references and progress are stored locally. No image is uploaded by this guide. Generation is not enabled until a compatible service is configured.", color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun GuidePhotoRow(title: String, uri: String, onPick: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE7D8DD))) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(if (uri.isBlank()) "No image selected" else Uri.parse(uri).lastPathSegment ?: "Image selected", color = Muted, fontSize = 11.sp, maxLines = 1)
            }
            OutlinedButton(onClick = onPick) { Icon(Icons.Default.Image, null); Spacer(Modifier.width(5.dp)); Text(if (uri.isBlank()) "Choose" else "Replace") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuideDropdown(label: String, value: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = value, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor().fillMaxWidth(), trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false }) }
            }
        }
    }
}

@Composable
private fun GuideField(label: String, value: String, multiline: Boolean = false, onChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        OutlinedTextField(value = value, onValueChange = onChange, modifier = Modifier.fillMaxWidth(), minLines = if (multiline) 3 else 1, maxLines = if (multiline) 5 else 1)
    }
}

@Composable
private fun PromptCard(title: String, prompt: String, onCopy: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, androidx.compose.ui.graphics.Color(0xFFE7D8DD))) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide" else "Preview") }
            }
            if (expanded) Text(prompt, fontSize = 12.sp, color = Muted)
            Button(onClick = onCopy, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(8.dp)); Text("Copy full prompt") }
        }
    }
}
