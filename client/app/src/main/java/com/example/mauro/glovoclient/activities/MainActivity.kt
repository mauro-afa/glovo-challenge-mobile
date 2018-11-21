package com.example.mauro.glovoclient.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.example.mauro.glovoclient.presenters.ModelPresenter
import com.example.mauro.glovoclient.model.SimpleCity
import com.example.mauro.glovoclient.model.Country
import com.example.mauro.glovoclient.R
import com.example.mauro.glovoclient.utility.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback, ModelPresenter.View {

    private var mFirstRun = true
    private lateinit var mMap: GoogleMap
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null

    private var mCity = SimpleCity()

    private val TAG = "MainActivity"
    private val mPresenter: ModelPresenter =
        ModelPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mPresenter.attachView(this)
        mPresenter.hydrateModel()
        setMapFragment()
    }

    override fun onStart() {
        Log.d(TAG, "onStart")
        super.onStart()
        if (mFirstRun && !Utils.checkPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mFirstRun = false
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady")

        mMap = googleMap

        mMap.setOnCameraIdleListener {

            Log.d(TAG, "Moving!" + mMap.cameraPosition.target)

            val oArray = mPresenter.updateMap(mMap)

            tv_location_information.text = oArray[0]
            tv_location_timezone.text = oArray[1]
            tv_location_currency.text = oArray[2]
        }

        mMap.setOnMarkerClickListener {
            val regex = "\\((.*?)\\)".toRegex()
            val matchResult = regex.find(it.title)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mPresenter.getBounds(matchResult!!.groupValues[1]), 0))
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "User location accepted.")
            } else {
                Log.i(TAG, "User location denied.")
                startActivityForResult(Intent(this, CitySelectorActivity::class.java)
                    .putExtra(CitySelectorActivity.ARG_CITY, mPresenter.getCityList())
                    .putExtra(CitySelectorActivity.ARG_COUNTRIES, mPresenter.getCountryList()), ARG_REQUEST_CODE_CITY)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult")

        if(requestCode == ARG_REQUEST_CODE_CITY) {

            if(resultCode == Activity.RESULT_OK) {


                mCity = data!!.getSerializableExtra(CitySelectorActivity.ARG_CITY) as SimpleCity

                setMapLocation()
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Name: getCurrentLocation
     *
     * Purpose: obtains user location if we are allowed
     */
    @SuppressLint("MissingPermission")
    override fun getCurrentLocation(i_alCities: ArrayList<SimpleCity>, i_alCountry: ArrayList<Country>) {
        Log.d(TAG, "getCurrentLocation")
        mFusedLocationClient!!.lastLocation
            .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                if (location == null) {
                    Log.w(TAG, "onSuccess:null")
                    return@OnSuccessListener
                }
                lastLocation = location

                //We should check if this location is contained within Glovo's delivery range
                if(!mPresenter.IsInsideWorkingArea(lastLocation!!.latitude, lastLocation!!.longitude)) {
                    startActivityForResult(Intent(this, CitySelectorActivity::class.java)
                        .putExtra(CitySelectorActivity.ARG_CITY, i_alCities)
                        .putExtra(CitySelectorActivity.ARG_COUNTRIES, i_alCountry), ARG_REQUEST_CODE_CITY)

                }
                else {
                    setMapLocation()
                }
            })
            .addOnFailureListener(this) { e -> Log.w(TAG, "getLastLocation:onFailure", e) }
    }

    /**
     * Name: setMapLocation
     *
     * Purpose: set location depending on if we have the current user location or
     * if the user selected a city
     */
    @SuppressLint("MissingPermission")
    private fun setMapLocation() {
        Log.d(TAG, "setMapLocation")
        mPresenter.addPolygons(mMap)
        if(lastLocation != null && mCity.code == "") {
            mMap.isMyLocationEnabled = true
            val myLocation = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f))
        }
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mPresenter.getBounds(mCity.code), 0))
        }
    }

    /**
     * Name: showSnackbar
     *
     * Purpose: shows snackbar on current view
     */
    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Log.d(TAG, "showSnackbar")
        Snackbar.make(findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(actionStringId), listener).show()
    }

    /**
     * Name: setMapFrargment
     *
     * Purpose: sets map fragment, enabling OnMapReady event
     */
    fun setMapFragment() {
        Log.d(TAG, "setMapFragment")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this@MainActivity)
    }

    /**
     * Name: getContext
     *
     * Purpose: answers the context call, usually used from the adapter.
     */
    override fun getContext(): Context {
        Log.d(TAG, "getContext")
        return applicationContext
    }

    /**
     * Name: requestPermissions
     *
     * Purpose: request the user permission to use location services, if the permissions are denied
     * they will be asked again unless told otherwise
     */
    override fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")

            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })

        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    companion object {
        const val ARG_REQUEST_CODE_CITY = 10
    }
}
