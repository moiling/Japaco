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

    private fun getTargets(
        allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>,
        startMethod: String
    ): ArrayList<ArrayList<Point>> {
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
            it.forEach { index ->
                target.add(pointArray[index])
            }
            targets.add(target)
        }

        return targets
    }

    /*
     * Start with 'START' point, push 'START' to the stack.
     * REPEAT:
     * Whether the peek of stack element is 'END'?
     * - Not 'END'：Whether find next point in the adjacency list?(NEED: the point isn't in the stack and unvisited)
     *  - Found: Push the found point, set adjacency list[last peek][found point] as visited. Means this path is visited.
     *  - Not found: Pop, and set adjacency list[pop point][:] as unvisited.
     * - Is 'END: Save stack as a target path, then pop.
     * UNTIL stack is empty.
     *
     * The algorithm above can ONLY find main paths(without circle path).
     * Use the following algorithm to insert circle path:
     * - If found the next point 'C' in the adjacency list which is unvisited BUT is IN THE STACK.(Means 'C' is the circle point)
     *   - Find the position of 'C' in the stack,
     *   - save the array form this position to the peek(circle path),
     *   - then add 'C' at the end of array.(if 'C' is the peek, the array only has element 'C')
     * - Save these circle paths, such as 'Array<Array<Int>> => [<[A,B,J]>,<[C,J]>,<[D,K]>]'
     * - Save all circle points, such as [J,K]
     * - Last, find the position of 'C' in all target paths computed in above, insert all circle paths.
     */
    private fun findPaths(
        adjList: Array<ArrayList<Vertex>>,
        startIndex: Int,
        endIndex: Int
    ): ArrayList<ArrayList<Int>> {
        val paths = ArrayList<ArrayList<Int>>()
        val mainPaths = ArrayList<ArrayList<Int>>()  // without circle
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

    data class Vertex(var value: Int, var visited: Boolean)
}

fun main() {
    val className = "com/moi/test/testReturn/TestMultiReturn"
    Analyzer().analyze(className, "test")
}