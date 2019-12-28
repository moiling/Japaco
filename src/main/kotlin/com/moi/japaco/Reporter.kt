package com.moi.japaco

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
        evaluator?.getSuiteCoverageState()?.forEachIndexed { index, it ->
            it.forEach { testCase ->
                print("Test Case $index:\tcoverage target:${testCase.second}\t")
                testCase.first.forEach { p ->
                    print("${p.label}")
                    if (p.label != "END") print("->")
                }
                println()
            }
        }

        evaluator?.getTargetCoverageState()?.forEachIndexed { index, target ->
            print("Target $index:\tcoverage times:${target.second}\t")
            target.first.forEach { p ->
                print("${p.label}")
                if (p.label != "END") print("->")
            }
            println()
        }

        println("coverage: ${100 * evaluator?.getTargetCoverageState()!!.count { it.second > 0 } / evaluator.getTargetCoverageState().size}%")
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