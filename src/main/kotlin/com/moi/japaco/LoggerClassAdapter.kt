package com.moi.japaco

import jdk.internal.org.objectweb.asm.*

class LoggerClassAdapter constructor(version: Int, cv: ClassVisitor?) : ClassVisitor(version, cv) {
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
        val mv: MethodVisitor? = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (isInterface) mv else LoggerMethodAdapter(version, mv)
    }

    inner class LoggerMethodAdapter(version: Int, mv: MethodVisitor?) : MethodVisitor(version, mv) {

        private fun log(mv: MethodVisitor, text: String) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
            mv.visitLdcInsn(text)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false)
        }

        override fun visitCode() {
            mv.visitCode()
            // log(mv, "start")
        }

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                // log(mv, "end")
            }
            mv.visitInsn(opcode)    // RETURN
        }

        override fun visitLabel(label: Label?) {
            mv.visitLabel(label)
            log(mv, "$currentMethod.$label")
        }
    }
}