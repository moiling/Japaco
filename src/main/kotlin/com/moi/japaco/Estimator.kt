package com.moi.japaco

import com.moi.japaco.data.Point

class Estimator {

    /*
     * repeat -> save once.
     * - eg: [a, b, c, b, c, b, d] -> [a, b, c, b, d]
     * coverage more than one path in circle -> coverage different paths.
     * - eg: [a, b, c, b, e, b, d] -> [a, b, c, b, d] + [a, b, e, b, d]
     */
    fun handleCircleCoverages(coverages: ArrayList<ArrayList<String>>, circlePoints: Set<String>) {
        val result = ArrayList<ArrayList<ArrayList<String>>>()

        coverages.forEach { c ->
            var newCoverages = ArrayList<ArrayList<String>>()
            val lastCoverages = ArrayList<ArrayList<String>>()
            lastCoverages.add(c)

            circlePoints.forEach { cp->
                newCoverages.clear()
                lastCoverages.forEach { current->
                    if (current.count { it == cp } > 2) {  // for each circle points (equals 2 means repeat once).
                        val indices = ArrayList<Int>()
                        val unitCircleSet = mutableSetOf<MutableList<String>>()

                        current.forEachIndexed { index, s -> if (s == cp) indices.add(index) }
                        for (i in 1 until indices.size) {
                            unitCircleSet.add(current.subList(indices[i - 1], indices[i]))
                        }
                        // use unit circle path to replace repeat circle path.
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

        println("-------")
        result.forEach {
            it.forEachIndexed { i, testCase ->
                println("Test Case $i:")
                testCase.forEach { text->
                    print("${Point(text).label}")
                    if (Point(text).label != "END") print("->")
                }
                println()
            }
        }
    }
}