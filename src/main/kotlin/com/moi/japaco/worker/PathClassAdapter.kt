package com.moi.japaco.worker

import com.moi.japaco.config.CLASS_DATA
import com.moi.japaco.config.LABEL_END
import com.moi.japaco.config.LABEL_INVOKE_METHOD
import com.moi.japaco.config.LABEL_START
import com.moi.japaco.data.Point
import jdk.internal.org.objectweb.asm.ClassVisitor
import jdk.internal.org.objectweb.asm.Label
import jdk.internal.org.objectweb.asm.MethodVisitor
import jdk.internal.org.objectweb.asm.Opcodes
import java.util.*

/*
 * allEdges: <method name, [path pairs]>
 *     eg: <"com/moi/Test.test", [<"L0", "L1">, <"L0", "l2">, ..., <"L4", "L5">]>
 */
class PathClassAdapter constructor(
    private var version: Int,
    cv: ClassVisitor?,
    private var allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>,  // out
    private var classNames: MutableList<String>
) : ClassVisitor(version, cv) {

    private var owner: String? = null
    private var isInterface: Boolean = false
    private var currentMethod: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        owner = name
        isInterface = (access and Opcodes.ACC_INTERFACE) != 0   // and -> &
        cv.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor? {
        currentMethod = name
        val mv: MethodVisitor? = cv.visitMethod(access, name, desc, signature, exceptions)
        return if (isInterface) mv else PathMethodAdapter(version, mv, allEdges)
    }

    /*
     * Code Analyze:
     * LABEL_START (when visitCode existed): set currentLabel as "LABEL_START".
     * NEW LABEL (if a new label is encountered):
     * - if currentLabel isn't null: pair the new label with the currentLabel，and replace the currentLabel. (in-order)
     * - if currentLabel is null: set currentLabel as label. (new branch)
     * JUMP:
     * - isn't GOTO: pair the jump target label with the currentLabel, and pair the "!jump target label"(means not jump) with the currentLabel.
     *               set currentLabel as "!jump target label"
     * - GOTO: pair the jump target label with the currentLabel, and clear the currentLabel.
     * SWITCH: use the default and other cases target label of switch to form multiple pairs with the currentLabel, and clear the currentLabel.
     * RETURN: pair the "LABEL_END" label with the currentLabel, and clear the currentLabel.
     * INVOKE: if method should be test(determine package name), pair the method name and the currentLabel, and replace the currentLabel.
     */
    inner class PathMethodAdapter(
        version: Int,
        mv: MethodVisitor?,
        private var allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>  // out
    ) : MethodVisitor(version, mv) {

        private var paths: ArrayList<Pair<Point, Point>> = ArrayList()
        private var displayNum: Int = 0
        private var currentPoint: Point? = null
        private var passedPoints: ArrayList<Point> = ArrayList()

        private fun addStub(mv: MethodVisitor, text: String) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, CLASS_DATA, "array", "Ljava/util/ArrayList;")
            mv.visitLdcInsn(text)
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false)
            mv.visitInsn(Opcodes.POP)
        }

        private fun getPoint(label: String): Point {
            if (!label.startsWith(LABEL_INVOKE_METHOD)) {
                val pointIndex = passedPoints.indexOfFirst { it.label == label }
                if (pointIndex != -1) {
                    return passedPoints[pointIndex]
                }
            }
            val display = when {
                label == LABEL_START -> LABEL_START
                label == LABEL_END -> LABEL_END
                label.startsWith(LABEL_INVOKE_METHOD) -> "I${displayNum++}"
                else -> "L${displayNum++}"
            }

            val newPoint = if (label.startsWith(LABEL_INVOKE_METHOD)) {
                Point(label.split('.')[1], label.split('.')[2], LABEL_INVOKE_METHOD, display)
            } else {
                Point(owner, currentMethod, label, display)
            }
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

            // stub
            addStub(mv, "$owner.$currentMethod:$label")
        }

        override fun visitCode() {
            mv.visitCode()
            currentPoint = getPoint(LABEL_START)

            // stub
            addStub(mv, "$owner.$currentMethod:$LABEL_START")
        }

        override fun visitInsn(opcode: Int) {
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
                addPair(getPoint(LABEL_END))
                currentPoint = null

                // stub
                addStub(mv, "$owner.$currentMethod:$LABEL_END")
            }
            mv.visitInsn(opcode)  // RETURN
        }

        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, p4: Boolean) {
            mv.visitMethodInsn(opcode, owner, name, desc, p4)
            if (owner in classNames) {
                val newPoint = getPoint("$LABEL_INVOKE_METHOD.$owner.$name")
                addPair(newPoint)
                currentPoint = newPoint
            }
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
            val newPoint = addPair(getPoint("!$label"))

            if (opcode == Opcodes.GOTO) {
                currentPoint = null
            } else {
                currentPoint = newPoint
            }
            // stub
            addStub(mv, "$owner.$currentMethod:!$label")
        }

        override fun visitEnd() {
            allEdges["$owner.$currentMethod"] = paths
            mv.visitEnd()
        }
    }
}