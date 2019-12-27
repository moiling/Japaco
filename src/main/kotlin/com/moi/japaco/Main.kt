package com.moi.japaco

import com.moi.japaco.data.Point
import com.moi.test.sample.StaticRunner
import java.util.*

data class TestCase(var a: Int, var b: Int)

fun main() {
    val className = "com/moi/test/sample/StaticRunner"
    val testFunc: (Int, Int) -> Unit = StaticRunner::test

    val userDir = System.getProperty("user.dir")
    val fileURL = "$userDir/build/classes/java/main/$className.class"
    SavePathGenerator().generate(className, fileURL)

    val suites = arrayOf(
        TestCase(0, 1),
        TestCase(1, 1),
        TestCase(2, 0),
        TestCase(3, 2),
        TestCase(3, 4),
        TestCase(3, 5)
    )
    val results = ArrayList<ArrayList<String>>()

    suites.forEach {
        testFunc(it.a, it.b)
        results.add(Data.getArray())
    }

    results.forEachIndexed { i, testCase ->
        println("Test Case $i:")
        testCase.forEach { text ->
            print("${Point(text).label}")
            if (Point(text).label != "END") print("->")
        }
        println()
    }

    // TODO: use true circle points
    val tmpCirclePoints = mutableSetOf<String>()
    tmpCirclePoints.add(results[0][3])
    // tmpCirclePoints.add(results[3][11])

    val handledResults = Estimator().handleCircleCoverages(results, tmpCirclePoints)

    println("-------")
    handledResults.forEach {
        it.forEachIndexed { i, testCase ->
            println("Test Case $i:")
            testCase.forEach { text ->
                print("${Point(text).label}")
                if (Point(text).label != "END") print("->")
            }
            println()
        }
    }

}

