package com.moi.japaco.test

import jdk.internal.org.objectweb.asm.*

class LogClassAdapter constructor(version: Int, cv: ClassVisitor?) : ClassVisitor(version, cv) {
    private var owner: String? = null
    private var isInterface: Boolean = false
    private var version: Int = Opcodes.ASM5

    init {
        this.version = version
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        println("[Class:visit]:\n\t{version:$version, access:$access, name:$name, signature:$signature, superName:$superName, interfaces:$interfaces}")
        owner = name
        isInterface = (access and Opcodes.ACC_INTERFACE) != 0   // and -> &
        cv.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitField(access: Int, name: String?, desc: String?, signature: String?, value: Any?): FieldVisitor {
        println("[Class:visitField]:\n\t{access:$access, name:$name, desc:$desc, signature:$signature, value:$value}")
        return cv.visitField(access, name, desc, signature, value)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        println("[Class:visitMethod]:\n\t{access:$access, name:$name, desc:$desc, signature:$signature, exceptions:$exceptions}")
        val mv: MethodVisitor? = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (isInterface) mv else LogMethodAdapter(version, mv)
    }

    override fun visitEnd() {
        println("[Class:visitEnd]")
        cv.visitEnd()
    }

    inner class LogMethodAdapter(version: Int, mv: MethodVisitor?) : MethodVisitor(version, mv) {
        override fun visitParameter(p0: String?, p1: Int) {
            println("[Method:visitParameter]:\n\t{p0:$p0, p1:$p1}")
            mv.visitParameter(p0, p1)
        }

        override fun visitAttribute(attribute: Attribute?) {
            println("[Method:visitAttribute]:\n\t{attribute:$attribute}")
            mv.visitAttribute(attribute)
        }

        override fun visitCode() {
            println("[Method:visitCode]")
            mv.visitCode()
        }

        override fun visitFrame(type: Int, nLocal: Int, local: Array<out Any>?, nStack: Int, stack: Array<out Any>?) {
            println("[Method:visitFrame]:\n\t{type:$type, nLocal:$nLocal, local:$local, nStack:$nStack, stack:$stack}")
            mv.visitFrame(type, nLocal, local, nStack, stack)
        }

        override fun visitInsn(opcode: Int) {
            println("[Method:visitInsn]:\n\t{opcode:$opcode}")
            mv.visitInsn(opcode)
        }

        override fun visitLdcInsn(ldc: Any?) {
            println("[Method:visitLdcInsn]:\n\t{ldc:$ldc}")
            mv.visitLdcInsn(ldc)
        }

        override fun visitInvokeDynamicInsn(p0: String?, p1: String?, p2: Handle?, vararg p3: Any?) {
            println("[Method:visitInvokeDynamicInsn]:\n\t{p0:$p0, p1:$p1, p2:$p2}")
            println("\tp3:")
            p3.forEach { println("\t" + it) }
            mv.visitInvokeDynamicInsn(p0, p1, p2, *p3)
        }

        override fun visitIincInsn(p0: Int, p1: Int) {
            println("[Method:visitIincInsn]:\n\t{p0:$p0, p1:$p1}")
            mv.visitIincInsn(p0, p1)
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, p4: Boolean) {
            println("[Method:visitMethodInsn]:\n\t{opcode:$opcode, owner:$owner, name:$name, desc:$desc, p4:$p4}")
            mv.visitMethodInsn(opcode, owner, name, desc, p4)
        }

        override fun visitTypeInsn(opcode: Int, owner: String?) {
            println("[Method:visitTypeInsn]:\n\t{opcode:$opcode, owner:$owner}")
            mv.visitTypeInsn(opcode, owner)
        }

        override fun visitLabel(label: Label?) {
            println("[Method:visitLabel]:\n\t{label:$label}")
            mv.visitLabel(label)
        }

        override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
            println("[Method:visitTableSwitchInsn]:\n\t{min:$min, max:$max, dflt:$dflt}")
            println("\tlabels:")
            labels.forEach { println("\t" + it) }
            mv.visitTableSwitchInsn(min, max, dflt, *labels)
        }

        override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
            println("[Method:visitLookupSwitchInsn]:\n\t{dflt:$dflt, keys:$keys, labels:$labels}")
            mv.visitLookupSwitchInsn(dflt, keys, labels)
        }

        override fun visitJumpInsn(opcode: Int, label: Label?) {
            println("[Method:visitJumpInsn]:\n\t{opcode:$opcode, label:$label}")
            mv.visitJumpInsn(opcode, label)
        }

        override fun visitLineNumber(line: Int, start: Label?) {
            println("[Method:visitLineNumber]:\n\t{line:$line, start:$start}")
            mv.visitLineNumber(line, start)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            println("[Method:visitMaxs]:\n\t{maxStack:$maxStack, maxLocals:$maxLocals}")
            mv.visitMaxs(maxStack, maxLocals)
        }

        override fun visitEnd() {
            println("[Method:visitEnd]")
            mv.visitEnd()
        }
    }
}