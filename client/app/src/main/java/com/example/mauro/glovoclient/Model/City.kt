package com.example.mauro.glovoclient.Model

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class City: Serializable {
    var code: String = ""
    var name: String = ""
    var currency: String = ""
    var country_code: String = ""
    var enabled: Boolean = false
    var time_zone: String = ""
    var working_area: ArrayList<String> = ArrayList()
    var busy: Boolean = false
    var language_code: String = ""

    constructor()

    constructor(
        code: String,
        name: String,
        currency: String,
        country_code: String,
        enabled: Boolean,
        time_zone: String,
        working_area: ArrayList<String>,
        busy: Boolean,
        language_code: String
    ) {
        this.code = code
        this.name = name
        this.currency = currency
        this.country_code = country_code
        this.enabled = enabled
        this.time_zone = time_zone
        this.working_area = working_area
        this.busy = busy
        this.language_code = language_code
    }


}