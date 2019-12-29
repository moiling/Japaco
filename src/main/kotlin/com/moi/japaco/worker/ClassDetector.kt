package com.moi.japaco.worker

import com.moi.japaco.config.PACKAGE_JAPACO
import java.io.File

class ClassDetector(
    private val ignorePackage: Array<String> = arrayOf(PACKAGE_JAPACO)
) {

    fun detect(classPaths: Array<String>): MutableList<String> {
        val classNames: MutableList<String> = mutableListOf()

        classPaths.forEach { classPath ->
            val fileTree: FileTreeWalk = File(classPath).walk()
            fileTree.filter { it.isFile }
                .filter { it.extension == "class" }
                .filter { f ->
                    var isIgnore = false
                    ignorePackage.forEach { ip ->
                        isIgnore = !f.absolutePath.substring(classPath.length).startsWith(ip) || isIgnore
                    }
                    isIgnore
                }
                .forEach {
                    classNames.add(
                        it.absolutePath.substring(
                            classPath.length,
                            it.absolutePath.length - it.extension.length - 1
                        )
                    )
                }
        }
        return classNames
    }
}