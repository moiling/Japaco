package com.moi.japaco.test

import com.sun.xml.internal.ws.org.objectweb.asm.ClassReader

import com.sun.xml.internal.ws.org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream

class AddTimeGenerator {
    fun generator() {
        val cr = ClassReader("com/moi/test/C")
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        // if used ASM version > 4.0: var cv = XXClassVisitor(Opcodes.ASM5, cw as ClassVisitor)
        val classAdapter = AddTimeClassAdapter(cw)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)
        val data = cw.toByteArray()

        val file = File("D:/Codes/Java Projects/Japaco/build/classes/java/main/com/moi/test/C.class")
        val fos = FileOutputStream(file)
        fos.write(data)
        fos.close()
        println("success!")
    }
}