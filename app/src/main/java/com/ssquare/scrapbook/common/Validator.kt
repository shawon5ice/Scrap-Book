package com.ssquare.scrapbook.common

import android.util.Patterns

class Validator {

    fun isValidEmail(target: String):Boolean {
        if(Patterns.EMAIL_ADDRESS.matcher(target).matches()){
            return true
        }
        return false
    }
}