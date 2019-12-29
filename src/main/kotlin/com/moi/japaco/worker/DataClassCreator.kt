package com.moi.japaco.worker

import com.moi.japaco.config.CLASS_DATA
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream

class DataClassCreator {

    /*
     *   package com.moi.japaco.data;
     *   import java.util.ArrayList;

     *   public class Data {
     *       public static ArrayList<String> array = new ArrayList<>();
     *
     *       public static ArrayList<String> getArray() {
     *           ArrayList<String> result = array;
     *           array = new ArrayList<>();
     *           return result;
     *       }
     *
     *       public static void clear() {
     *           array.clear();
     *       }
     *   }
     */
    fun create(dataClassPath: String) {
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, CLASS_DATA, "Ljava/util/ArrayList<Ljava/lang/String;>;", "java/lang/Object", null)

        // array
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "array", "Ljava/util/ArrayList;", null, null
        ).visitEnd()

        // <init>
        var mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null)
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()

        // getArray
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "getArray", "()Ljava/util/ArrayList;", "()Ljava/util/ArrayList<Ljava/lang/String;>;", null)
        mv.visitFieldInsn(Opcodes.GETSTATIC, "com/moi/japaco/Data", "array", "Ljava/util/ArrayList;")
        mv.visitVarInsn(Opcodes.ASTORE, 0)
        mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/moi/japaco/Data", "array", "Ljava/util/ArrayList;")
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitInsn(Opcodes.ARETURN)
        mv.visitMaxs(2, 1)
        mv.visitEnd()

        // clear
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "clear", "()V", null, null)
        mv.visitFieldInsn(Opcodes.GETSTATIC, "com/moi/japaco/Data", "array", "Ljava/util/ArrayList;")
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "clear", "()V", false)
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 0)
        mv.visitEnd()

        // <clinit>
        mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null)
        mv.visitTypeInsn(Opcodes.NEW, "java/util/ArrayList")
        mv.visitInsn(Opcodes.DUP)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false)
        mv.visitFieldInsn(Opcodes.PUTSTATIC, "com/moi/japaco/Data", "array", "Ljava/util/ArrayList;")
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(2, 0)
        mv.visitEnd()

        cw.visitEnd()

        val data = cw.toByteArray()
        val file = File(dataClassPath)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        with(FileOutputStream(file)) {
            write(data)
            close()
        }
    }
}