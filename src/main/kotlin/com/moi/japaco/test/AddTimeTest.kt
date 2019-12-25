package com.moi.japaco.test

import com.moi.test.C

fun main() {
    AddTimeGenerator().generator()

    val c = C()
    c.m()
    val cClazz = c.javaClass
    println(cClazz.getField("timer").get(c))
}