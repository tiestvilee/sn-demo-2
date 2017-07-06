package com.springernature.e2e

import com.springernature.e2e.ManuscriptTable.manuscriptId
import com.springernature.e2e.ManuscriptTable.manuscriptLabel
import com.springernature.kachtml.*
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import org.jooq.DSLContext
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.Node
import org.neo4j.helpers.collection.Iterators.loop
import java.io.File
import java.time.ZonedDateTime
import java.util.*

val originalContent = File("example.txt").readLines()
    .mapIndexed { index, line ->
        "<p data-index=\"$index\">$line</p>"
    }
    .joinToString("\n")

val oldOriginalContent = """
<p data-index="0">Epigenomic and functional analyses reveal roles of epialleles in the loss of photoperiod sensitivity during domestication of allotetraploid cottons</p>
<p data-index="1">Qingxin Song, Tianzhen Zhang <t.zhang@wherever.com>, David M. Stelly(1234-2345-3456-4567), Z. Jeffrey Chen</p>
<p data-index="2">Genome Biology201718:99</p>
<p data-index="3">DOI: 10.1186/s13059-017-1229-8©  The Author(s). 2017</p>
<p data-index="4">Received: 23 January 2017Accepted: 3 May 2017Published: 31 May 2017</p>
<p data-index="5">Abstract</p>
<p data-index="6">Background</p>
<p data-index="7">Polyploidy is a pervasive evolutionary feature of all flowering plants and some animals, leading to genetic and epigenetic changes that affect gene expression and morphology. DNA methylation changes can produce meiotically stable epialleles, which are transmissible through selection and breeding. However, the relationship between DNA methylation and polyploid plant domestication remains elusive.</p>
<p data-index="8">Results</p>
<p data-index="9">We report comprehensive epigenomic and functional analyses, including ~12 million differentially methylated cytosines in domesticated allotetraploid cottons and their tetraploid and diploid relatives. Methylated genes evolve faster than unmethylated genes; DNA methylation changes between homoeologous loci are associated with homoeolog-expression bias in the allotetraploids. Significantly, methylation changes induced in the interspecific hybrids are largely maintained in the allotetraploids. Among 519 differentially methylated genes identified between wild and cultivated cottons, some contribute to domestication traits, including flowering time and seed dormancy. CONSTANS (CO) and CO-LIKE (COL) genes regulate photoperiodicity in Arabidopsis. COL2 is an epiallele in allotetraploid cottons. COL2A is hypermethylated and silenced, while COL2D is repressed in wild cottons but highly expressed due to methylation loss in all domesticated cottons tested. Inhibiting DNA methylation activates COL2 expression, and repressing COL2 in cultivated cotton delays flowering.</p>
<p data-index="10">Conclusions</p>
<p data-index="11">We uncover epigenomic signatures of domestication traits during cotton evolution. Demethylation of COL2 increases its expression, inducing photoperiodic flowering, which could have contributed to the suitability of cotton for cultivation worldwide. These resources should facilitate epigenetic engineering, breeding, and improvement of polyploid crops.</p>
<p data-index="12">Keywords</p>
<p data-index="13">DNA methylation Epigenomics Cotton Photoperiod Hybrid Polyploidy Biotechnology</p>
<p data-index="14">Background</p>
<p data-index="15">Polyploidy or whole genome duplication (WGD) is a pervasive evolutionary feature of some animals and all flowering plants [1, 2], leading to genetic and epigenetic changes that affect gene expression and morphology [3, 4, 5]. Estimates indicate that two rounds of ancestral WGD occurred before the divergence of extant seed plants and angiosperms, giving rise to the diversification of genes and pathways important to seed and flower development and eventually the dominance of angiosperms on the earth [6, 7]. Many important crops including wheat, cotton, and canola are allopolyploids, which usually arise via fusion of 2n gametes between species or by interspecific hybridization followed by genome doubling [3, 8]. Genomic interactions in the polyploids can induce genetic and epigenetic changes including DNA methylation [1, 3]. DNA methylation changes can produce meiotically stable epialleles [9, 10] which are transmissible through natural selection and breeding. For example, stable DNA methylation in promoters can be inherited as epialleles, which confer symmetric flower development in Linaria vulgaris [11] and quantitative trait loci of colorless non-ripening and vitamin E content in tomato [12, 13]. In plants, DNA methylation occurs in CG, CHG, and CHH (H = A, T, or C) contexts through distinct pathways [14]. In Arabidopsis, maintenance methylation of CG and CHG is regulated by METHYLTRANSFERASE1 (MET1) and CHROMOMETHYLASE3 (CMT3), respectively [15, 16, 17]. De novo CHH methylation is established by RNA-directed DNA methylation (RdDM) and CMT2-mediated pathways [18, 19, 20]. DNA methylation is essential for maintaining animal and plant development. Methylation defects are embryonic lethal in animals [21], induce additional epigenetic changes during self-pollination in Arabidopsis [22], and lead to lethality in rice [23]. DNA methylation is also responsible for seed development [24] and adaptation to environments [25]. Furthermore, DNA methylation changes are associated with expression of homoeologous genes in resynthesized and natural Arabidopsis allotetraploids [26, 27, 28], natural Spartina allopolyploids [29], and paleopolyploid beans [30]. However, epigenomic resources in polyploids are very limited, and the functional role of epialleles in morphological evolution and crop domestication remains largely unknown.</p>
<p data-index="16">Cotton is the largest source of renewable textile fiber and an excellent model for studying polyploid evolution and crop domestication [31, 32]. Allotetraploid cotton was formed approximately 1– 1.5 million years ago (MYA) [33] by interspecific hybridization between two diploid species, one having the A genome like in Gossypium arboreum (Ga, A2) and Gossypium herbaceum (A1), and the other resembling the D5 genome found in extant species Gossypium raimondii (Gr); divergence of A-genome and D-genome ancestors is estimated at ~6 MYA (Fig. 1a). The allotetraploid diverged into five or more species [32, 34]. Two of them, Gossypium hirsutum (Gh, Upland cotton) and Gossypium barbadense (Gb, Pima cotton), were independently domesticated for higher fiber yield and wider geographical distribution; these characteristics were accompanied by extraordinary morphological changes including loss of photoperiod sensitivity, reduction in seed dormancy, and conversion from tree-like wild species to an annual crop [31, 33, 35].</p>
<img data-index="17" src="/static/images/fig1.gif"/>
<p data-index="18">Fig. 1</p>
<p data-index="19">Evolution of DNA methylation and genome sequence during polyploidization in cotton. a Allotetraploid cotton (AADD) was formed between A-genome species like G. arboreum (Ga) and D-genome species like G. raimondii (Gr), giving rise to five allotetraploid species: wild G. hirsutum (wGh), wild G. barbadense (wGb), G. tomentosum (Gt), G. darwinii (Gd), and G. mustelinum (Gm). Wild Gh and Gb are domesticated into cultivated G. hirsutum (cGh) and G. barbadense (cGb), respectively. b The number of differentially methylated cytosines in CG context (DmCG) in each pairwise comparison between different cotton species as shown in a. A2D5 is an interspecific hybrid between Ga (A2) and Gr (D5). Blue, green, black, and yellow brackets indicate comparisons of wild vs. wild, cultivated vs. wild allotetraploids, diploid vs. allotetraploid, and diploid parents vs. interspecific hybrid, respectively. c Phylogenetic tree was reconstructed based on genome-wide mCG divergent levels among cotton species. d, e Distribution of synonymous substitution values (Ks) (left) and gene-body DmCG percentages (right) of 6781 methylated orthologous genes (d) and 4063 unmethylated orthologous genes (e). As, Ds A subgenome and D subgenome in cultivated G. hirsutum, A G. arboreum, D G. raimondii. Peak values are indicated by arrows. The rate of methylation changes in each gene pair was estimated as the number of DmCG divided by the total number of CG in the gene body</p>
<p data-index="20">Here we generated single-base resolution methylomes of domesticated allotetraploid cottons and their wild tetraploid and diploid relatives. More than 12 million differentially methylated cytosines across all species were comparatively analyzed, which revealed different rates of DNA methylation and sequence changes and distinct methylation distributions in transposable elements (TEs) and genes. Integrating the data of methylomes with transcriptomes, we discovered more than 500 putative epialleles that may contribute to morphological and physiological changes during evolution and domestication of polyploid cottons. Genomic and functional analyses of an epiallele confirmed its role in photoperiodic flowering, contributing to wider geographical distribution of cotton. We propose that epigenomic resources can be used to improve crop production by epigenetic engineering and breeding.</p>
<p data-index="21">Results</p>
<p data-index="22">Different rates between DNA methylation changes and sequence evolution</p>
<p data-index="23">To uncover DNA methylation changes during cotton evolution and domestication, we generated single-base resolution methylomes from diploid G. arboreum (A2), diploid G. raimondii (D5), their interspecific hybrid (A2D5), wild allotetraploid G. hirsutum (wGh), wild allotetraploid G. barbadense (wGb), allotetraploid G. tomentosum (Gt), allotetraploid G. mustelinum (Gm), allotetraploid G. darwinii (Gd), cultivated allotetraploid G. hirsutum (cGh), and cultivated allotetraploid G. barbadense (cGb) (Fig. 1a; Additional file 1: Table S1). To exclude the effect of nucleotide variation across species (especially between C and T) on DNA methylation analysis, we identified 352,667,453 conserved cytosines (~48% of the total cytosines of the genome) between all species and present in two biological replicates for further analysis (Additional file 2: Figure S1). Among them, 12,045,718 (~3.4% of) differentially methylated cytosines (DmCs) were found across all species; there were more DmCs between diploid cottons and tetraploid cottons (diploid vs. tetraploid) than for other comparisons (diploid vs. diploid cottons, wild tetraploid vs. wild tetraploid, and wild vs. cultivated cottons) (Fig. 1b).</p>
<p data-index="24">Methylation divergent levels at CG and non-CG sites, respectively, that were conserved among all species (Additional file 2: Figure S1) were used to generate neighbor-joining phylogenetic trees. Phylogenetic trees with CG and non-CG sites recapitulated the known evolutionary relationships of cotton species [32], including sister taxa relationships between G. hirsutum and G. tomentosum and between G. barbadense and G. darwinii (Fig. 1c; Additional file 2: Figure S2). This suggests concerted evolution between DNA sequence and methylation changes. Gene-body methylated genes occur largely in CG sites [36] and evolve slowly [37]. To test the relationship between methylation and sequence evolution in genic regions, we divided orthologous genes into CG body-methylated (P CG &lt; 0.05) and CG body-unmethylated genes (P CG > 0.95) using a binomial test with body-methylation levels [37]. To reduce the effect of non-CG methylation on CG methylation analysis, CHG or CHH body-methylated orthologous genes (P CHG &lt; 0.05 or P CHH &lt; 0.05) were removed. Among CG body-methylated genes, the percentage of CG methylation changes (peaks at 0.18–0.24) was substantially higher than the substitution rate of coding sequence (Ks value peaks at 0.007–0.034) (Fig. 1d), suggesting that the methylation change rate is faster than the neutral sequence substitution rate. In the CG body-unmethylated genes, although the sequence variation remained at a similar level, the methylation peak disappeared (Fig. 1e).</p>
<p data-index="25">DNA methylation divergence between progenitor-like diploid species</p>
<p data-index="26">TEs are often associated with DNA methylation and genome complexity [14, 38, 39]. In diploid species, the G. arboreum genome is twofold larger and contains more TEs than the G. raimondii genome, probably because of TE expansion in the centromeric and peri-centromeric regions [40, 41] (Additional file 2: Figure S3a). However, in genic regions there were more DNA TEs and especially retrotransposons in G. raimondii than in G. arboreum (Fig. 2a). For retrotransposons, G. raimondii had more Copia and unclassified long terminal repeats (LTRs) in flanking sequences of the gene body than G. arboreum (Fig. 2b). Terminal repeat retrotransposons in miniature (TRIMs), which are enriched near genes [42, 43], showed similar distribution patterns between G. raimondii and G. arboreum (Fig. 2b). Because of high CG methylation levels in the TEs, G. raimondii homoeologs were generally more methylated than G. arboreum homoeologs (Fig. 2c; Additional file 2: Figure S3b).</p>
<img data-index="27" src="/static/images/fig2.gif"/>
<p data-index="28">Fig. 2</p>
<p data-index="29">Asymmetrical distribution of TE and DNA methylation in A and D genomes. a Distribution of class I and II TEs in genic regions of G. arboreum (Ga, blue for I and gray for II) and G. raimondii (Gr, orange for I and yellow for II). b Density differences (between G. raimondii and G. arboreum) of Gypsy (orange), Copia (blue), TRIM (terminal repeat retrotransposons in miniature, green), and other long terminal repeats (LTR, gray) in the genic regions. c, d CG (c) and CHG (d) methylation levels in the genic regions of G. arboreum and G. raimondii. e Average CG methylation differences between intergenic and intragenic TEs. f Average CHG methylation differences between intergenic and intragenic TEs</p>
<p data-index="30">Although TEs were also associated with high CHG methylation levels (Additional file 2: Figure S3b), CHG methylation levels in the gene body were similar between G. raimondii and G. arboreum (Fig. 2d). CHG methylation correlates positively with the repressive histone mark H3K9 methylation and negatively with gene expression [19, 39]. We speculate that TEs inserted in the gene body (intragenic TEs) could gradually lose CHG methylation during evolution to prevent silencing. Consistent with the hypothesis, CHG methylation levels were lower in the intragenic TEs than in the intergenic TEs; reduction in CHG methylation (27–49%) is higher than decrease in CG methylation (~12%) (Fig. 2e, f). CHG methylation levels of intragenic TEs were decreased to lower levels in G. raimondii than in G. arboreum (Fig. 2e, f). As a result, CHG methylation levels in the gene body were similar between them, although G. raimondii has more TEs in the gene body than G. arboreum (Fig. 2b, d).</p>
<p data-index="31">Hybrid-induced DNA methylation changes are conserved in polyploids</p>
<p data-index="32">Methods</p>
<p data-index="33">Plant materials</p>
<p data-index="34">G. raimondii (Gr, D5-3), G. arboreum (Ga, A2), interspecific hybrid (A2D5) between G. arboreum and G. raimondii, wild G. hirsutum (wGh, TX2095), wild G. barbadense (wGb, Gb-706), G. tomentosum (Gt, AD3-30), G. mustelinum (Gm, AD4-11), G. darwinii (Gd, AD5-31), cultivated G. hirsutum (cGh, TM-1), and cultivated G. barbadense (cGd, Pima-S6) were grown in the greenhouses at College Station and Austin, Texas. Leaves of each genotype were harvested with three biological replications for MethylC-seq and RNA-seq libraries. DNA methylation and expression levels of GhCOL2_D were further analyzed in five wild G. hirsutum accessions (TX701, TX1039, TX2092, TX2095, and TX2096), five cultivated G. hirsutum (TM-1, SA-308, SA-508, SA-528, and SA-1475), four wild G. barbadense (Gb-472, Gb-470, Gb-617, Gb-716), and four cultivated G. barbadense (Pima S2, Pima S6, Phytogen 800, 3-79), which were grown in the greenhouse under the light/dark (L/D) cycle of 16 h/L at 24 °C and 8 h/D at 20 °C. To exclude effects of development stage and circadian rhythm on gene expression change, the first true leaves at 16 days after sowing were harvested at ZT15 (zeitgeber time, ZT0 = dawn, 6 am) for DNA and RNA extraction.</p>
<p data-index="35">mRNA-seq library construction</p>
<p data-index="36">After DNase treatment, total RNA (~1 μg) was subjected to construct strand-specific mRNA-seq libraries with two biological replications using NEBNext® Ultra™ Directional RNA Library Prep Kit (New England Bioloabs (NEB), Ipswich, MA, USA) according to the manufacturer’s instructions. For each genotype, mRNA-seq libraries were constructed with two biological replicates and were paired-end sequenced for 126 cycles.</p>
<p data-index="37">MethylC-seq library construction</p>
<p data-index="38">Total genomic DNA (~5 μg) was fragmented to 100–1000 bp using Bioruptor (Diagenode, Denville, NJ, USA). End repair (NEBNext® End Repair Module) was performed on the DNA fragments followed by adding an ”A” base to the 3′ end (NEBNext® dA-Tailing Module), and the resulting DNA fragments were ligated to the methylated DNA adapter (NEXTflex™ DNA Barcodes, Bioo Scientific, Austin, TX, USA). The adapter-ligated DNA of 200–400 bp was purified using AMPure beads (Beckman Coulter, Brea, CA, USA), followed by sodium bisulfite conversion using the MethylCode™ Bisulfite Conversion Kit (Life Technologies, Foster City, CA, USA). The bisulfite-converted DNA was amplified by 12 cycles of PCR using LongAmp® Taq DNA Polymerase (NEB) and subject to purification using AMPure beads (Beckman Coulter). For each genotype, MethylC-seq libraries were constructed with two biological replicates and paired-end sequenced for 126 cycles.</p>
<p data-index="39">qRT-PCR</p>
<p data-index="40">After DNase treatment, total RNA (2 μg) was used to produce cDNA with the Omniscript RT Kit (Qiagen, Valencia, CA, USA). The cDNA was used as the template for qRT-PCR using FastStart Universal SYBR Green Master (Roche, Indianapolis, IN, USA). The reaction was run on the LightCycler® 96 System (Roche, Pleasanton, CA, USA). The relative expression level of a gene was quantified using the expression value of cotton GhUBQ10 as an internal control using the primers (Additional file 10: Table S9).</p>
"""

fun logFor(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val body = dataContext.select(TransactionLog.transactionId, TransactionLog.transactionType, TransactionLog.payload)
        .from(TransactionLog.transactionLogTable)
        .where(manuscriptId.eq(id.raw))
        .fetchMany().fold("",
        { acc, result ->
            result.fold(acc,
                { acc2, record ->
                    "$acc2  ==========> ${record.getValue(0)} - ${record.getValue(1)}\n${record.getValue(2)}\n"
                })
        })

    Response(Status.OK).header("Content-Type", "text/plain; charset=utf-8")
        .body(body)
}

fun graphFor(graphDb: GraphDatabaseService): HttpHandler = { request ->
    val id = request.path("id")
    val body = StringBuilder("Graph!$id\n")

    graphDb.findNodes(manuscriptLabel, "id", id).use {
        for (node in loop(it)) {
            outputNode(body, node, "", mutableSetOf())
        }
    }

    Response(Status.OK).header("Content-Type", "text/plain; charset=utf-8")
        .body(body.toString())
}

fun outputNode(builder: StringBuilder, node: Node, indent: String, visitedNodes: MutableSet<Long>) {
    visitedNodes.add(node.id)
    builder.append(indent).append("NODE: ").append(node.id).append(" (" + node.labels.joinToString(", ")).append(")\n")
    node.allProperties.forEach { name, value ->
        builder.append(indent).append(name).append(" -> ").append(value.toString().trimTo(60)).append("\n")
    }
    node.getRelationships().forEach { relationship ->
        if (!visitedNodes.contains(relationship.endNodeId)) {
            builder.append(indent).append("RELATION: ").append(relationship.type.name()).append("\n")
            outputNode(builder, relationship.endNode, indent + "  ", visitedNodes)
        }
    }
}

private fun String.trimTo(length: Int): String = if (this.trim().length >= length) this.trim().substring(0, length - 3) + "..." else this.trim()

fun asXml(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    Response(Status.OK).header("Content-Type", "application/xml; charset=utf-8")
        .body(jatsFrom(manuscript).asString())
}

fun asPdf(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    Response(Status.OK).header("Content-Type", "application/pdf; charset=utf-8")
        .body(org.http4k.core.Body(pdfFrom(manuscript)))
}

fun updateTitleForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val fragment = manuscript.title
    authorEditPage(manuscript, fragment.state, originalContent.reserve(manuscript.title, manuscript), typeSet(manuscript), "title",
        htmlEditor("editable-title", fragment.markUp.raw, fragment.originalDocumentLocation, "title"))
}

fun updateAbstractForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val fragment = manuscript.abstract
    authorEditPage(manuscript, fragment.state, originalContent.reserve(manuscript.abstract, manuscript), typeSet(manuscript), "abstract",
        htmlEditor("editable-abstract", fragment.markUp.raw, fragment.originalDocumentLocation, "abstract"))
}

fun updateAuthorsForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val authors = manuscript.authors
    val authorSelection = originalContent.lines().filter({ line ->
        Regex("data-index=\"([^\"]*)\"").find(line)?.groupValues?.get(1)
            ?.let { index -> authors.originalDocumentLocation?.contains(Integer.parseInt(index)) }
            ?: false
    }
    ).joinToString()
    authorEditPage(manuscript, authors.state, originalContent.reserve(manuscript.authors, manuscript), typeSet(manuscript), "authors",
        selectionDisplayer("readonly-authors", authorSelection, authors.originalDocumentLocation, "authors"))
}

private fun String.reserve(unreservedRange: FragmentOriginalDocumentLocation, manuscript: Manuscript): String {
    val reservedRange = listOf(manuscript.title.originalDocumentLocation, manuscript.abstract.originalDocumentLocation, manuscript.authors.originalDocumentLocation)
        .filterNotNull()
        .filter { range -> range != unreservedRange.originalDocumentLocation }
    return reservedRange
        .fold(this,
            { acc, range ->
                range.fold(acc,
                    { acc2, index -> acc2.replace("data-index=\"$index\"", "data-index=\"$index\" data-already-used") })
            })
}

private fun htmlEditor(editorId: String, originalContent: String, originalContentSelection: IntRange?, fieldName: String) =
        htmlEditor(editorId, originalContent, originalContentSelection, fieldName, true)

private fun selectionDisplayer(editorId: String, originalContent: String, originalContentSelection: IntRange?, fieldName: String) =
        htmlEditor(editorId, originalContent, originalContentSelection, fieldName, false)

private fun htmlEditor(editorId: String, originalContent: String, originalContentSelection: IntRange?, fieldName: String, editable: Boolean) =
        div(cl("row responsive-margin bordered rounded"),
                div(id(editorId), cl("html-editor with-scrollbars hack-height"), "contenteditable" attr editable.toString(), originalContent),
                input("type" attr "hidden", cl("input-backing-for-div"), "name" attr fieldName, "data-for" attr editorId),
                input("type" attr "hidden", "name" attr "selectionStart", "value" attr (originalContentSelection?.first?.toString() ?: "")),
                input("type" attr "hidden", "name" attr "selectionEnd", "value" attr (originalContentSelection?.last?.toString() ?: ""))
        )

private fun authorEditPage(manuscript: Manuscript, fragmentState: FragmentState, originalManuscript: String, typesetManuscript: KTag, currentForm: String, vararg formRows: KTag): Response {
    return htmlPage(manuscript.title.markUp, div(cl("row"),
        div(cl("col-lg-4"),
            div(id("content"), cl("full-screen-height with-scrollbars")),
            div(id("original-content"), cl("hidden"), originalManuscript)
        ),
        div(cl("col-lg-4 full-screen-height"),
            form("method" attr "POST",
                div(cl("row"),
                    select(cl("form-selector"), "name" attr "formSelector",
                        fragmentOption(currentForm, "title", "Title", manuscript.title),
                        fragmentOption(currentForm, "abstract", "Abstract", manuscript.abstract),
                        fragmentOption(currentForm, "authors", "Authors", manuscript.authors)
                    ),
                    button(id("formSelectorButton"), cl("hidden"), "name" attr "action", "value" attr "selected", "Go")),
                *formRows,
                div(cl("row"),
                    div(cl("col-lg-3"),
                            button("action", "previous", "Save and Previous")
                    ),
                    div(cl("col-lg-3"),
                            button("action", "revert", "Revert"),
                            button("action", "submit", "Save")
                    ),
                    div(cl("col-lg-3 input-group"),
                        approvedCheckbox(fragmentState)
                    ),
                    div(cl("col-lg-3"),
                            button("action", "next", "Save and Next")
                    )
                )
            )
        ),
        div(cl("col-lg-4 full-screen-height with-scrollbars"),
            typesetManuscript
        )
    ))
}

private fun button(name: String, value: String, contents: String) = button("name" attr name, "value" attr value, contents)

private fun fragmentOption(currentForm: String, name: String, title: String, fragment: FragmentWithState): Option {
    return option("value" attr name, fragment.state.asIcon + " " + title,
        if (currentForm == name) {
            attr("selected")
        } else {
            ""
        })
}

private fun approvedCheckbox(fragmentState: FragmentState): List<KTag> {
    return listOf(
        input(id("approved"),
            "type" attr "checkbox",
            "name" attr "approved",
            if (fragmentState == FragmentState.approved) listOf("checked" attr "checked") else listOf(),
            "tabindex" attr "0"),
        label("for" attr "approved", "approved")
    )
}

enum class FragmentState(val asIcon: String) {

    invalid("❌"), valid("🔀"), approved("✅");
}

fun redirectToTitle(): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))
    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/title")

}

fun createArticleForm(): HttpHandler {
    return { request ->
        htmlPage(MarkUp("submission"),
            div(cl("row"),
                div(cl("col-md-4")),
                div(cl("col-md-4"),
                    div(cl("fluid card"),
                        div(cl("section"), h3("Welcome to Nature Immunology")),
                        form("method" attr "POST", "action" attr "", "enctype" attr "multipart/form-data",
                            fieldset(
                                legend("Create a new article"),
                                formRow(label("for" attr "articleType", "Article Type"),
                                    select("id" attr "articleType", "name" attr "articleType",
                                        option("review"),
                                        option("obituary")
                                    )),
                                formRow(label("for" attr "uploadManuscript", "Upload Manuscript"),
                                    input("type" attr "file", "name" attr "uploadManuscript", "id" attr "uploadManuscript"),
                                    label("for" attr "uploadManuscript", cl("button"), "Upload")
                                ),
                                formRow(span(), button("Create", "type" attr "submit"))
                            )
                        )
                    )))
        )
    }
}

private fun formRow(label: KTag, vararg input: KTag): Div {
    return div(cl("row responsive-label"),
        div(cl("col-md-5"), label),
        div(cl("col-md"), *input))
}

val styles = """
.full-screen-height {
    height:calc(100vh - 140px);
}
.hack-height {
    max-height:calc(100vh - 320px);
}
.with-scrollbars {
    overflow-y:scroll;
}
.form-selector {
    width: 100%;
    font-size: 24pt;
}
.html-editor {
    width: 100%;
}

.selected {
	background: #FFAAAA;
}
[data-already-used] {
	-moz-user-select: none; -webkit-user-select: none; -ms-user-select:none; user-select:none;-o-user-select:none;
	background: #AAAAAA;
}

.typeset .title {
    align: center;
}

.typeset .abstract {
    align: center;
}
"""

val scripts = """

function copyInputBackedDivsOnFormSubmit() {
    for (form of Array.from(document.querySelectorAll("form"))) {
        form.addEventListener("submit", function(e) {
            for (hiddenInput of Array.from(form.querySelectorAll(".input-backing-for-div"))) {
                hiddenInput.value = form.querySelector("#" + hiddenInput.attributes["data-for"].value).innerHTML;
            }
        });
    }
}

function moveDirectlyToFormOnDropDownSelection() {
    var select = document.querySelector("select[name=formSelector]");
    select.addEventListener("change", function(e) {
        document.querySelector("#formSelectorButton").click();
    });
}


function resetToOriginalManuscript(from, to) {
	to.innerHTML = "";
	var children = from.childNodes;

	for (var i = 0; i < children.length; i++) {
		to.appendChild(children[i].cloneNode(true));
	}
}

function getStartAndEndBlocksFromSelection(doThis) {
	function findParentDataIndex(start) {
		while(start != null && !(start.attributes && start.attributes.hasOwnProperty("data-index"))) {
			start = start.parentElement
		}
		return (start && start.attributes && start.attributes["data-index"]) ? start.attributes["data-index"].value : null;
	}

	var selection = document.getSelection();
	if(selection.isCollapsed) {
		return;
	}

	var range = selection.getRangeAt(0);

	var startIndex = findParentDataIndex(range.startContainer)
	var endIndex = findParentDataIndex(range.endContainer)

	if(startIndex == null || endIndex == null) {
		return;
	}

	if(range.endOffset === 0) {
		endIndex -= 1;
	}

	return doThis(startIndex, endIndex);
}

function updateUiWithSelection(originalContent, content, current, selectionStart, selectionEnd) {
	return function (startIndex, endIndex) {

			resetToOriginalManuscript(originalContent, content)

			start = content.querySelector("[data-index='" + startIndex + "']")

			var newNode = document.createElement('div')
			newNode.className = "selected"
			current.innerHTML = "";

			start.parentElement.insertBefore(newNode, start)
			var childers = start.parentElement.childNodes;
			var copying = false;
			for (var i = 0; i < childers.length; i++) {
				var index = (childers[i].attributes && childers[i].attributes["data-index"] ? childers[i].attributes["data-index"].value : null);
				if (index==startIndex) {
					copying = true;
				}
				if(copying) {
					if(childers[i].attributes && childers[i].attributes.hasOwnProperty("data-already-used")) {
						resetToOriginalManuscript(originalContent, content);
						return;
					}
					current.appendChild(childers[i].cloneNode(true))
					newNode.appendChild(childers[i])
				}
				if (index==endIndex) {
					break;
				}
			}
            selectionStart.value = startIndex;
            selectionEnd.value = endIndex;
			content.attributes["data-dirty"] = "true";
		}
}

function copyContentSelectionBlockToForm() {
	var content = document.querySelector("#content");
	var originalContent = document.querySelector("#original-content");
	var current = document.querySelector(".html-editor");
    var selectionStart = document.querySelector("input[name='selectionStart']");
    var selectionEnd = document.querySelector("input[name='selectionEnd']");

	resetToOriginalManuscript(originalContent, content)

	function selectCurrentBlock() {
		getStartAndEndBlocksFromSelection(
			updateUiWithSelection(originalContent, content, current, selectionStart, selectionEnd));
		document.getSelection().collapse();
	}

	var oldTimeout = null;
	content.addEventListener("mouseup", function(e) {
		if(oldTimeout) clearTimeout(oldTimeout);
		oldTimeout = setTimeout(selectCurrentBlock, 100);
		return true;
	})
};

function contentLoaded() {
    copyInputBackedDivsOnFormSubmit();
    moveDirectlyToFormOnDropDownSelection();
    copyContentSelectionBlockToForm();
}

if (document.readyState === "complete" || (document.readyState !== "loading" && !document.documentElement.doScroll)) {
  contentLoaded();
} else {
  document.addEventListener("DOMContentLoaded", contentLoaded);
}
"""

private fun htmlPage(title: MarkUp, content: KTag): Response {
    return Response(Status.OK).header("Content-Type", "${ContentType.TEXT_HTML.value}; charset=utf-8").body(
        page(title, content).toCompactHtml()
    )
}

private fun page(title: MarkUp, content: KTag): KTag {
    return doctype(attr("html"),
        html(
            head(
                title(title.raw),
                link("rel" attr "stylesheet", "href" attr "https://gitcdn.link/repo/Chalarangelo/mini.css/master/dist/mini-default.min.css"),
                style(styles)
            ),
            body(
                header(cl("sticky row"),
                    div(cl("col-sm col-md-10, col-md-offset-1"),
                        a("href" attr "/editor/manuscriptTable", "role" attr "button", "Manuscripts"),
                        a("href" attr "asXml", "role" attr "button", "As XML"),
                        a("href" attr "asPdf", "role" attr "button", "As PDF")
                    )),
                div(cl("container"), content),
                footer(
                    div(cl("col-sm col-md-10 col-md-offset-1"),
                        p("Copyright &copy; SpringerNature ${ZonedDateTime.now().year}"))
                ),
                script(scripts)
            )))
}


fun typeSet(manuscript: Manuscript) =
    div(cl("typeset"),
        h1(cl("title"), manuscript.title.markUp.raw),
        h2(cl("abstract"), "Abstract"),
        div(cl("abstract"), manuscript.abstract.markUp.raw),
        hr(),
        manuscript.content.markUp.raw
    )
