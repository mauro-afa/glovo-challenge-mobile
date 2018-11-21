package com.example.mauro.glovoclient.model

import java.io.Serializable


class Cities: Serializable {
    var working_area: ArrayList<String> = ArrayList()
    var code: String = ""
    var name: String = ""
    var country_code: String = ""

    constructor()

    constructor(working_area: ArrayList<String>, code: String, name: String, country_code: String) {
        this.working_area = working_area
        this.code = code
        this.name = name
        this.country_code = country_code
    }
}
