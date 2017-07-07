package com.springernature.e2e

import com.google.gson.Gson
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.graphdb.Relationship
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

    fun saveNode(graphDb: GraphDatabaseService): Node {
        val node = graphDb.createNode()
            .prop("markUp", markUp.raw)
            .prop("approved", approved)
        originalDocumentLocation?.let { range ->
            node
                .prop("startSelection", originalDocumentLocation.first)
                .prop("endSelection", originalDocumentLocation.last)
        }
        return node
    }
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
    val authors: Authors,
    val originalContent: MarkUp) {

    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(payload: String): Manuscript = Gson().fromJson<Manuscript>(payload, Manuscript::class.java)
        fun EMPTY(id: ManuscriptId) = Manuscript(id, MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null), MarkUpFragment(MarkUp(""), false, null), Authors(null, false), MarkUp(""))
    }

    fun saveNode(graphDb: GraphDatabaseService): Node {
        val existingNode = graphDb.findNode(ManuscriptTable.manuscriptLabel, "id", this.id.raw.toString())
        if (existingNode != null) {
            existingNode.getRelationships(ManuscriptTable.titleRelationship).deleteOtherEnd()
            existingNode.getRelationships(ManuscriptTable.abstractRelationship).deleteOtherEnd()
            existingNode.getRelationships(ManuscriptTable.contentRelationship).deleteOtherEnd()
            existingNode.delete()
        }
        val manuscript = graphDb.createNode(ManuscriptTable.manuscriptLabel)
            .prop("id", id.raw.toString())
            .prop("authors.approved", authors.approved).let {
            if (authors.originalDocumentLocation != null) {
                it.prop("authors.startSelection", authors.originalDocumentLocation.first)
                    .prop("authors.endSelection", authors.originalDocumentLocation.last)
            } else {
                it
            }
        }


        manuscript.createRelationshipTo(title.saveNode(graphDb), ManuscriptTable.titleRelationship)
        manuscript.createRelationshipTo(abstract.saveNode(graphDb), ManuscriptTable.abstractRelationship)
        manuscript.createRelationshipTo(content.saveNode(graphDb), ManuscriptTable.contentRelationship)
        return manuscript
    }

}

private fun Node.prop(name: String, value: Any?): Node {
    this.setProperty(name, value)
    return this
}

private fun MutableIterable<Relationship>.deleteOtherEnd() {
    this.forEach {
        it.endNode.delete()
        it.delete()
    }
}