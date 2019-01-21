package com.nono.multistation.util

object LogHelper {

    private var iDebug = false
    private var iError = true

    fun d(str: String) {
        if (iDebug) {
            System.out.println(str)
        }
    }

    fun e(str: String) {
        if (iError) {
            System.err.println(str)
        }
    }
}