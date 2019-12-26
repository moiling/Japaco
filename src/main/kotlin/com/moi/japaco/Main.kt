package com.moi.japaco

import com.moi.test.Data
import com.moi.test.testReturn.TestMultiReturn
import com.moi.test.testall.StaticTestAll
import java.util.ArrayList

data class TestCase(var a: Int, var b: Int)

fun main() {
    val userDir = System.getProperty("user.dir")
    val className = "com/moi/test/testReturn/TestMultiReturn"
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
    val testFunc: (Int, Int) -> Unit = TestMultiReturn::testMultiReturn

    suites.forEach {
        testFunc(it.a, it.b)
        results.add(Data.getArray())
    }

    results.forEach { println(it) }
}

