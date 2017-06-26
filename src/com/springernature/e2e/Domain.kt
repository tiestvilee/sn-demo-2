package com.springernature.e2e

import com.google.gson.Gson
import java.util.*

data class MarkUp(override val raw: String) : HasExternalForm<String>
data class ManuscriptId(override val raw: UUID) : HasExternalForm<UUID>

data class MarkUpFragment(val markUp: MarkUp, val approved: Boolean, val originalDocumentLocation: IntRange?) {
    val state: FragmentState
        get() = if (markUp.raw.isNotEmpty()) {
            if (approved) {
                FragmentState.approved
            } else {
                FragmentState.valid
            }
        } else {
            FragmentState.invalid
        }
    val valid: Boolean
        get() = !markUp.raw.isNullOrBlank()
}

data class Manuscript(val id: ManuscriptId, val title: MarkUpFragment, val abstract: MarkUpFragment, val content: MarkUpFragment) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(payload: String): Manuscript = Gson().fromJson<Manuscript>(payload, Manuscript::class.java)
        fun EMPTY(id: ManuscriptId) = Manuscript(id, MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null))
    }
}