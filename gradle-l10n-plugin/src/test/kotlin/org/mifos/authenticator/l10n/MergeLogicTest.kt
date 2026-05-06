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

import org.gradle.testfixtures.ProjectBuilder
import org.w3c.dom.Document
import java.io.File
import java.nio.file.Files
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergeLogicTest {

    private val tempDir: File = Files.createTempDirectory("mifos-l10n-test").toFile()

    @AfterTest
    fun cleanup() {
        tempDir.deleteRecursively()
    }

    private fun newTask(): DownloadPasscodeStringsTask {
        val project = ProjectBuilder.builder().withProjectDir(tempDir).build()
        return project.tasks.register("testTask", DownloadPasscodeStringsTask::class.java).get()
    }

    private fun parseXml(content: String): Document {
        return DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = false
            isIgnoringElementContentWhitespace = true
        }.newDocumentBuilder().parse(content.byteInputStream())
    }

    private val templateXml = """
        <?xml version="1.0" encoding="utf-8"?>
        <resources>
            <string name="mifos_passcode_create_passcode">Create Passcode</string>
            <string name="mifos_passcode_confirm_passcode">Confirm Passcode</string>
            <string name="mifos_passcode_yes">Yes</string>
            <string name="mifos_passcode_no">No</string>
        </resources>
    """.trimIndent()

    @Test
    fun `clean install writes template into a fresh file`() {
        val task = newTask()
        val target = File(tempDir, "fresh/strings.xml")
        assertFalse(target.exists())

        val (added, preserved) = task.mergeIntoFile(target, parseXml(templateXml))

        assertEquals(4, added)
        assertEquals(0, preserved)
        assertTrue(target.exists())
        val written = target.readText()
        assertContains(written, "mifos_passcode_create_passcode")
        assertContains(written, "Create Passcode")
    }

    @Test
    fun `append into existing file adds new keys without touching existing ones`() {
        val task = newTask()
        val target = File(tempDir, "existing/strings.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_name">My App</string>
                    <string name="login_button">Sign in</string>
                </resources>
                """.trimIndent(),
            )
        }

        val (added, preserved) = task.mergeIntoFile(target, parseXml(templateXml))

        assertEquals(4, added) // 4 new mifos_passcode_* keys
        assertEquals(0, preserved) // no overlap with consumer keys
        val written = target.readText()
        assertContains(written, "app_name")          // consumer key untouched
        assertContains(written, "login_button")
        assertContains(written, "mifos_passcode_create_passcode") // new key added
        assertContains(written, "mifos_passcode_yes")
    }

    @Test
    fun `keys already present in consumer file are preserved, not overwritten`() {
        val task = newTask()
        // Consumer has hand-customized mifos_passcode_yes value.
        val target = File(tempDir, "customized/strings.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="mifos_passcode_yes">YEP, ABSOLUTELY</string>
                </resources>
                """.trimIndent(),
            )
        }

        val (added, preserved) = task.mergeIntoFile(target, parseXml(templateXml))

        assertEquals(3, added)     // create_passcode, confirm_passcode, no
        assertEquals(1, preserved) // mifos_passcode_yes — consumer's value wins
        val written = target.readText()
        assertContains(written, "YEP, ABSOLUTELY")
        assertFalse(written.contains(">Yes<"), "consumer's customized value must not be overwritten by template")
    }

    @Test
    fun `idempotent re-run produces zero new additions on second invocation`() {
        val task = newTask()
        val target = File(tempDir, "idempotent/strings.xml")

        val (added1, preserved1) = task.mergeIntoFile(target, parseXml(templateXml))
        assertEquals(4, added1)
        assertEquals(0, preserved1)

        // Run again with the same template — every key now exists, so nothing should be added.
        val (added2, preserved2) = task.mergeIntoFile(target, parseXml(templateXml))
        assertEquals(0, added2)
        assertEquals(4, preserved2)
    }

    @Test
    fun `re-run produces byte-identical file output (canonical formatting)`() {
        val task = newTask()
        val target = File(tempDir, "stable/strings.xml")

        task.mergeIntoFile(target, parseXml(templateXml))
        val firstRunBytes = target.readBytes()

        task.mergeIntoFile(target, parseXml(templateXml))
        val secondRunBytes = target.readBytes()

        assertTrue(
            firstRunBytes.contentEquals(secondRunBytes),
            "second run produced different bytes than first run; canonical formatter is not stable",
        )
    }

    @Test
    fun `merge into an existing file containing only the resources element header`() {
        val task = newTask()
        val target = File(tempDir, "empty/strings.xml").apply {
            parentFile.mkdirs()
            writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                </resources>
                """.trimIndent(),
            )
        }

        val (added, preserved) = task.mergeIntoFile(target, parseXml(templateXml))

        assertEquals(4, added)
        assertEquals(0, preserved)
    }
}
