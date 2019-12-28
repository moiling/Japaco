package com.moi.japaco.data

interface ISuiteCreator<T> {
    fun createSuites() : Array<T>
}
