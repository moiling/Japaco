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
    private val classPathRoot: String
) {
    private val allEdges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()
    private val analyzer = Analyzer()
    private var reporter: Reporter? = null
    private var evaluator: Evaluator? = null

    fun generate() {
        val className = startClass.replace('.', '/')
        val fileURL = "$classPathRoot$className.class"
        println(fileURL)

        val cr = ClassReader(startClass)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classAdapter = PathClassAdapter(Opcodes.ASM5, cw, allEdges)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)  // out: allEdges

        // analyze
        analyzer.analyze(startMethod, className, allEdges)

        // save class file
        val data = cw.toByteArray()
        with(FileOutputStream(File(fileURL))) {
            write(data)
            close()
        }
    }

    fun test(suites: Array<BaseTestCase>, classObj: Any? = null): Evaluator {
        val results = ArrayList<ArrayList<String>>()
        // !SHOULD NOT! load class before this.
        val method = Class.forName(startClass).methods.find { it.name == startMethod }

        suites.forEach {
            method?.invoke(classObj, *it.args)
            results.add(Data.getArray())
        }

        evaluator = Evaluator(analyzer, results)
        evaluator?.evaluate()
        return evaluator!!
    }

    fun report() {
        // report
        reporter = Reporter(allEdges, evaluator)
        reporter?.report()

    }
}