package com.springernature.e2e

import com.springernature.kachtml.*
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import org.jooq.DSLContext
import java.time.ZonedDateTime
import java.util.*

val originalContent = """
<p data-index="0">Better Form Design: One Thing Per Page (Case Study)</p>
<p data-index="1">By Adam Silver, www.smashingmagazine.comView OriginalMay 22nd, 2017</p>
<p data-index="2">May 22nd, 2017</p>
<p data-index="3">In 2008, I worked on Boots.com. They wanted a single-page checkout with the trendiest of techniques from that era, including accordions, AJAX and client-side validation.</p>

<p data-index="4">Each step (delivery address, delivery options and credit-card details) had an accordion panel. Each panel was submitted via AJAX. Upon successful submission, the panel collapsed and the next one opened, with a sliding transition.</p>

<p data-index="5">It looked a little like this:</p>

<img data-index="6" src="/static/images/uploaded-image.png"/>
<p data-index="7">Boots' single-page checkout, using an accordion panel for each step. (View large version2)
<p data-index="8">Users struggled to complete their orders. Errors were hard to fix because users had to scroll up and down. And the accordion panels were painful and distracting. Inevitably, the client asked us to make changes.</p>

<p data-index="9">We redesigned it so that each panel became its own page, removing the need for accordions and AJAX. However, we kept the client-side validation to avoid an unnecessary trip to the server.</p>
"""

fun updateTitleForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val fragment = manuscript.title
    authorEditPage(manuscript, fragment.state, originalContent.reserve(manuscript.abstract.originalDocumentLocation), "title",
        htmlEditor("editable-title", fragment.markUp.raw, fragment.originalDocumentLocation, "title"))
}

fun updateAbstractForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val fragment = manuscript.abstract
    authorEditPage(manuscript, fragment.state, originalContent.reserve(manuscript.title.originalDocumentLocation), "abstract",
        htmlEditor("editable-abstract", fragment.markUp.raw, fragment.originalDocumentLocation, "abstract"))
}

private fun String.reserve(vararg reservationRanges: IntRange?): String =
    reservationRanges.asList()
        .filterNotNull()
        .fold(this,
            { acc, range ->
                range.fold(acc,
                    { acc2, index -> acc2.replace("data-index=\"$index\"", "data-index=\"$index\" data-already-used") })
            })

private fun htmlEditor(editorId: String, originalContent: String, originalContentSelection: IntRange?, fieldName: String) =
    div(cl("row responsive-margin bordered rounded"),
        div(id(editorId), cl("html-editor"), "contenteditable" attr "true", originalContent),
        input("type" attr "hidden", cl("input-backing-for-div"), "name" attr fieldName, "data-for" attr editorId),
        input("type" attr "hidden", "name" attr "selectionStart", "value" attr (originalContentSelection?.first?.toString() ?: "")),
        input("type" attr "hidden", "name" attr "selectionEnd", "value" attr (originalContentSelection?.last?.toString() ?: ""))
    )

private fun authorEditPage(manuscript: Manuscript, fragmentState: FragmentState, originalManuscript: String, currentForm: String, vararg formRows: KTag): Response {
    return htmlPage(manuscript.title.markUp, div(cl("row"),
        div(cl("col-lg-4"),
            div(id("content"), cl("full-screen-height")),
            div(id("original-content"), cl("hidden"), originalManuscript)
        ),
        div(cl("col-lg-4"),
            form("method" attr "POST",
                div(cl("row"),
                    select(cl("form-selector"),
                        "name" attr "formSelector",
                        option("value" attr "title", manuscript.title.state.asIcon + " Title", if (currentForm == "title") {
                            attr("selected")
                        } else {
                            ""
                        }),
                        option("value" attr "abstract", manuscript.abstract.state.asIcon + " Abstract", if (currentForm == "abstract") {
                            attr("selected")
                        } else {
                            ""
                        })),
                    button(id("formSelectorButton"), cl("hidden"), "name" attr "action", "value" attr "selected", "Go")),
                *formRows,
                div(cl("row"),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "previous", "Previous")
                    ),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "revert", "Revert")
                    ),
                    div(cl("col-lg-3 input-group"),
                        approvedCheckbox(fragmentState)
                    ),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "next", "Next")
                    )
                )
            )
        ),
        div(cl("col-lg-4"),
            p("typeset")
        )
    ))
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
                        form("method" attr "POST",
                            fieldset(
                                legend("Create a new article"),
                                formRow(label("for" attr "articleType", "Article Type"),
                                    select("id" attr "articleType",
                                        option("review"),
                                        option("obituary")
                                    )),
                                formRow(label("for" attr "uploadManuscript", "Upload Manuscript"),
                                    input("type" attr "file", "id" attr "uploadManuscript"),
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
                        a("href" attr "/editor/manuscript", "role" attr "button", "Manuscripts"))),
                div(cl("container"), content),
                footer(
                    div(cl("col-sm col-md-10 col-md-offset-1"),
                        p("Copyright &copy; SpringerNature ${ZonedDateTime.now().year}"))
                ),
                script(scripts)
            )))
}