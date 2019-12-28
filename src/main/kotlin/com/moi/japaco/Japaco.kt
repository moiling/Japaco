package com.moi.japaco

import com.moi.japaco.data.BaseTestCase
import com.moi.japaco.data.Point
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

class Japaco(
    private var startClass: String,
    private val startMethod: String,
    private val classPath: String
) {
    private var analyzer: Analyzer? = null
    private var reporter: Reporter? = null
    private var evaluator: Evaluator? = null

    fun generate() {

        val classNames = ClassDetector().detect(classPath)

        val allEdges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()

        classNames.forEach { className ->
            val edges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()
            val fileURL = "$classPath$className.class"
            val cr = ClassReader(className)
            val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
            val classAdapter = PathClassAdapter(Opcodes.ASM5, cw, edges, classNames)
            cr.accept(classAdapter, ClassReader.SKIP_DEBUG)  // out: edges
            allEdges.putAll(edges)

            // save class file
            val data = cw.toByteArray()
            with(FileOutputStream(File(fileURL))) {
                write(data)
                close()
            }
        }

        // analyze
        analyzer = Analyzer(startMethod, startClass.replace('.', '/'), allEdges)
        analyzer!!.analyze()
    }

    fun test(suites: Array<BaseTestCase>, classObj: Any? = null): Evaluator {
        val results = ArrayList<ArrayList<String>>()
        // !SHOULD NOT! load class before this.
        val method = Class.forName(startClass).methods.find { it.name == startMethod }

        suites.forEach {
            method?.invoke(classObj, *it.args)
            results.add(Data.getArray())
        }

        evaluator = Evaluator(analyzer!!, results)
        evaluator!!.evaluate()
        return evaluator!!
    }

    fun report() {
        // report
        reporter = Reporter(analyzer!!.getPassedEdges(), evaluator)
        reporter!!.report()

    }
}