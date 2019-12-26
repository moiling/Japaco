package com.moi.japaco

import com.moi.japaco.data.Point
import java.util.ArrayList

class Reporter {

    fun report(allPaths: MutableMap<String, ArrayList<Pair<Point, Point>>>) {
        println("graph TB")
        allPaths.forEach {
            println("subgraph ${it.key.split('.')[1]}")
            it.value.forEach { p->
                // <init> -> #lt#init#gt#
                val fMethod = p.first.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                val sMethod = p.second.method?.replace("<", "#lt#")?.replace(">", "#gt#")
                println("${p.first.owner}.$fMethod:${p.first.label}((${p.first.display}))-->" +
                        "${p.second.owner}.$sMethod:${p.second.label}((${p.second.display}))")
            }
            println("end")
        }
    }
}