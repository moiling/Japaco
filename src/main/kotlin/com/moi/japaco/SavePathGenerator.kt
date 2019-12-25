package com.moi.japaco

import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

class SavePathGenerator {
    fun generate(className: String, fileURL: String) {
        val cr = ClassReader(className)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classAdapter = SavePathClassAdapter(Opcodes.ASM5, cw)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)
        val data = cw.toByteArray()

        val file = File(fileURL)
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.close()
    }
}