package com.moi.japaco

import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import java.util.ArrayList

/**
 * allPaths: <method name, [path pairs]>
 *     eg: <"com/moi/Test.test", [<"L0", "L1">, <"L0", "l2">, ..., <"L4", "L5">]>
 */
class AnalyzePathClassAdapter constructor(version: Int, cv: ClassVisitor?, allPaths: MutableMap<String, ArrayList<Pair<String, String>>>) : ClassVisitor(version, cv) {
    private var owner: String? = null
    private var isInterface: Boolean = false
    private var version: Int = Opcodes.ASM5
    private var currentMethod: String? = null
    private var allPath: MutableMap<String, ArrayList<Pair<String, String>>>

    init {
        this.version = version
        this.allPath = allPaths
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
        return if (isInterface) mv else AnalyzePathMethodAdapter(version, mv, allPath)
    }

    inner class AnalyzePathMethodAdapter(version: Int, mv: MethodVisitor?, allPath: MutableMap<String, ArrayList<Pair<String, String>>>) : MethodVisitor(version, mv) {

        private var paths: ArrayList<Pair<String, String>> = ArrayList()
        private var currentLabel: Label? = null

        /**
         * 空：如果currentLabel为null，将新label设置为currentLabel，其他什么都不做
         * 开始：visitCode时，将currentLabel置为start
         * 顺接：有新的label了，和currentLabel组成一个pair，然后替换掉currentLabel
         * 跳转：如果是非GOTO的跳转，就用跳转的label和currentLabel组成一个pair，不替换，因为还有一条分支要顺接
         * GOTO: 如果是GOTO跳转，就用跳转的label和currentLabel组成一个pair，并清空currentLabel
         * TABLESWITCH： 如果是SWITCH，用SWITCH的dflt和Labels的Label和currentLabel组成多个pairs，并用dflt替换currentLabel
         * RETURN: 如果是RETURN，将currentLabel保存到一个returnLabels中，清空currentLabel
         * 执行函数：如果执行函数，则将函数名作为label和顺接一样操作（判断是否是待测classPath中的函数），之后做多个图拼接
         * 结束：如果visitEnd了，把returnLabels中的label和end标签组成多个pairs
         */

        private fun addLabel(mv: MethodVisitor, text: String) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "com/moi/test/Data", "array", "Ljava/util/ArrayList;")
            mv.visitLdcInsn(text)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false)
            mv.visitInsn(Opcodes.POP)
        }

        override fun visitLabel(label: Label?) {
            mv.visitLabel(label)
            addLabel(mv, "$currentMethod.$label")
        }

        override fun visitCode() {
            mv.visitCode()
            addLabel(mv, "$currentMethod.start")
        }

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                addLabel(mv, "$currentMethod.end")
            }
            mv.visitInsn(opcode)    // RETURN
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, p4: Boolean) {
            mv.visitMethodInsn(opcode, owner, name, desc, p4)
        }

        override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
            mv.visitTableSwitchInsn(min, max, dflt, *labels)
        }

        override fun visitJumpInsn(opcode: Int, label: Label?) {
            mv.visitJumpInsn(opcode, label)
        }

        override fun visitEnd() {
            mv.visitEnd()
        }
    }
}