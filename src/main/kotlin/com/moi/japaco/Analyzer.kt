package com.moi.japaco

import com.moi.japaco.config.END
import com.moi.japaco.config.START
import com.moi.japaco.data.Point
import jdk.internal.org.objectweb.asm.ClassReader
import jdk.internal.org.objectweb.asm.ClassWriter
import jdk.internal.org.objectweb.asm.Opcodes
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Analyzer {

    fun analyze(className: String, startMethod: String) {
        val allEdges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()
        val cr = ClassReader(className)
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
        val classAdapter = AnalyzePathClassAdapter(Opcodes.ASM5, cw, allEdges)
        cr.accept(classAdapter, ClassReader.SKIP_DEBUG)

        val targets = getTargets(allEdges, "$className.$startMethod")

        Reporter().report(allEdges)
    }

    private fun getTargets(allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>, startMethod: String):ArrayList<ArrayList<Point>> {
        // get number of points.
        val pointSet = HashSet<Point>()
        val targets = ArrayList<ArrayList<Point>>()
        allEdges.filter { it.key == startMethod }.forEach {
            it.value.forEach { p ->
                pointSet.add(p.first)
                pointSet.add(p.second)
            }
        }
        val pointArray = pointSet.toTypedArray()

        // find START and END.
        val startIndex = pointArray.indexOfFirst { it.label == START }
        val endIndex = pointArray.indexOfFirst { it.label == END }

        // create adjacency list(save value of pointArray, Bool:is visited).
        val adjList = Array<ArrayList<Vertex>>(pointArray.size) { ArrayList() }

        allEdges.filter { it.key == startMethod }.forEach {
            it.value.forEach { p ->
                adjList[pointArray.indexOf(p.first)].add(Vertex(pointArray.indexOf(p.second), false))
            }
        }

        val paths = findPaths(adjList, startIndex, endIndex)
        paths.forEach {
            val target = ArrayList<Point>()
            it.forEach { index->
                target.add(pointArray[index])
            }
            targets.add(target)
        }

        return targets
    }

    /*
     * 从START的节点开始，将START入栈
     * REPEAT:
     * 当前栈顶元素是否是END
     * - 不是END：能否在邻接表中找到栈顶元素的下一个节点(不在栈中且visited为False的节点)
     *  - 能找到: 入栈，并将邻接表中当前栈顶元素对应的该节点的visited置为True，表示这条路当前访问过
     *  - 找不到: 出栈，并把出栈的节点在邻接表中的所有下个节点的visited全置为False
     * - 是END: 将栈输出，保存当前路径，出栈
     * 直到栈为空时退出循环
     *
     * 以上过程将非循环部分的路径找到，然后通过以下方式插入循环部分
     * 如果找到栈顶元素的下一个节点'J'(visited为False，但在栈中)，表示遇到了循环
     * 此时在栈中找'J'的位置，保存从'J'后一个元素到栈顶的数组，并在数组最后添加'J'(如果'J'就是栈顶，则数组中只有'J')
     * 将这些数组保存起来，如Array<Array<Int>> => [<[A,B,J]>,<[C,J]>,<[D,K]>]
     * 并保存有多少循环节[J,K]
     * 然后在上面找到的所有路径上，找到'J'的位置，循环插入生成新的路径
     */
    private fun findPaths(adjList: Array<ArrayList<Vertex>>, startIndex: Int, endIndex: Int):ArrayList<ArrayList<Int>> {
        val paths = ArrayList<ArrayList<Int>>()
        var mainPaths = ArrayList<ArrayList<Int>>() // without circle
        val circledPaths = HashSet<ArrayList<Int>>()
        val circledPoints = HashSet<Int>()
        val stack = Stack<Int>()
        stack.push(startIndex)

        while (stack.isNotEmpty()) {

            if (stack.peek() == endIndex) {
                mainPaths.add(stack.toMutableList() as ArrayList<Int>)
                stack.pop()
                continue
            }

            var nextIndex = adjList[stack.peek()].indexOfFirst { !it.visited && stack.search(it.value) == -1 }
            if (nextIndex != -1) {  // found
                val next = adjList[stack.peek()][nextIndex]
                next.visited = true
                stack.push(next.value)
                continue
            }

            nextIndex = adjList[stack.peek()].indexOfFirst { !it.visited && stack.search(it.value) != -1 }
            if (nextIndex != -1) {  // found circled
                val next = adjList[stack.peek()][nextIndex]
                val stackIndex = stack.toMutableList().indexOf(next.value)
                val path = ArrayList<Int>()
                for (i in stackIndex + 1 until stack.size) {
                    path.add(stack.toMutableList()[i])
                }
                path.add(next.value)
                circledPaths.add(path)
                circledPoints.add(next.value)
            }
            adjList[stack.pop()].forEach { it.visited = false }

        }

        paths.addAll(mainPaths)

        circledPoints.forEach { point ->
            mainPaths.filter { it.indexOf(point) != -1 }.forEach { main ->
                circledPaths.filter { it.last() == point }.forEach {
                    val circledPath = ArrayList<Int>()
                    circledPath.addAll(main)
                    circledPath.addAll(circledPath.indexOf(point) + 1, it)
                    paths.add(circledPath)
                }
            }
            mainPaths.clear()
            mainPaths.addAll(paths)
        }
        return paths
    }

    data class Vertex(var value:Int, var visited:Boolean)
}

fun main() {
    val className = "com/moi/test/testReturn/TestMultiReturn"
    Analyzer().analyze(className, "test")
}