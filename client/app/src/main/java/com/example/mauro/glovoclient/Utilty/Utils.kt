package com.example.mauro.glovoclient.Utilty

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat

class Utils {

    companion object {
        fun checkPermission(context: Context, sPermissionName: String) : Boolean {
            val permissionState = ActivityCompat.checkSelfPermission(context,
                sPermissionName)
            return permissionState == PackageManager.PERMISSION_GRANTED
        }
    }
}