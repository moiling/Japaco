package com.moi.japaco

import com.moi.japaco.config.*
import com.moi.japaco.data.Point
import com.moi.japaco.worker.*
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

class Japaco(
    private var startClass: String,
    private val startMethod: String,
    private val classPaths: Array<String>,
    private val ignorePackage:Array<String> = arrayOf(PACKAGE_JAPACO)
) {
    private var analyzer: Analyzer? = null
    private var reporter: Reporter? = null
    private var evaluator: Evaluator? = null

    fun generate() {

        val classNames = ClassDetector(ignorePackage).detect(classPaths)
        val allEdges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()

        DataClassCreator().create("${classPaths[0]}$CLASS_DATA.class")

        classPaths.forEach { classPath ->
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
        }

        // analyze
        if (allEdges.isEmpty()) {
            throw Exception(EXCEPTION_NO_PATH)
        }
        analyzer = Analyzer(startMethod, startClass.replace('.', '/'), allEdges)
        analyzer!!.analyze()
    }

    fun test(suites: Array<Array<Any>>, classObj: Any? = null): Evaluator {
        val results = ArrayList<ArrayList<String>>()
        // !SHOULD NOT! load class before here.
        val method = Class.forName(startClass).methods.find { it.name == startMethod }


        // Means: Data.clear() -> clear the path before here.
        Class.forName(CLASS_DATA.replace('/', '.')).methods.find { it.name == METHOD_CLEAR }!!.invoke(null)

        suites.forEach {
            try {
                method?.invoke(classObj, *it)
            } catch (e: java.lang.Exception) {
                throw Exception(EXCEPTION_WRONG_TEST_CASE)
            }
            // Means: results.add(Data.getArray())
            val path = Class.forName(CLASS_DATA.replace('/', '.')).methods.find { m -> m.name == METHOD_GET_ARRAY }!!.invoke(null)
            if (path is ArrayList<*>) {
                val point = ArrayList<String>()
                path.filter { p -> p is String }.forEach { p ->
                    point.add(p as String)
                }
                results.add(point)
            }
        }

        evaluator = Evaluator(analyzer!!, results)
        evaluator!!.evaluate()
        return evaluator!!
    }

    fun report(evaluator: Evaluator, suites: Array<Array<Any>>, outFile: String, log: Boolean = false) {
        // report
        reporter = Reporter(analyzer!!.getPassedEdges(), classPaths, ignorePackage, startClass, startMethod)
        reporter!!.report(outFile, evaluator, suites, log)

    }
}
