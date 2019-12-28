package com.moi.japaco

import com.moi.japaco.config.JAPACO_PACKAGE
import java.io.File

class ClassDetector(
    private val ignorePackage:Array<String> = arrayOf(JAPACO_PACKAGE)
) {

    fun detect(classPath: String): MutableList<String> {
        val classNames: MutableList<String> = mutableListOf()

        val fileTree: FileTreeWalk = File(classPath).walk()
        fileTree.filter { it.isFile }
            .filter { it.extension == "class" }
            .filter { f ->
                var isIgnore = false
                ignorePackage.forEach { ip ->
                    isIgnore = !f.absolutePath.substring(classPath.length).startsWith(ip)
                }
                isIgnore
            }
            .forEach { classNames.add(it.absolutePath.substring(classPath.length, it.absolutePath.length - it.extension.length - 1)) }
        return classNames
    }
}