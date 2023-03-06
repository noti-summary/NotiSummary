package edu.mui.noti.noti.util

// import this package, and can directly use TAG in Log function
val Any.TAG: String
    get() {
        val tag = javaClass.simpleName
        return if (tag.length <= 23) tag else tag.substring(0, 23)
    }