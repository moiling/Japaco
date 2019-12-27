package com.moi.japaco.test

import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

// javap -c Test.class > Test.java
fun main(args: Array<String>) {
    val cw = ClassWriter(0)

    cw.visit(
        Opcodes.V1_8,  // java version
        Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE,  // class modifier
        "com/moi/Test",  // class name (full path)
        null,  // signature
        "java/lang/Object",  // super name
        arrayOf("com/moi/Parent")  // interfaces
    )

    cw.visitField(
        Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_STATIC,  // field modifier
        "LESS", // field name
        "I",    // field type, I = 'int'
        null,   // unknown
        null    // default value (some errors)
    ).visitEnd()

    cw.visitMethod(
        Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT,  // method modifier
        "compareTo",  // method name
        "(Ljava/lang/Object;)I",  // input->Object, output->int
        null,   // unknown
        null    // unknown
    ).visitEnd()

    cw.visitEnd()

    val data = cw.toByteArray()
    val file = File("./Test.class")
    val fos = FileOutputStream(file)
    fos.write(data)
    fos.close()
}
