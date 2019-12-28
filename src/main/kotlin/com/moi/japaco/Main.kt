package com.moi.japaco

import com.moi.japaco.data.BaseTestCase
import com.moi.japaco.data.ISuiteCreator

fun main() {
    val classPath = "${System.getProperty("user.dir")}/build/classes/java/main/"
    val startClass = "com.moi.test.sample.Main"
    val startMethod = "test"

    val paco = Japaco(startClass, startMethod, classPath)
    paco.generate()

    val suites = TestSuiteCreator().createSuites()

    val evaluator = paco.test(suites)

    paco.report()
}

class TestSuiteCreator : ISuiteCreator<BaseTestCase> {

    override fun createSuites(): Array<BaseTestCase> {
        return arrayOf(
            BaseTestCase(arrayOf(0, 1, "Test 1")),
            BaseTestCase(arrayOf(1, 1, "Test 2")),
            BaseTestCase(arrayOf(2, 0, "Test 3")),
            BaseTestCase(arrayOf(3, 2, "Test 4")),
            BaseTestCase(arrayOf(3, 4, "Test 5")),
            BaseTestCase(arrayOf(3, 5, "Test 6"))
        )
    }
}

