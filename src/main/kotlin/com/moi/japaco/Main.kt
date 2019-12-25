package com.moi.japaco

import com.moi.test.Data
import com.moi.test.testall.StaticTestAll
import java.util.ArrayList

data class TestCase(var a: Int, var b: Int)

fun main() {
    val userDir = System.getProperty("user.dir")
    val className = "com/moi/test/testall/StaticTestAll"
    val fileURL = "$userDir/build/classes/java/main/$className.class"
    SavePathGenerator().generate(className, fileURL)

    val suites = arrayOf(
        TestCase(0, 1),
        TestCase(1, 1),
        TestCase(2, 0),
        TestCase(3, 2)
    )
    val results = ArrayList<ArrayList<String>>()
    val testFunc: (Int, Int) -> Unit = StaticTestAll::testAll

    suites.forEach {
        testFunc(it.a, it.b)
        results.add(Data.getArray())
    }

    results.forEach { println(it) }
}

