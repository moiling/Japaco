package com.moi.japaco

class Estimator {

    /*
     * repeat -> save once.
     * - eg: [a, b, c, b, c, b, d] -> [a, b, c, b, d]
     * coverage more than one path in circle -> coverage different paths.
     * - eg: [a, b, c, b, e, b, d] -> [a, b, c, b, d] + [a, b, e, b, d]
     */
    // TODO ERROR CODE
    fun handleCircleCoverages(coverages: ArrayList<ArrayList<String>>) {
        val newCoverages = ArrayList<ArrayList<String>>()
        coverages.forEach { c ->
            val sameMap = mutableMapOf<String, Int>()   // <repeat label, repeat times>
            c.forEach { with(sameMap[it]) { sameMap.put(it, if (this == null) 1 else this + 1) } }
            // equals 2 means repeat once.
            sameMap.filter { it.value > 2 }.forEach { map ->
                val indices = ArrayList<Int>()
                c.forEachIndexed { index, s ->
                    if (s == map.key) indices.add(index)
                }
                val circlePathSet = mutableSetOf<MutableList<String>>()
                for (i in 1 until indices.size) {
                    circlePathSet.add(c.subList(indices[i - 1], indices[i]))
                }
                circlePathSet.forEach {
                    val path = ArrayList<String>()
                    path.addAll(c.subList(0, indices.first()))
                    path.addAll(it)
                    path.addAll(c.subList(indices.last(), c.lastIndex))
                    newCoverages.add(path)
                }
                println(indices)
            }
        }
        // newCoverages.addAll(coverages)
        println("-------")
        newCoverages.forEach { println(it) }
    }

}