package com.moi.japaco.worker

import com.moi.japaco.data.Point
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Reporter(
    private val allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>,
    private val classPaths: Array<String>,
    private val ignorePackage: Array<String>,
    private val startClass: String,
    private val startMethod:String
) {

    fun report(outFile: String, evaluator: Evaluator, suites: Array<Array<Any>>, log: Boolean = false) {
        var reportText = "#Report\n"
        reportText += "**Time**: ${SimpleDateFormat("M/d/Y HH:mm").format(Date())}\n"
        reportText += "**Class Paths**: ${getArrayText(classPaths)}\n"
        reportText += "**Ignore Packages**: ${getArrayText(ignorePackage)}\n"
        reportText += "**Start Method**: $startClass.$startMethod\n"
        reportText += "\n"
        reportText += "##Flow\n"
        reportText += getGraphText()
        reportText += "\n"
        reportText += "##Path Coverage\n"
        reportText += "Path Coverage Rate: **${100 * evaluator.getTargetCoverageState().count { it.second > 0 } / evaluator.getTargetCoverageState().size}%**\n"
        reportText += "###Targets Coverage State\n"
        reportText += getTargetsCoverageStateTable(evaluator)
        reportText += "\n"
        reportText += "###Suites Coverage State\n"
        reportText += getSuitesCoverageStateTable(evaluator, suites)
        reportText += "\n"
        reportText += "###Original Suites Path\n"
        reportText += getOriginalSuitePathTable(evaluator, suites)
        reportText += "\n"

        if (log) print(reportText)

        val data = reportText.toByteArray()
        val file = File(outFile)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        with(FileOutputStream(file)) {
            write(data)
            close()
        }
    }

    private fun <T> getArrayText(arrayText: Array<T>): String {
        var text = ""
        arrayText.forEachIndexed { index, s ->
            text += "$s"
            if (index != arrayText.lastIndex) text += ", "
        }
        return text
    }

    private fun getOriginalSuitePathTable(evaluator: Evaluator, suites: Array<Array<Any>>): String {
        var text = "|Args|Paths|\n|:-|:-|\n"
        evaluator.getOriginalSuitePath().forEachIndexed { index, it ->
            text += "|${getArrayText(suites[index])}|${getPathText(it)}|\n"
        }
        return text
    }

    private fun getTargetsCoverageStateTable(evaluator: Evaluator): String {
        var text = "|Id|Coverage Times|Paths|\n|:-|:-|:-|\n"
        evaluator.getTargetCoverageState().forEachIndexed { index, target ->
            text += "|$index|${target.second}|${getPathText(target.first)}|\n"
        }
        return text
    }

    private fun getSuitesCoverageStateTable(evaluator: Evaluator, suites: Array<Array<Any>>): String {
        var text = "|Args|Coverage Target Id|Paths|\n|:-|:-|:-|\n"
        evaluator.getSuiteCoverageState().forEachIndexed { index, it ->
            it.forEach { testCase ->
                text += "|${getArrayText(suites[index])}|${testCase.second}|${getPathText(testCase.first)}|\n"
            }
        }
        return text
    }

    private fun getPathText(path: ArrayList<Point>): String {
        var currentMethod = ""
        var currentOwner = ""
        var text = ""
        path.forEachIndexed { i, p ->
            if (p.owner != currentOwner) {
                currentOwner = p.owner!!
                currentMethod = ""
                text += "{"
                text += "${p.owner!!.split('/').last()}::"
            }
            if (p.method != currentMethod) {
                currentMethod = p.method!!
                text += "["
                text += "${p.method?.replace("<", "")?.replace(">", "")}."
            }
            text += "`${p.display}`"

            if (i != path.lastIndex) {
                when {
                    path[i + 1].owner != currentOwner -> {
                        text += "}"
                        text += " ==> "
                    }
                    path[i + 1].method != currentMethod -> {
                        text += "]"
                        text += " -> "
                    }
                    else -> text += " > "
                }
            } else {
                text += "]}"
            }
        }
        return text
    }

    private fun getGraphText(): String {
        var text = ""
        text += "```mermaid\n"
        text += "graph TB\n"
        allEdges.forEach {
            text += "subgraph ${it.key.split('.')[0].split('/').last()}.${it.key.split('.')[1]}\n"
            val tmpSet = it.value.toSet()
            tmpSet.forEach { p ->
                // <init> -> #lt#init#gt#
                val fMethod = p.first.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sMethod = p.second.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val fLabel = p.first.label?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sLabel = p.second.label?.replace("<", "#lt#")?.replace(">", "#gt#")
                text += "${p.first.owner}.$fMethod:$fLabel((${p.first.display}))-->" +
                            "${p.second.owner}.$sMethod:$sLabel((${p.second.display}))\n"
            }
            text += "end\n"
        }
        text += "```\n"

        return text
    }
}