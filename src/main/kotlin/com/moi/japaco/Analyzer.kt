package com.moi.japaco

import com.moi.japaco.config.END
import com.moi.japaco.config.START
import com.moi.japaco.data.Point
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Analyzer {

    fun analyze(className: String, startMethod: String) {
        val allPaths = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()
        val cr = ClassReader(className)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classAdapter = AnalyzePathClassAdapter(Opcodes.ASM5, cw, allPaths)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)

        Reporter().report(allPaths)

        getAllTargets(allPaths, "$className.$startMethod")
    }

    private fun getAllTargets(allPaths: MutableMap<String, ArrayList<Pair<Point, Point>>>, startMethod: String) {
        // get number of points.
        val pointSet = HashSet<Point>()
        allPaths.forEach {
            if (it.key == startMethod) {
                it.value.forEach { p ->
                    pointSet.add(p.first)
                    pointSet.add(p.second)
                }
            }
        }
        val pointArray = pointSet.toTypedArray()

        // find START and END.
        val startIndex = pointArray.indexOfFirst { it.label == START }
        val endIndex = pointArray.indexOfFirst { it.label == END }

        // create adjacency list(save index of pointArray).
        val adjList = Array<ArrayList<Int>>(pointArray.size) { ArrayList() }

        allPaths.forEach {
            if (it.key == startMethod) {
                it.value.forEach { p ->
                    adjList[pointArray.indexOf(p.first)].add(pointArray.indexOf(p.second))
                }
            }
        }

        println("size:${pointArray.size}, start index:$startIndex, end index:$endIndex")
        adjList.forEach { println(it) }
    }
}

fun main() {
    val className = "com/moi/test/sample/StaticRunner"
    Analyzer().analyze(className, "run")
}