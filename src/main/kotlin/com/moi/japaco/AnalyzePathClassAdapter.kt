package com.moi.japaco

import com.moi.japaco.config.END
import com.moi.japaco.config.START
import com.moi.japaco.data.Point
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import java.util.*

/*
 * allPaths: <method name, [path pairs]>
 *     eg: <"com/moi/Test.test", [<"L0", "L1">, <"L0", "l2">, ..., <"L4", "L5">]>
 */
class AnalyzePathClassAdapter constructor(
    private var version: Int,
    cv: ClassVisitor?,
    private var allPaths: MutableMap<String, ArrayList<Pair<Point, Point>>>
) : ClassVisitor(version, cv) {

    private var owner: String? = null
    private var isInterface: Boolean = false
    private var currentMethod: String? = null

    override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
        owner = name
        isInterface = (access and Opcodes.ACC_INTERFACE) != 0   // and -> &
        cv.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
        currentMethod = name
        // val newDesc = desc?.replaceBefore(')', "Ljava/util/ArrayList;")
        val mv: MethodVisitor? = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (isInterface) mv else AnalyzePathMethodAdapter(version, mv, allPaths)
    }

    inner class AnalyzePathMethodAdapter(
        version: Int,
        mv: MethodVisitor?,
        private var allPaths: MutableMap<String, ArrayList<Pair<Point, Point>>>
    ) : MethodVisitor(version, mv) {

        private var paths: ArrayList<Pair<Point, Point>> = ArrayList()
        private var displayNum: Int = 0
        private var currentPoint: Point? = null
        private var passedPoints: ArrayList<Point> = ArrayList()

        /*
         * START (when visitCode existed): set currentLabel as "START".
         * NEW LABEL (if a new label is encountered):
         * - if currentLabel isn't null: pair the new label with the currentLabel，and replace the currentLabel. (in-order)
         * - if currentLabel is null: set currentLabel as label. (new branch)
         * JUMP:
         * - isn't GOTO: pair the jump target label with the currentLabel. DO NOT replace the currentLabel. (another branch will run in sequence)
         * - GOTO: pair the jump target label with the currentLabel, and clear the currentLabel.
         * SWITCH: use the default and other cases target label of switch to form multiple pairs with the currentLabel, and clear the currentLabel.
         * RETURN: pair the "END" label with the currentLabel, and clear the currentLabel.
         * TODO INVOKE: if method should be test(determine package name), pair the method name and the currentLabel, and replace the currentLabel.
         */

        private fun getPoint(label: String?): Point {
            val pointIndex = passedPoints.indexOfFirst { it.label == label }
            if (pointIndex != -1) {
                return passedPoints[pointIndex]
            }
            val display = if (label == START) START else if (label == END) END else "L${displayNum++}"
            val newPoint = Point(owner, currentMethod, label, display)
            passedPoints.add(newPoint)
            return newPoint
        }

        private fun addPair(newPoint: Point): Point {
            currentPoint?.let { paths.add(Pair(it, newPoint)) }
            return newPoint
        }

        override fun visitLabel(label: Label?) {
            mv.visitLabel(label)
            val newPoint = getPoint("$label")
            addPair(newPoint)
            currentPoint = newPoint
        }

        override fun visitCode() {
            mv.visitCode()
            currentPoint = getPoint(START)
        }

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                addPair(getPoint(END))
                currentPoint = null
            }
            mv.visitInsn(opcode)    // RETURN
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, p4: Boolean) {
            mv.visitMethodInsn(opcode, owner, name, desc, p4)
        }

        override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label?, vararg labels: Label?) {
            mv.visitTableSwitchInsn(min, max, dflt, *labels)
            addPair(getPoint("$dflt"))
            labels.forEach { addPair(getPoint("$it")) }
            currentPoint = null
        }

        override fun visitLookupSwitchInsn(dflt: Label?, keys: IntArray?, labels: Array<out Label>?) {
            mv.visitLookupSwitchInsn(dflt, keys, labels)
            addPair(getPoint("$dflt"))
            labels?.forEach { addPair(getPoint("$it")) }
            currentPoint = null
        }

        override fun visitJumpInsn(opcode: Int, label: Label?) {
            mv.visitJumpInsn(opcode, label)
            addPair(getPoint("$label"))

            if (opcode == Opcodes.GOTO) {
                currentPoint = null
            }
        }

        override fun visitEnd() {
            allPaths["$owner.$currentMethod"] = paths
            mv.visitEnd()
        }
    }
}