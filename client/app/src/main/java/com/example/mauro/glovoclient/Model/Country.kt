package com.example.mauro.glovoclient.model

import java.io.Serializable

class Country: Serializable {

    var code: String = ""
    var name: String = ""

    constructor()

    constructor(code: String, name: String) {
        this.code = code
        this.name = name
    }
}