package dev.vyp.stringcare.benchmark

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import java.io.File
import java.nio.file.Files
import javax.xml.parsers.SAXParserFactory
import kotlin.random.Random
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler

@State(Scope.Benchmark)
open class XmlParsingBenchmark {
    @Param("10", "100", "1000")
    var stringCount: Int = 10

    private lateinit var xmlFile: File

    @Setup(Level.Trial)
    fun setup() {
        xmlFile = Files.createTempFile("strings-", ".xml").toFile()
        xmlFile.writeText(buildXml(stringCount))
    }

    @Benchmark
    fun parseStringsXml(): Int {
        var matched = 0
        val parser = SAXParserFactory.newInstance().newSAXParser()
        parser.parse(
            xmlFile,
            object : DefaultHandler() {
                override fun startElement(
                    uri: String?,
                    localName: String?,
                    qName: String?,
                    attributes: Attributes?,
                ) {
                    if (qName == "string" && attributes?.getValue("hidden") == "true") {
                        matched += 1
                    }
                }
            },
        )
        return matched
    }

    private fun buildXml(items: Int): String {
        val payload =
            buildString {
                append("<resources>\n")
                repeat(items) { index ->
                    val value = Random.nextInt(1_000_000, 9_999_999)
                    append("""    <string name="k_$index" hidden="true">value_$value</string>""")
                    append('\n')
                }
                append("</resources>\n")
            }
        return payload
    }
}
