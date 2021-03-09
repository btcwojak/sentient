package com.spudg.sentient

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class RecordModel(
        var id: String,
        var score: Int,
        var time: String,
        var note: String
) {

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
                "id" to id,
                "score" to score,
                "time" to time,
                "note" to note
        )
    }

}
