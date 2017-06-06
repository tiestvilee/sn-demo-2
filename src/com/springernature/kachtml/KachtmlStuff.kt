package com.springernature.kachtml

open class KAttribute(val name: String, val value: String) {
    fun asPair(): Pair<String, String> = Pair(name, value)
}

class Id(id: String) : KAttribute("id", id)

class Class(className: String) : KAttribute("class", className)

open class KTag(val tagName: String, vararg params: Any) {
    private val flattenedParams = params.fold(listOf<Any>(), { acc, item ->
        when (item) {
            is Collection<*> ->
                item.fold(acc, { flattened, subItem ->
                    if (subItem == null) {
                        flattened
                    } else {
                        flattened + subItem
                    }
                })
            else -> acc + item
        }
    })

    val attributes = flattenedParams
        .filter { it is KAttribute }
        .map { it as KAttribute }
        .fold(mapOf<String, String>(),
            { acc, attr ->
                acc + if (acc.containsKey(attr.name)) {
                    Pair(attr.name, (acc[attr.name] + " " + attr.value))
                } else {
                    attr.asPair()
                }
            })

    val content = flattenedParams
        .filter { it !is KAttribute }
        .map {
            when (it) {
                is String -> it
                is KTag -> it
                else -> throw IllegalArgumentException(
                    "Don't understand argument: ${it.javaClass.simpleName}, expecting one of String, KTag")
            }
        }

    override fun toString(): String {
        return "${javaClass.simpleName}$flattenedParams"
    }


}


object SimpleFormatter {
    fun toHtml(tag: KTag): String = toHtml(tag, "")
    fun toHtml(tag: KTag, indent: String): String {
        val innerText = innerText(tag, indent + "  ")
        return indent + "<${tag.tagName}${attributes(tag)}>$innerText${if (innerText.trim().isNotEmpty()) "\n" + indent else ""}</${tag.tagName}>"
    }

    private fun innerText(tag: KTag, indent: String): String =
        tag.content
            .map {
                "\n" + when (it) {
                    is String -> if (it.trim().isNotEmpty()) indent + it else ""
                    is KTag -> toHtml(it, indent)
                    else -> throw IllegalArgumentException("Don't understand item: " + it.javaClass.simpleName)
                }
            }
            .joinToString("")

    private fun attributes(tag: KTag): String {
        val attributes = tag.attributes
            .asSequence()
            .sortedBy { it.key }
            .joinToString(" ") {
                if (it.value.isBlank()) it.key else """${it.key}="${it.value}""""
            }
        return if (attributes.isBlank()) "" else " " + attributes
    }

}

object CompactFormatter {
    fun toHtml(tag: KTag): String =
        "<${tag.tagName}${attributes(tag)}>${innerText(tag)}</${tag.tagName}>"

    private fun innerText(tag: KTag): String =
        tag.content
            .map {
                when (it) {
                    is String -> it
                    is KTag -> toHtml(it)
                    else -> throw IllegalArgumentException("Don't understand item: " + it.javaClass.simpleName)
                }
            }
            .joinToString("")

    private fun attributes(tag: KTag): String {
        val attributes = tag.attributes
            .asSequence()
            .sortedBy { it.key }
            .joinToString(" ") {
                if (it.value.isBlank()) it.key else """${it.key}="${it.value}""""
            }
        return if (attributes.isBlank()) "" else " " + attributes
    }

}

fun KTag.toHtml(): String = SimpleFormatter.toHtml(this)
fun KTag.toCompactHtml(): String = CompactFormatter.toHtml(this)

