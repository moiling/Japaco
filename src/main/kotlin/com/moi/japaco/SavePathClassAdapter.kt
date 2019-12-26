package com.moi.japaco

import com.moi.japaco.config.END
import com.moi.japaco.config.START
import jdk.internal.org.objectweb.asm.*

class SavePathClassAdapter constructor(version: Int, cv: ClassVisitor?) : ClassVisitor(version, cv) {
    private var owner: String? = null
    private var isInterface: Boolean = false
    private var version: Int = Opcodes.ASM5
    private var currentMethod: String? = null

    init {
        this.version = version
    }

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        owner = name
        isInterface = (access and Opcodes.ACC_INTERFACE) != 0   // and -> &
        cv.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        currentMethod = name
        // val newDesc = desc?.replaceBefore(')', "Ljava/util/ArrayList;")
        val mv: MethodVisitor? = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (isInterface) mv else SavePathMethodAdapter(version, mv)
    }

    inner class SavePathMethodAdapter(version: Int, mv: MethodVisitor?) : MethodVisitor(version, mv) {

        private fun addLabel(mv: MethodVisitor, text: String) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "com/moi/test/Data", "array", "Ljava/util/ArrayList;")
            mv.visitLdcInsn(text)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false)
            mv.visitInsn(Opcodes.POP)
        }

        override fun visitLabel(label: Label?) {
            mv.visitLabel(label)
            addLabel(mv, "$owner.$currentMethod:$label")
        }

        override fun visitCode() {
            mv.visitCode()
            addLabel(mv, "$owner.$currentMethod:$START")
        }

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                addLabel(mv, "$owner.$currentMethod:$END")
            }
            mv.visitInsn(opcode)    // RETURN
        }
    }
}