package org.example.project.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.dtos.ExerciseContentDto
import org.example.project.dtos.WordDto

data class VocabularyEntry(
    val english: String,
    val spanish: String,
    val phonetic: String? = null,
    val description: String? = null,
)

@Composable
fun ExerciseInfoSections(
    content: ExerciseContentDto?,
    words: List<WordDto>,
    vocabularyExpanded: Boolean,
    onToggleVocabulary: () -> Unit,
) {
    if (content == null) {
        VocabularyExpandableCard(
            entries = mergeVocabulary(words, emptyList()),
            expanded = vocabularyExpanded,
            onToggle = onToggleVocabulary
        )
        return
    }

    val parsed = remember(content.grammarExplanation) {
        parseGrammarAndVocabulary(content.grammarExplanation)
    }
    val grammarOnly = parsed.first
    val mergedVocabulary = remember(words, parsed.second) {
        mergeVocabulary(words, parsed.second)
    }

    ContentSectionCard(
        title = "Contexto",
        iconTint = Color(0xFF1565C0),
        icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF1565C0)) },
        content = {
            Text(
                text = "Tipo: ${content.contentType}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            InlineVocabularyText(text = content.textContent, entries = mergedVocabulary)
        }
    )

    ContentSectionCard(
        title = "Gramatica",
        iconTint = Color(0xFF6A1B9A),
        icon = { Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF6A1B9A)) },
        content = {
            Text(
                text = grammarOnly.ifBlank { "Sin explicacion gramatical adicional." },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )

    if (!content.audioUrl.isNullOrBlank()) {
        ContentSectionCard(
            title = "Audio",
            iconTint = Color(0xFF00897B),
            icon = { Icon(Icons.Default.Quiz, contentDescription = null, tint = Color(0xFF00897B)) },
            content = {
                Text(
                    text = content.audioUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF37474F)
                )
            }
        )
    }

    VocabularyExpandableCard(
        entries = mergedVocabulary,
        expanded = vocabularyExpanded,
        onToggle = onToggleVocabulary
    )
}

@Composable
fun ContentSectionCard(
    title: String,
    iconTint: Color,
    icon: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icon()
                Text(title, fontWeight = FontWeight.Bold, color = iconTint)
            }
            content()
        }
    }
}

@Composable
fun VocabularyExpandableCard(
    entries: List<VocabularyEntry>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5FFF8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = Color(0xFF2E7D32))
                    Text("Vocabulario", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${entries.size} palabra(s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32)
                    )
                }
            }

            if (entries.isEmpty()) {
                Text(
                    text = "No hay vocabulario asociado a este ejercicio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            } else if (expanded) {
                entries.forEach { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(entry.english, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
                            Text("Significado: ${entry.spanish}", style = MaterialTheme.typography.bodySmall)
                            entry.phonetic?.takeIf { it.isNotBlank() }?.let {
                                Text("Fonetica: $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            entry.description?.takeIf { it.isNotBlank() }?.let {
                                Text("Descripcion: $it", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            } else {
                val preview = entries.take(3).joinToString(" | ") { it.english }
                Text(
                    text = "Vista previa: $preview",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

fun parseGrammarAndVocabulary(grammarExplanation: String): Pair<String, List<VocabularyEntry>> {
    val source = grammarExplanation.trim()
    if (source.isBlank()) return "" to emptyList()

    val markerRegex = Regex("(?i)\\bvocabulario\\s*:")
    val marker = markerRegex.find(source) ?: return source to emptyList()

    val grammarOnly = source.substring(0, marker.range.first).trim().trimEnd(';')
    val vocabularyRaw = source.substring(marker.range.last + 1)

    val entryRegex = Regex("^([^=:\\-]+?)\\s*(?:=|:|-)\\s*(.+)$")
    val parsed = vocabularyRaw
        .split(';', '\n')
        .mapNotNull { chunk ->
            val normalized = chunk.trim().trim('.').trim()
            if (normalized.isBlank()) return@mapNotNull null

            val match = entryRegex.find(normalized)
            if (match != null) {
                val english = match.groupValues[1].trim()
                val spanish = match.groupValues[2].trim()
                if (english.isBlank() || spanish.isBlank()) null else VocabularyEntry(english = english, spanish = spanish)
            } else {
                null
            }
        }

    return grammarOnly to parsed
}

fun mergeVocabulary(words: List<WordDto>, parsedFromGrammar: List<VocabularyEntry>): List<VocabularyEntry> {
    val merged = linkedMapOf<String, VocabularyEntry>()

    words.forEach { word ->
        val key = word.english.trim().lowercase()
        if (key.isNotBlank()) {
            merged[key] = VocabularyEntry(
                english = word.english,
                spanish = word.spanish,
                phonetic = word.phonetic,
                description = word.description,
            )
        }
    }

    parsedFromGrammar.forEach { entry ->
        val key = entry.english.trim().lowercase()
        if (key.isNotBlank() && key !in merged) {
            merged[key] = entry
        }
    }

    return merged.values.toList()
}

@Composable
fun InlineVocabularyText(text: String, entries: List<VocabularyEntry>) {
    if (text.isBlank()) {
        Text("")
        return
    }

    if (entries.isEmpty()) {
        Text(text)
        return
    }

    val vocabColor = Color(0xFFB57A3E) // naranja grisaseo
    // Map keys to entries by lowercase english word
    val vocabMap = entries.associateBy { it.english.trim().lowercase() }

    // Build regex pattern from entries, longer first to avoid partial matches
    val wordsSorted = entries.map { Regex.escape(it.english.trim()) }.distinct().sortedByDescending { it.length }
    val pattern = if (wordsSorted.isEmpty()) null else "\\b(${wordsSorted.joinToString("|")})\\b"
    val regex = pattern?.let { Regex(it, RegexOption.IGNORE_CASE) }

    val annotated = if (regex == null) {
        AnnotatedString(text)
    } else {
        buildAnnotatedString {
            var lastIndex = 0
            for (m in regex.findAll(text)) {
                val start = m.range.first
                val end = m.range.last + 1
                if (start > lastIndex) append(text.substring(lastIndex, start))
                val matched = text.substring(start, end)
                val key = matched.lowercase()
                pushStringAnnotation(tag = "vocab", annotation = key)
                withStyle(style = SpanStyle(color = vocabColor, fontWeight = FontWeight.SemiBold)) {
                    append(matched)
                }
                pop()
                lastIndex = end
            }
            if (lastIndex < text.length) append(text.substring(lastIndex))
        }
    }

    var selected by remember { mutableStateOf<VocabularyEntry?>(null) }

    ClickableText(
        text = annotated,
        onClick = { offset ->
            val list = annotated.getStringAnnotations(tag = "vocab", start = offset, end = offset)
            if (list.isNotEmpty()) {
                val key = list.first().item
                selected = vocabMap[key]
            }
        },
        style = MaterialTheme.typography.bodyMedium
    )

    selected?.let { entry ->
        VocabInfoDialog(entry = entry, onDismiss = { selected = null })
    }
}

@Composable
fun VocabInfoDialog(entry: VocabularyEntry, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cerrar") }
        },
        title = { Text(entry.english, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Significado: ${entry.spanish}")
                entry.phonetic?.takeIf { it.isNotBlank() }?.let {
                    Text("Fonetica: $it", color = Color.Gray)
                }
                entry.description?.takeIf { it.isNotBlank() }?.let {
                    Text("Descripcion: $it", color = Color.Gray)
                }
            }
        }
    )
}

