package com.kaltura.playerdemo

import com.kaltura.playkit.PKMediaEntry

open class DemoItem (var name: String, var id: String, var pkMediaEntry: PKMediaEntry?) {
    constructor(name: String, id: String) : this(name, id, null)

    override fun toString(): String {
        return "$name ➜ $id"
    }
}