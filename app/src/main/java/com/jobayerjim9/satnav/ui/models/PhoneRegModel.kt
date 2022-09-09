package com.jobayerjim9.satnav.ui.models

class PhoneRegModel(phoneNumber: String = "", uid: String = "") {
    val phoneNumber: String = phoneNumber
    val uid: String = uid
    val subscribed = false
    val signUpType: String = "phone"
    val approved = false
    val type: String = Constants.TYPE_USER

}