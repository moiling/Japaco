package com.moi.japaco.test

import com.sun.xml.internal.ws.org.objectweb.asm.*

// ClassAdapter is now gone form ASM 4.0, so use com.sun...asm for test only.
// Use ClassVisitor instead.
public class AddTimeClassAdapter public constructor(cv: ClassVisitor?) : ClassAdapter(cv) {
    private var owner: String? = null
    private var isInterface: Boolean = false

    init {
        // default constructor
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        cv.visit(version, access, name, signature, superName, interfaces)
        owner = name
        isInterface = (access and Opcodes.ACC_INTERFACE) != 0   // and -> &
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        var mv = cv.visitMethod(access, name, desc, signature, exceptions)
        if (!name.equals("<init>") && !isInterface && mv != null) {
            mv = AddTimeMethodAdapter(mv)
        }
        return mv
    }

    override fun visitEnd() {
        if (!isInterface) {
            cv.visitField(
                Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "timer",
                "J",
                null,
                null
            )?.visitEnd()
        }
        cv.visitEnd()
    }


    inner class AddTimeMethodAdapter(mv: MethodVisitor?) : MethodAdapter(mv) {
        override fun visitCode() {
            mv.visitCode()
            // add some codes at top
            // #TOP#
            // L0
            // LINENUMBER 6 L0
            // + GETSTATIC com/moi/test/B.timer : J
            // + INVOKESTATIC java/lang/System.currentTimeMillis ()J
            // + LSUB
            // + PUTSTATIC com/moi/test/B.timer : J
            mv.visitFieldInsn(
                Opcodes.GETSTATIC,
                owner,
                "timer",
                "J"
            )
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "java/lang/System",
                "currentTimeMillis",
                "()J"
            )
            mv.visitInsn(Opcodes.LSUB)
            mv.visitFieldInsn(
                Opcodes.PUTSTATIC,
                owner,
                "timer",
                "J"
            )
        }

        override fun visitInsn(opcode: Int) {
            // if opcode == (any)return or throw
            // add some codes before return or throw
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                // L2
                // LINENUMBER 8 L2
                // + GETSTATIC com/moi/test/B.timer : J
                // + INVOKESTATIC java/lang/System.currentTimeMillis ()J
                // + LADD
                // + PUTSTATIC com/moi/test/B.timer : J
                // L3
                // LINENUMBER 9 L3
                // RETURN (<- return is here.)
                mv.visitFieldInsn(
                    Opcodes.GETSTATIC,
                    owner,
                    "timer",
                    "J"
                )
                mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "java/lang/System",
                    "currentTimeMillis",
                    "()J"
                )
                mv.visitInsn(Opcodes.LADD)
                mv.visitFieldInsn(
                    Opcodes.PUTSTATIC,
                    owner,
                    "timer",
                    "J"
                )
            }
            // RETURN
            mv.visitInsn(opcode)
        }

        override fun visitMaxs(maxStack: Int, maxLocals: Int) {
            // MAXSTACK = 2, MAXLOCALS = 1 -> MAXSTACK = 4, MAXLOCALS = 1
            mv.visitMaxs(maxStack + 4, maxLocals)   // why (+) 4?
        }
    }
}
