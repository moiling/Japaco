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

    override fun toString(): String {
        return "$owner.$method:$label"
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Point -> this.owner == other.owner && this.method == other.method && this.label == other.label
            is String -> this.toString() == other
            else -> super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = owner?.hashCode() ?: 0
        result = 31 * result + (method?.hashCode() ?: 0)
        result = 31 * result + (label?.hashCode() ?: 0)
        return result
    }
}