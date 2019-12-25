package com.moi.japaco.test


import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

class LogGenerator {
    fun generate() {
        val cr = ClassReader("com/moi/test/testall/TestAll")
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        // if used ASM version > 4.0: var cv = XXClassVisitor(Opcodes.ASM5, cw as ClassVisitor)
        val classAdapter = LogClassAdapter(Opcodes.ASM5, cw)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)
        val data = cw.toByteArray()

        val file = File("D:/Codes/Java Projects/Japaco/build/classes/java/main/com/moi/test/testall/TestAll.class")
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.close()
    }
}