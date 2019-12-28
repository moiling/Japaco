package com.moi.japaco

import com.moi.japaco.data.BaseTestCase
import com.moi.japaco.data.ISuiteCreator

data class TestCase(val a: Int, val b: Int):BaseTestCase(arrayOf(a, b))

fun main() {
    val className = "com.moi.test.sample.StaticRunner"
    val methodName = "test"

    val paco = Japaco(className, methodName, "${System.getProperty("user.dir")}/build/classes/java/main/")
    // analyze and stub.
    paco.generate()
    // create test suites.
    val suites = TestSuiteCreator().createSuites()
    // run test suites.
    val evaluator = paco.test(suites)

    paco.report()
}

class TestSuiteCreator : ISuiteCreator<BaseTestCase> {

    override fun createSuites(): Array<BaseTestCase> {
        return arrayOf(
            TestCase(0, 1),
            TestCase(1, 1),
            TestCase(2, 0),
            TestCase(3, 2),
            TestCase(3, 4),
            TestCase(3, 5)
        )
    }
}

