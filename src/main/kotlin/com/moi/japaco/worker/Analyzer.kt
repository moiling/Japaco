package com.moi.japaco.worker

import com.moi.japaco.config.LABEL_END
import com.moi.japaco.config.LABEL_INVOKE_METHOD
import com.moi.japaco.config.LABEL_START
import com.moi.japaco.data.Point
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class Analyzer(
    private val startMethod: String,
    private val startClass: String,
    private val allEdges: MutableMap<String, ArrayList<Pair<Point, Point>>>
) {
    private val passedEdges = mutableMapOf<String, ArrayList<Pair<Point, Point>>>()
    private val circledPoints: HashSet<Int> = HashSet()
    private var pointArray: Array<Point> = emptyArray()
    private var targets: ArrayList<ArrayList<Point>> = ArrayList()
    private val searchedMethod: ArrayList<String> = ArrayList()

    fun analyze() {
        findPassedEdges("$startClass.$startMethod")  // out: passedEdges
        // passedEdges.putAll(allEdges)
        targets = findTargets()
    }

    /*
     * Method A:L0->#INVOKE#:Method B->Method A:L1
     * =>
     * Method A:L0->Method B:LABEL_START->Method B:...->Method B:LABEL_END->Method A:L1
     * (Method B is in the class path.)
     */
    private fun findPassedEdges(method: String): ArrayList<Pair<Point, Point>>? {
        val edges: ArrayList<Pair<Point, Point>> = ArrayList()

        // method is ignored.
        if (allEdges[method] == null) return null

        edges.addAll(allEdges[method]!!)
        // for each invoke.
        allEdges[method]!!.filter { it.first.label!! == LABEL_INVOKE_METHOD }.forEach { p ->
            val invokeMethod = "${p.first.owner}.${p.first.method}"
            val invokeMethodEdges: ArrayList<Pair<Point, Point>>?

            // if the method is be searched -> get from passed edges.
            if (searchedMethod.indexOfFirst { it == invokeMethod } == -1) {
                invokeMethodEdges = findPassedEdges(invokeMethod)
                invokeMethodEdges?.let { searchedMethod.add(invokeMethod) }
            } else {
                invokeMethodEdges = passedEdges[invokeMethod]!!
            }

            invokeMethodEdges?.let { ime ->
                val invokeInFirst = ArrayList<Int>()
                val invokeInSecond = ArrayList<Int>()
                val invokeStart = ime.find { it.first.label == LABEL_START }!!.first
                val invokeEnd = ime.find { it.second.label == LABEL_END }!!.second
                // change invoke start and end.
                for (e in edges.withIndex()) {
                    if (e.value.first == p.first && e.value.first.display == p.first.display) {
                        invokeInFirst.add(e.index)
                    } else if (e.value.second == p.first && e.value.second.display == p.first.display) {
                        invokeInSecond.add(e.index)
                    }
                }

                invokeInFirst.forEach { f ->
                    invokeInSecond.forEach { s ->
                        edges[f] = Pair(invokeEnd, edges[f].second)
                        edges[s] = Pair(edges[s].first, invokeStart)
                    }
                }
            }
        }

        passedEdges[method] = edges
        return edges
    }

    private fun findTargets(): ArrayList<ArrayList<Point>> {
        // get number of points.
        val pointSet = HashSet<Point>()
        val targets = ArrayList<ArrayList<Point>>()

        passedEdges.forEach {
            it.value.forEach { p ->
                pointSet.add(p.first)
                pointSet.add(p.second)
            }
        }
        pointArray = pointSet.toTypedArray()

        // find LABEL_START and LABEL_END.
        val startIndex = pointArray.indexOfFirst { it.label == LABEL_START && it.owner == startClass && it.method == startMethod }
        val endIndex = pointArray.indexOfFirst { it.label == LABEL_END && it.owner == startClass && it.method == startMethod }

        // create adjacency list(save value of pointArray, Bool:is visited).
        val adjList = Array<ArrayList<Vertex>>(pointArray.size) { ArrayList() }

        passedEdges.forEach {
            it.value.forEach { p ->
                // replace repeat
                if (adjList[pointArray.indexOf(p.first)].find { a -> a.value == pointArray.indexOf(p.second) } == null) {
                    adjList[pointArray.indexOf(p.first)].add(
                        Vertex(
                            pointArray.indexOf(p.second),
                            false
                        )
                    )
                }
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
     * Start with 'LABEL_START' point, push 'LABEL_START' to the stack.
     * REPEAT:
     * Whether the peek of stack element is 'LABEL_END'?
     * - Not 'LABEL_END'：Whether find next point in the adjacency list?(NEED: the point isn't in the stack and unvisited)
     *  - Found: Push the found point, set adjacency list[last peek][found point] as visited. Means this path is visited.
     *  - Not found: Pop, and set adjacency list[pop point][:] as unvisited.
     * - Is 'LABEL_END: Save stack as a target path, then pop.
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

    fun getCircledPointsStr(): HashSet<String> {
        val result = HashSet<String>()
        if (pointArray.isNotEmpty()) {
            circledPoints.forEach {
                result.add("${pointArray[it]}")
            }
        }
        return result
    }

    fun getTargets(): ArrayList<ArrayList<Point>> {
        return this.targets
    }

    fun getPassedEdges(): MutableMap<String, ArrayList<Pair<Point, Point>>> {
        return this.passedEdges
    }

    fun getPointArray(): Array<Point> {
        return this.pointArray
    }

    data class Vertex(var value: Int, var visited: Boolean)
}