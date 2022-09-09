package com.jobayerjim9.satnav.ui.models

data class UserProfile(
    var email: String = "",
    var name: String = "",
    var photo: String = "",
    var phoneNumber: String = "",
    var institute: String = "",
    val signUpType: String = "",
    var gender: String = "",
    var city: String = "",
    var state: String = "",
    var country: String = "",
    var uid: String = "",
    var subscribed: Boolean = false,
    var approved: Boolean = false,
    var type: String = "",
    var expireSubscription: String = "",
    var subsType: String = "",
)