package com.jobayerjim9.satnav.ui.models

import java.io.Serializable

class TelemetryData : Serializable {
    var satelliteName: String = ""
    var satelliteMap: String = ""
    var satelliteDescription: String = ""
    var status: String = ""
    var launchDate: String = ""
    var image: String = ""
    var tle: TLE = TLE()
    var lastTelemetry: LastTelemetry = LastTelemetry()
    var pastTelemetries: ArrayList<PastTelemetry> = ArrayList()


    class TLE : Serializable {
        var satelliteName: String = ""
        var line1: String = ""
        var line2: String = ""
    }

    class LastTelemetry : Serializable {
        var averageExternalTemp: String = ""
        var averageInternalTemp: String = ""
        var averagePowerConsumption: String = ""
        var batteryAverageTemp: String = ""
        var batteryChargingStat: String = ""
        var batteryCurrentCapacity: String = ""
        var batteryDischargingStat: String = ""
        var batteryMaxCapacity: String = ""
        var batteryTemp: String = ""
        var batteryVoltage: String = ""
        var data: String = ""
        var gyro: String = ""
        var operationMode: String = ""
        var powerBalanceRatio: String = ""
        var rssi: String = ""
        var solarPanelVoltages: String = ""
        var sunIllumination: String = ""
        var txPower: String = ""
    }

    class PastTelemetry : Serializable {
        var date: String = ""
        var mode: String = ""
        var receivedBy: String = ""
        var averageExternalTemp: String = ""
        var averageInternalTemp: String = ""
        var averagePowerConsumption: String = ""
        var batteryAverageTemp: String = ""
        var batteryChargingStat: String = ""
        var batteryCurrentCapacity: String = ""
        var batteryDischargingStat: String = ""
        var batteryMaxCapacity: String = ""
        var batteryTemp: String = ""
        var batteryVoltage: String = ""
        var data: String = ""
        var gyro: String = ""
        var operationMode: String = ""
        var powerBalanceRatio: String = ""
        var rssi: String = ""
        var solarPanelVoltages: String = ""
        var sunIllumination: String = ""
        var txPower: String = ""
    }

}