package com.kaltura.playerdemo

import com.kaltura.playkit.PKMediaEntry

open class DemoItem {
    val name: String
    val id: String
    val pkMediaEntry: PKMediaEntry?

    constructor(name: String, id: String) {
        this.id = id
        this.name = name
        this.pkMediaEntry = null
    }

    constructor(name: String, id: String, pkMediaEntry: PKMediaEntry?) {
        this.id = id
        this.name = name
        this.pkMediaEntry = pkMediaEntry
    }


    override fun toString(): String {
        return "$name ➜ $id"
    }
}