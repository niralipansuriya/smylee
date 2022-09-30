package com.urfeed.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Topic(
    val description: String?,
    val sources: String?,
    val subtitle: String?,
    val thumb: String?,
    val title: String
) : Parcelable

/*val UrlVoteCount: Int?,
    val feedIcon: String?,
    val feedId: String?,
    val feedName: String?,
    val isFeedOwner: Int?,
    val isTopicReactionOwner: Int?,
    val isVoted: Int?,
    val linkDesc: String?,
    val linkIcon: String?,
    val linkThumb: String?,
    val linkTitle: String?,
    val linkUrl: String?,
    val profilePic: String?,
    val reaction: ArrayList<Reaction>?,
    val title: String?,
    val topicId: String?,
    val topicThumb: String?,
    val topicVideo: String?,
    val urlDownVoted: Int?,
    val urlId: String?,
    val urlUpVoted: Int?,
    val userId: String?,
    val userName: String?,
    val videoDuration: String?,
    val voteCount: Int?,
    var proxyUrl:String?*/