package com.springernature.e2e

import org.w3c.dom.Document
import org.w3c.dom.Node

val jatsTemplate = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE article PUBLIC "-//NLM//DTD JATS (Z39.96) Journal Publishing DTD v1.1d1 20130915//EN" "JATS-journalpublishing1.dtd">
<article
   article-type="research-article"
   dtd-version="1.1d1" xml:lang="en"
   xmlns:mml="http://www.w3.org/1998/Math/MathML"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 >
  <front>
    <journal-meta>
      <journal-id journal-id-type="pmc">ni</journal-id>
      <issn>0959-8138</issn>
      <publisher>
        <publisher-name>Macmillan</publisher-name>
      </publisher>
    </journal-meta>
    <article-meta>
      <title-group>
        <article-title>Title goes here</article-title>
      </title-group>
    </article-meta>
    <abstract>abstract goes here</abstract>
  </front>
  <body>
    body goes here
  </body>
  <back>
  </back>
</article>"""

/*
    <sec>
      <title>Next steps</title>
      <p>General practitioners do not behave in a uniform way. They can be categorised as slow, medium, and fast and react in different ways to changes in consulting speed.<xref ref-type="bibr" rid="B18">18</xref> They are likely to have differing views about a widespread move to lengthen consultation time. We do not need further confirmation that longer consultations are desirable and necessary, but research could show us the best way to learn how to introduce them with minimal disruption to the way in which patients and practices like primary care to be provided.<xref ref-type="bibr" rid="B24">24</xref> We also need to learn how to make the most of available time in complex consultations.</p>
      <p>Devising appropriate incentives and helping practices move beyond just reacting to demand in the traditional way by working harder and faster is perhaps our greatest challenge in the United Kingdom. The new primary are trusts need to work together with the growing primary care research networks to carry out the necessary development work. In particular, research is needed on how a primary care team can best provide the right balance of quick access and interpersonal knowledge and trust.</p>
    </sec>

 */

fun jatsFrom(manuscript: Manuscript): Document {
    val document = Xml.document(jatsTemplate)

    val title = Xml.document("<root>${manuscript.title.markUp.toXmlstring()}</root>").nodes("/root/child::node()")
    document.nodes("//article-title")[0].removeChildren().appendFragment(title)

    val abstract = Xml.document("<root>${manuscript.abstract.markUp.toXmlstring()}</root>").nodes("/root/child::node()")
    document.nodes("//abstract")[0].removeChildren().appendFragment(abstract)

    val content = Xml.document("<root>${manuscript.content.markUp.toXmlstring()}</root>").nodes("/root/child::node()")
    document.nodes("//body")[0].removeChildren().appendFragment(content)



    return document
}

fun MarkUp.toXmlstring(): String {
    return this.raw.replace(Regex("<br[^>]*>"), "")
}

private fun Node.appendFragment(fragment: List<Node>): Node {
    fragment.forEach({ node ->
        this.appendChild(
            this.ownerDocument.adoptNode(
                node.cloneNode(true)
            )
        )
    })
    return this
}

fun Node.removeChildren(): Node {
    val nodes = childNodes

    while (nodes.length > 0) {
        removeChild(nodes.item(0))
    }

    return this
}
