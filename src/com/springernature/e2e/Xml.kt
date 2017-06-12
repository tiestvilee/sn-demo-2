package com.springernature.e2e

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.SAXException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.StringWriter
import java.io.UnsupportedEncodingException
import java.lang.String.format
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import javax.xml.transform.*
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

fun Node.nodes(query: String): List<Node> {
    val xpath = synchronized(Xml::class.java) { Xml.xpathFactory.newXPath() }
    val nodes = xpath.evaluate(query, this, XPathConstants.NODESET) as NodeList
    val result = kotlin.collections.mutableListOf<Node>()

    return (0..nodes.length - 1).mapTo(result) { nodes.item(it) }
}

fun Node.asString(): String {
    val writer = StringWriter()
    try {
        val trans: Transformer = synchronized(Xml::class.java) { Xml.transformFactory.newTransformer() }
        trans.setOutputProperty(OutputKeys.INDENT, "yes")
        trans.setOutputProperty(OutputKeys.VERSION, "1.0")
        if (this !is Document) {
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        }
        trans.transform(
            DOMSource(this),
            StreamResult(writer)
        )
    } catch (ex: TransformerConfigurationException) {
        throw IllegalStateException(ex)
    } catch (ex: TransformerException) {
        throw IllegalArgumentException(ex)
    }

    return writer.toString()
}


fun List<Node>.asString(): String =
    this.joinToString("") { it.asString() }

fun List<Node>.mergeUnder(rootNode: Node): Node {
    this.forEach { node ->
        rootNode.appendChild(node)
    }
    return rootNode
}

object Xml {
    internal val domFactory = DocumentBuilderFactory.newInstance()
    internal val xpathFactory = XPathFactory.newInstance()
    internal val transformFactory = TransformerFactory.newInstance()

    fun document(xml: String): Document =
        try {
            domFactory.isValidating = false
            domFactory.isNamespaceAware = true
            domFactory.setFeature("http://xml.org/sax/features/namespaces", false)
            domFactory.setFeature("http://xml.org/sax/features/validation", false)
            domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false)
            domFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)

            val documentBuilder = domFactory.newDocumentBuilder()
            documentBuilder.parse(
                ByteArrayInputStream(xml.toByteArray(charset("UTF-8")))
            )
        } catch (ex: UnsupportedEncodingException) {
            throw IllegalStateException(ex)
        } catch (ex: IOException) {
            throw IllegalStateException(ex)
        } catch (ex: ParserConfigurationException) {
            throw IllegalStateException(ex)
        } catch (ex: SAXException) {
            throw IllegalArgumentException(
                format("Invalid XML: \"%s\"", xml), ex
            )
        }

}