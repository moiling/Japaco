package com.moi.sample

import com.moi.japaco.Japaco
import com.moi.japaco.config.PACKAGE_JAPACO

fun main() {
    val classPaths = arrayOf("${System.getProperty("user.dir")}/build/classes/java/main/")
    val startClass = "com.moi.test.sample.Multi"
    val startMethod = "test"
    val ignorePackage = arrayOf(PACKAGE_JAPACO)

    val paco = Japaco(startClass, startMethod, classPaths, ignorePackage)
    paco.generate()
    val suites = TestSuiteCreator().createSuites()
    val evaluator = paco.test(suites)
    paco.report()
}

class TestSuiteCreator {

    fun createSuites(): Array<Array<Any>> {
        return arrayOf(
            arrayOf(0, 1, "Test 1"),
            arrayOf(1, 1, "Test 2"),
            arrayOf(2, 0, "Test 3"),
            arrayOf(3, 2, "Test 4"),
            arrayOf(3, 4, "Test 5"),
            arrayOf(3, 5, "Test 6")
        )
    }
}

