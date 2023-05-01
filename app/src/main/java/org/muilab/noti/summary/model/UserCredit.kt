package org.muilab.noti.summary.model

data class UserCredit(
    var userId: String = "",
    var credit: Int = 20,
    var gender: String,
    var country: String,
    var birthYear: Int
)