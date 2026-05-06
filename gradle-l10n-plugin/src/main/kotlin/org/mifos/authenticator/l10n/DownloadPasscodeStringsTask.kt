/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.l10n

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/**
 * Fetches `l10n-templates/passcode/values-XX/strings.xml` from the mifos-passcode-cmp repo
 * (pinned to `v$libraryVersion` by default) and merges the `<string>` entries into the
 * consumer's matching `composeResources/values-XX/strings.xml`. Existing keys in the
 * consumer's file are preserved unchanged; new keys are appended; the result is rewritten
 * with a canonical 4-space indentation so subsequent runs produce zero diff.
 */
abstract class DownloadPasscodeStringsTask : DefaultTask() {

    @get:Input
    abstract val locales: ListProperty<String>

    @get:Input
    abstract val libraryVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val branch: Property<String>

    @get:Input
    abstract val repoOwner: Property<String>

    @get:Input
    abstract val repoName: Property<String>

    @get:Internal
    abstract val targetModule: DirectoryProperty

    init {
        group = "mifos l10n"
        description = "Downloads passcode-library translations for the configured locales " +
            "into <targetModule>/src/commonMain/composeResources/values-XX/strings.xml"
    }

    @TaskAction
    fun execute() {
        val ref = branch.orNull?.takeIf { it.isNotBlank() } ?: "v${libraryVersion.get()}"
        val baseUrl = "https://raw.githubusercontent.com/${repoOwner.get()}/${repoName.get()}/$ref/l10n-templates/passcode"
        val targetRoot = targetModule.get().asFile.resolve("src/commonMain/composeResources")

        val client = HttpClient.newHttpClient()
        var totalAdded = 0
        var totalPreserved = 0
        val unsupportedLocales = mutableListOf<String>()
        val processedLocales = mutableListOf<String>()

        for (locale in locales.get()) {
            val folderName = if (locale == "default" || locale.isEmpty()) "values" else "values-$locale"
            val url = "$baseUrl/$folderName/strings.xml"

            logger.lifecycle("Fetching $url")
            val request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build()
            val response = try {
                client.send(request, HttpResponse.BodyHandlers.ofString())
            } catch (e: Exception) {
                logger.error("Network error fetching $url: ${e.message}; skipping locale '$locale'")
                continue
            }

            when (response.statusCode()) {
                200 -> { /* proceed below */ }
                404 -> {
                    logger.warn(
                        "Locale '$locale' is not in the template set at ref '$ref'. " +
                            "Create $folderName/strings.xml manually using " +
                            "https://github.com/openMF/mifos-passcode-cmp/blob/$ref/l10n-templates/passcode/README.md as the key list.",
                    )
                    unsupportedLocales.add(locale)
                    continue
                }
                else -> {
                    logger.error("HTTP ${response.statusCode()} fetching $url; skipping locale '$locale'")
                    continue
                }
            }

            val fetchedDoc = parseXml(response.body())
            val targetFile = targetRoot.resolve(folderName).resolve("strings.xml")
            val (added, preserved) = mergeIntoFile(targetFile, fetchedDoc)
            totalAdded += added
            totalPreserved += preserved
            processedLocales.add(locale)
        }

        logger.lifecycle(
            "downloadPasscodeStrings: processed ${processedLocales.size} locale(s) → " +
                "added $totalAdded keys, preserved $totalPreserved consumer-customized keys" +
                if (unsupportedLocales.isNotEmpty()) ", skipped ${unsupportedLocales.size} unsupported: $unsupportedLocales" else "",
        )
    }

    /**
     * Merge `<string>` elements from [fetchedDoc] into [targetFile].
     * - If [targetFile] doesn't exist: write [fetchedDoc] as-is (canonically formatted).
     * - If it exists: parse it; for each fetched `<string name="X">`, append iff a string with
     *   the same name doesn't already exist (consumer-customized keys win); rewrite canonically.
     *
     * Returns `(added, preserved)`.
     */
    internal fun mergeIntoFile(targetFile: File, fetchedDoc: Document): Pair<Int, Int> {
        targetFile.parentFile.mkdirs()
        if (!targetFile.exists()) {
            writeCanonical(targetFile, fetchedDoc)
            val count = fetchedDoc.documentElement.getElementsByTagName("string").length
            return count to 0
        }

        val targetDoc = parseXml(targetFile.readText())
        val targetRoot = targetDoc.documentElement
        val existingNames = mutableSetOf<String>()
        val existing = targetRoot.getElementsByTagName("string")
        for (i in 0 until existing.length) {
            val element = existing.item(i) as Element
            existingNames.add(element.getAttribute("name"))
        }

        var added = 0
        var preserved = 0
        val fetched = fetchedDoc.documentElement.getElementsByTagName("string")
        for (i in 0 until fetched.length) {
            val element = fetched.item(i) as Element
            val name = element.getAttribute("name")
            if (name in existingNames) {
                preserved++
                continue
            }
            val imported = targetDoc.importNode(element, true)
            targetRoot.appendChild(imported)
            added++
        }

        if (added > 0) {
            writeCanonical(targetFile, targetDoc)
        } else {
            // Even with zero adds, rewrite to ensure canonical formatting (so a fresh consumer
            // file with non-standard formatting normalizes on first plugin run, then is stable).
            writeCanonical(targetFile, targetDoc)
        }
        return added to preserved
    }

    private fun parseXml(content: String): Document {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isIgnoringComments = false
            isIgnoringElementContentWhitespace = true
        }
        return factory.newDocumentBuilder().parse(content.byteInputStream())
    }

    private fun writeCanonical(file: File, document: Document) {
        normalizeWhitespace(document.documentElement)
        val transformer = TransformerFactory.newInstance().newTransformer().apply {
            setOutputProperty(OutputKeys.INDENT, "yes")
            setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            setOutputProperty(OutputKeys.ENCODING, "UTF-8")
        }
        file.outputStream().use { os ->
            transformer.transform(DOMSource(document), StreamResult(os))
        }
    }

    /** Strip whitespace-only text nodes so the Transformer's auto-indent produces clean output. */
    private fun normalizeWhitespace(element: Element) {
        val toRemove = mutableListOf<Node>()
        val children = element.childNodes
        for (i in 0 until children.length) {
            val child = children.item(i)
            if (child.nodeType == Node.TEXT_NODE && child.nodeValue?.isBlank() == true) {
                toRemove.add(child)
            }
        }
        toRemove.forEach { element.removeChild(it) }
    }
}
