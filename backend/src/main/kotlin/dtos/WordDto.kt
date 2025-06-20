package com.example.dtos

data class WordDTO(
    val id: Int,
    val english: String,
    val spanish: String,
    val phonetic: String?
)

data class PhraseDTO(
    val id: Int,
    val englishText: String,
    val spanishText: String
)

data class PhraseWithWordsDTO(
    val phrase: PhraseDTO,
    val words: List<WordDTO>
)

data class UpdatePhraseWordsRequest(
    val phraseId: Int,
    val wordIds: List<Int>
)