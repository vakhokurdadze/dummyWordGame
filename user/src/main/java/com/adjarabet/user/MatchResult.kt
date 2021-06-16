package com.adjarabet.user

sealed class MatchResult {
    object UserWon : MatchResult()
    class UserLost(val properWordSequence:String,val userWordSequence:String,val reason:LostReason):MatchResult()
}