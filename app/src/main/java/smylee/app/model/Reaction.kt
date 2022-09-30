package com.urfeed.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class Reaction(
    val isFeedOwner: Int?,
    val isTopicReactionOwner: Int?,
    val isVoted: Int?,
    val linkDesc: String?,
    val linkIcon: String?,
    val linkThumb: String?,
    val linkTitle: String?,
    val linkUrl: String?,
    val profilePic: String?,
    val reactionId: String?,
    val reactionThumb: String?,
    val reactionVideo: String?,
    val title: String?,
    val urlDownVoted: Int?,
    val urlId: String?,
    val urlUpVoted: Int?,
    val userId: String?,
    val userName: String?,
    val videoDuration: String?,
    val voteCount: Int?
) : Parcelable