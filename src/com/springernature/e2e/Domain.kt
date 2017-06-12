package com.springernature.e2e

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
}