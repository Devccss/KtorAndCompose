import com.example.config.DatabaseFactory
import com.example.dtos.PhraseDTO
import com.example.dtos.WordDTO
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq

import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertIgnore
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll


class PhraseWordRepository {
    // Añadir palabras a una frase
    suspend fun addWordsToPhrase(phraseId: Int, wordIds: List<Int>): Boolean = DatabaseFactory.dbQuery {
        wordIds.forEach { wordId ->
            PhraseWords.insertIgnore {
                it[PhraseWords.phraseId] = phraseId
                it[PhraseWords.wordId] = wordId
            }
        }
        true
    }

    // Eliminar palabras de una frase
    suspend fun removeWordsFromPhrase(phraseId: Int, wordIds: List<Int>): Unit = DatabaseFactory.dbQuery {
        PhraseWords.deleteWhere {
            (PhraseWords.phraseId eq phraseId) and
                    (wordId inList wordIds)
        } > 0
    }

    // Obtener todas las palabras de una frase
    suspend fun getWordsByPhrase(phraseId: Int): List<WordDTO> = DatabaseFactory.dbQuery {
        (Words innerJoin PhraseWords)
            .selectAll()
            .where { PhraseWords.phraseId eq phraseId }
            .map { row ->
                WordDTO(
                    id = row[Words.id].value,
                    english = row[Words.english],
                    spanish = row[Words.spanish],
                    phonetic = row[Words.phonetic]
                )
            }
    }

    // Obtener frases que contengan una palabra específica
    suspend fun getPhrasesByWord(wordId: Int): List<PhraseDTO> = DatabaseFactory.dbQuery {
        (Phrases innerJoin PhraseWords)
            .selectAll()
            .where{ PhraseWords.wordId eq wordId }
            .map { row ->
                PhraseDTO(
                    id = row[Phrases.id].value,
                    englishText = row[Phrases.englishText],
                    spanishText = row[Phrases.spanishText]
                )
            }
    }
}
