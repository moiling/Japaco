package com.moi.japaco

class Estimator {

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
    fun handleCircleCoverages(
        coverages: ArrayList<ArrayList<String>>,
        circlePoints: Set<String>
    ): ArrayList<ArrayList<ArrayList<String>>> {
        val result = ArrayList<ArrayList<ArrayList<String>>>()

        // for each test case.
        coverages.forEach { c ->
            // Because of one test case may coverage more than one target,
            // the number of coverages will changed for each circle points.
            // So need use a extra list to save last coverages.
            // eg: [a,'b',c,'b',d,'b','e','e','e',f]
            //     First circle point is 'b', the last is origin list [a,'b',c,'b',d,'b','e','e','e',f].
            //     Run algorithm, the new is [a,'b',c,'b','e','e','e',f] + [a,'b',d,'b','e','e','e',f].
            //     Second circle point is 'e', set last as new [a,'b',c,'b','e','e','e',f] + [a,'b',d,'b','e','e','e',f]
            //     Clear new, run algorithm, the new will be set as  [a,'b',c,'b','e','e',f] + [a,'b',d,'b','e','e',f]
            //     Finished.
            var newCoverages = ArrayList<ArrayList<String>>()
            val lastCoverages = ArrayList<ArrayList<String>>()
            lastCoverages.add(c)

            circlePoints.forEach { cp ->
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
}