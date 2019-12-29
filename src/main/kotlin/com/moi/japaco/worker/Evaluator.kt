package com.moi.japaco.worker

import com.moi.japaco.data.Point

class Evaluator(
    private val analyzer: Analyzer,
    private val suiteCoverages: ArrayList<ArrayList<String>>
) {
    private var targetCoverageState: ArrayList<Pair<ArrayList<Point>, Int>> = ArrayList()  // int -> coverage times
    private var suiteCoverageState: ArrayList<ArrayList<Pair<ArrayList<Point>, Int>>> = ArrayList()  // int -> coverage target index

    fun evaluate() {
        val handledSuiteCoverages = handleCircleCoverages()
        val targetCoverageTimes = Array(analyzer.getTargets().size) { 0 }

        handledSuiteCoverages.forEach {
            val caseCoverageState = ArrayList<Pair<ArrayList<Point>, Int>>()
            it.forEach { s ->
                for (t in analyzer.getTargets().withIndex()) {
                    if (s deepEquals t.value) {
                        caseCoverageState.add(Pair(t.value, t.index))
                        targetCoverageTimes[t.index]++
                        break
                    }
                }
            }
            suiteCoverageState.add(caseCoverageState)
        }

        // init target coverage state
        analyzer.getTargets().forEachIndexed { index, it ->
            targetCoverageState.add(Pair(it, targetCoverageTimes[index]))
        }
    }

    /*
     * repeat -> save once.
     * eg: [a, 'b', c, 'b', c, 'b', d] -> [a, 'b', c, 'b', d]
     * coverage more than one path in circle -> coverage different paths.
     * eg: [a, 'b', c, 'b', e, 'b', d] -> [a, 'b', c, 'b', d] + [a, 'b', e, 'b', d]
     *
     * '*' means the circle point.
     *
     * RETURN: the coverageS of each test case.
     * eg: Case 0: [a, b, c, b, d], [a, b, e, b, d]  // One test case may coverage more than one target.
     *     Case 1: [a, b, d]
     *     Case 2: [a, b, c, b, d]
     */
    private fun handleCircleCoverages(): ArrayList<ArrayList<ArrayList<String>>> {
        val result = ArrayList<ArrayList<ArrayList<String>>>()
        val circledPoints = analyzer.getCircledPointsStr().toSet()
        // for each test case.
        suiteCoverages.forEach { c ->
            // Because of one test case may coverage more than one target,
            // the number of suiteCoverages will changed for each circle points.
            // So need use a extra list to save last suiteCoverages.
            // eg: [a,'b',c,'b',d,'b','e','e','e',f]
            //     First circle point is 'b', the last is origin list [a,'b',c,'b',d,'b','e','e','e',f].
            //     Run algorithm, the new is [a,'b',c,'b','e','e','e',f] + [a,'b',d,'b','e','e','e',f].
            //     Second circle point is 'e', set last as new [a,'b',c,'b','e','e','e',f] + [a,'b',d,'b','e','e','e',f]
            //     Clear new, run algorithm, the new will be set as  [a,'b',c,'b','e','e',f] + [a,'b',d,'b','e','e',f]
            //     Finished.
            var newCoverages = ArrayList<ArrayList<String>>()
            val lastCoverages = ArrayList<ArrayList<String>>()
            lastCoverages.add(c)

            circledPoints.forEach { cp ->
                newCoverages.clear()
                lastCoverages.forEach { current ->
                    if (current.count { it == cp } > 2) {  // for each circle points (equals 2 means repeat once).
                        val indices = ArrayList<Int>()
                        val unitCircleSet = mutableSetOf<MutableList<String>>()
                        // find unit circle path.
                        current.forEachIndexed { index, s -> if (s == cp) indices.add(index) }
                        for (i in 1 until indices.size) {
                            unitCircleSet.add(current.subList(indices[i - 1], indices[i]))
                        }
                        // Use unit circle path to replace repeat circle path.
                        // eg: repeat circle:['a', b, 'a', b, 'a'] -> unit circle:['a', b, 'a']
                        //     repeat circle:['a', b, 'a', c, 'a'] -> unit circle:['a', b, 'a'] + ['a', c, 'a']
                        unitCircleSet.forEach {
                            val path = ArrayList<String>()
                            path.addAll(current.subList(0, indices.first()))
                            path.addAll(it)
                            path.addAll(current.subList(indices.last(), current.lastIndex + 1))
                            newCoverages.add(path)
                        }
                    } else {  // didn't run in circle or just run once.
                        newCoverages.add(current)
                    }
                }
                lastCoverages.clear()
                lastCoverages.addAll(newCoverages)
            }
            result.add(newCoverages)
            newCoverages = ArrayList()
        }

        return result
    }

    private infix fun ArrayList<String>.deepEquals(collection: ArrayList<Point>?): Boolean {
        if (collection == null) return false
        this.forEachIndexed { index, t ->
            if (!collection[index].equals(t)) {
                return false
            }
        }
        return true
    }

    fun getTargetCoverageState(): ArrayList<Pair<ArrayList<Point>, Int>> {
        return this.targetCoverageState
    }

    fun getSuiteCoverageState(): ArrayList<ArrayList<Pair<ArrayList<Point>, Int>>> {
        return this.suiteCoverageState
    }

    fun getOriginalSuitePath(): ArrayList<ArrayList<Point>> {
        val result = ArrayList<ArrayList<Point>>()
        suiteCoverages.forEach { case ->
            val caseCoverage = ArrayList<Point>()

            case.forEach { point ->
                val p = analyzer.getPointArray().find { it.equals(point) }
                p?.let { caseCoverage.add(it) }
            }

            result.add(caseCoverage)
        }
        return result
    }
}