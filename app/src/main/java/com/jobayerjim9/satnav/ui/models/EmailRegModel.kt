package com.jobayerjim9.satnav.ui.models

class EmailRegModel(email: String = "", uid: String = "", signUpType: String = "") {
    val email: String = email
    val uid: String = uid
    val signUpType: String = signUpType
    val subscribed = false
    val approved = false
    val type: String = Constants.TYPE_USER
    val institute: String = "Select Institute"
}