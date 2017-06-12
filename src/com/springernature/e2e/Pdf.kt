package com.springernature.e2e

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun pdfFrom(manuscript: Manuscript): ByteBuffer {
    val doc = PDDocument()
    val a4 = PDRectangle.A4
    var page = PDPage(a4)
    doc.addPage(page)

    val font = PDType1Font.HELVETICA
    val boldFont = PDType1Font.HELVETICA_BOLD

    var contents = PDPageContentStream(doc, page)

    val bounds = PDRectangle(50f, 50f, a4.width - 100, a4.height - 100)

    var offset = paragraph(contents, boldFont, 18f, manuscript.title.markUp.raw, bounds)

    val abstract = Xml.document("<root>${manuscript.abstract.markUp.toXmlstring()}</root>").nodes("/root/child::node()")

    abstract.forEach({node ->
        bounds.upperRightY = offset
        offset = paragraph(contents, font, 12f, node.textContent, bounds)
        if(offset < bounds.lowerLeftY) {
            offset = a4.height - 100 + 50
            contents.close()
            page = PDPage(a4)
            doc.addPage(page)
            contents = PDPageContentStream(doc, page)
        }
    })

    val content = Xml.document("<root>${manuscript.content.markUp.toXmlstring()}</root>").nodes("/root/child::node()")

    content.forEach({node ->
        bounds.upperRightY = offset
        offset = paragraph(contents, font, 12f, node.textContent, bounds)
        if(offset < bounds.lowerLeftY) {
            offset = a4.height - 100 + 50
            contents.close()
            page = PDPage(a4)
            doc.addPage(page)
            contents = PDPageContentStream(doc, page)
        }
    })

    bounds.upperRightY = offset
    offset = paragraph(contents, font, 12f, manuscript.content.markUp.raw, bounds)

    contents.close()


    val bytes = ByteArrayOutputStream()
    doc.save(bytes)
    doc.close()

    return ByteBuffer.wrap(bytes.toByteArray())
}

private fun paragraph(contents: PDPageContentStream, font: PDType1Font, fontSize: Float, markUp: String, bounds: PDRectangle): Float {
    val leading = fontSize * 1.5f
    var offset = bounds.upperRightY - leading
    contents.beginText()
    contents.setFont(font, fontSize)
    contents.newLineAtOffset(bounds.lowerLeftX, bounds.upperRightY)

    var remainingString = markUp.trim()
        .replace('\r', ' ').replace('\n', ' ')
        .replace('\u2009', '?')
        .replace('\u03BC', '?')
        .replace('\u2032', '?')
    while (remainingString.isNotEmpty()) {
        var end = remainingString.length
        var stepSize: Int = end / 2
        while (stepSize > 0 && end <= remainingString.length) {
            val stringWidth = stringWidth(font, remainingString.substring(0, end), fontSize)
            if (stringWidth > bounds.width) {
                end -= stepSize
            } else if (stringWidth < bounds.width) {
                end += stepSize
            } else {
                break
            }
            stepSize /= 2
        }
        if (end < remainingString.length) {
            if (!remainingString[end].isWhitespace()) {
                while (!remainingString[end - 1].isWhitespace()) {
                    end -= 1
                }
            }
        } else {
            end = remainingString.length
        }


        println("one line = ${remainingString.substring(0, end)}")
        println("offset = ${offset}")
        contents.newLineAtOffset(0f, -leading)
        offset -= leading
        contents.showText(remainingString.substring(0, end).trim())

        remainingString = remainingString.substring(end).trim()
    }

    contents.endText()

    return offset
}

private fun stringWidth(font: PDType1Font, remainingString: String, fontSize: Float) =
    font.getStringWidth(remainingString) / 1000.0 * fontSize