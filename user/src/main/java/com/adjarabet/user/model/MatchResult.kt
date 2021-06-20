package com.adjarabet.user.model

sealed class MatchResult {
    object UserWon : MatchResult()
    class UserLost(
        val properWordSequence: String,
        val userWordSequence: String,
        val reason: LostReason
    ) :
        MatchResult()
}