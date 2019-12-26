package com.moi.japaco.data

data class Point(
    var owner:String?,
    var method:String?,
    var label: String?,
    var display: String?
) {
    public constructor(text:String) : this(
        text.split('.')[0],
        text.split('.')[0].split(':')[0],
        text.split('.')[0].split(':')[1],
        ""
    )
}