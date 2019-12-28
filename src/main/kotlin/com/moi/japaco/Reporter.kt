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
                print("Test Case $index:\tcoverage target:${testCase.second}")
                testCase.first.forEach { p ->
                    print("${p.label}")
                    if (p.label != "END") print("->")
                }
                println()
            }
        }

        evaluator?.getTargetCoverageState()?.forEachIndexed { index, target ->
            print("Target $index:\tcoverage times:${target.second}")
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
            println("subgraph ${it.key.split('.')[1]}")
            it.value.forEach { p ->
                // <init> -> #lt#init#gt#
                val fMethod = p.first.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sMethod = p.second.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                println("${p.first.owner}.$fMethod:${p.first.label}((${p.first.display}))-->" +
                        "${p.second.owner}.$sMethod:${p.second.label}((${p.second.display}))")
            }
            println("end")
        }
        println("```")
    }
}