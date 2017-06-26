package com.springernature.e2e

import com.google.gson.Gson
import java.util.*

data class MarkUp(override val raw: String) : HasExternalForm<String>
data class ManuscriptId(override val raw: UUID) : HasExternalForm<UUID>

interface FragmentWithState {
    val state: FragmentState
}

interface FragmentOriginalDocumentLocation {
    val originalDocumentLocation: IntRange?
}

data class MarkUpFragment(val markUp: MarkUp, val approved: Boolean, override val originalDocumentLocation: IntRange?) : FragmentWithState, FragmentOriginalDocumentLocation {
    override val state: FragmentState
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

data class Authors(override val originalDocumentLocation: IntRange?, val approved: Boolean) : FragmentWithState, FragmentOriginalDocumentLocation {
    override val state: FragmentState
        get() = if (approved) {
            FragmentState.approved
        } else {
            FragmentState.valid
        }
}

data class Manuscript(
    val id: ManuscriptId,
    val title: MarkUpFragment,
    val abstract: MarkUpFragment,
    val content: MarkUpFragment,
    val authors: Authors) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(payload: String): Manuscript = Gson().fromJson<Manuscript>(payload, Manuscript::class.java)
        fun EMPTY(id: ManuscriptId) = Manuscript(id, MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null), Authors(null, false))
    }
}