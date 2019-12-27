package com.moi.japaco.data

data class Point(
    var owner:String?,
    var method:String?,
    var label: String?,
    var display: String?
) {
    constructor(text:String) : this(
        text.split('.')[0],
        text.split('.')[1].split(':')[0],
        text.split('.')[1].split(':')[1],
        ""
    )
}