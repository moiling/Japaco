package com.moi.japaco.worker

import com.moi.japaco.data.Point
import java.util.*

class Reporter(
    private val allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>,
    private val evaluator: Evaluator?
) {

    fun report() {
        reportGraph()
        reportCoverageState()
    }

    private fun reportCoverageState() {
        println("============== Original Suites ==============")
        evaluator?.getOriginalSuiteCoverages()?.forEachIndexed { index, it ->
            print("Test Case $index:\t")
            print(getPathStr(it))
            println()
        }

        println("============== Suites Coverage State ==============")
        evaluator?.getSuiteCoverageState()?.forEachIndexed { index, it ->
            it.forEach { testCase ->
                print("Test Case $index:\tcoverage target:${testCase.second}\t")
                print(getPathStr(testCase.first))
                println()
            }
        }

        println("============= Targets Coverage State ==============")
        evaluator?.getTargetCoverageState()?.forEachIndexed { index, target ->
            print("Target $index:\tcoverage times:${target.second}\t")
            print(getPathStr(target.first))
            println()
        }

        println("coverage: ${100 * evaluator?.getTargetCoverageState()!!.count { it.second > 0 } / evaluator.getTargetCoverageState().size}%")
    }

    private fun getPathStr(path: ArrayList<Point>): String {
        var currentMethod = ""
        var currentOwner = ""
        var pathStr = ""
        path.forEachIndexed { i, p ->
            if (p.owner != currentOwner) {
                currentOwner = p.owner!!
                currentMethod = ""
                pathStr = pathStr.plus("{")
                pathStr = pathStr.plus("${p.owner!!.split('/').last()}::")
            }
            if (p.method != currentMethod) {
                currentMethod = p.method!!
                pathStr = pathStr.plus("[")
                pathStr = pathStr.plus("${p.method?.replace("<", "")?.replace(">", "")}.")
            }
            pathStr = pathStr.plus("${p.display}")

            if (i != path.lastIndex) {
                when {
                    path[i + 1].owner != currentOwner -> {
                        pathStr = pathStr.plus("}")
                        pathStr = pathStr.plus(" ==> ")
                    }
                    path[i + 1].method != currentMethod -> {
                        pathStr = pathStr.plus("]")
                        pathStr = pathStr.plus(" -> ")
                    }
                    else -> pathStr = pathStr.plus(" > ")
                }
            } else {
                pathStr = pathStr.plus("]}")
            }
        }
        return pathStr
    }

    private fun reportGraph() {
        println("```mermaid")
        println("graph TB")
        allEdges.forEach {
            println("subgraph ${it.key.split('.')[0].split('/').last()}.${it.key.split('.')[1]}")
            val tmpSet = it.value.toSet()
            tmpSet.forEach { p ->
                // <init> -> #lt#init#gt#
                val fMethod = p.first.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sMethod = p.second.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val fLabel = p.first.label?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sLabel = p.second.label?.replace("<", "#lt#")?.replace(">", "#gt#")
                println("${p.first.owner}.$fMethod:$fLabel((${p.first.display}))-->" +
                        "${p.second.owner}.$sMethod:$sLabel((${p.second.display}))")
            }
            println("end")
        }
        println("```")
    }
}