package com.itami.calorie_tracker.authentication_feature.data.remote.request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleLoginRequest(
    val googleIdToken: String,
)