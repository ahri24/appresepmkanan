package com.tugasmobile.appresepmakanan.model


import io.realm.RealmObject
import java.io.Serializable

class ModelRecipes : RealmObject(), Serializable {
    var strTitleResep: String? = null
    var strThumbnail: String? = null
    var strKeyResep: String? = null
    var strTimes: String? = null
    var strPortion: String? = null
    var strDificulty: String? = null
}