package com.moi.japaco.test

import com.moi.test.testall.TestAll

fun main() {
    LogGenerator().generate()

    val testAll = TestAll()
    testAll.testAll(0, 2)
}