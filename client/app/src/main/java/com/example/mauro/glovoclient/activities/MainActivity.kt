package com.example.mauro.glovoclient.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import com.example.mauro.glovoclient.interfaces.ICity
import com.example.mauro.glovoclient.interfaces.ICountry
import com.example.mauro.glovoclient.model.Cities
import com.example.mauro.glovoclient.model.City
import com.example.mauro.glovoclient.model.Country
import com.example.mauro.glovoclient.R
import com.example.mauro.glovoclient.utility.RetrofitClient
import com.example.mauro.glovoclient.utility.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.maps.android.PolyUtil
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var bMapMarked = false

    lateinit var cityAPI: ICity
    lateinit var countryAPI: ICountry
    private var mCity = Cities()

    private var alCountry = ArrayList<Country>()
    private var alCity = ArrayList<City>()
    private var alCities = ArrayList<Cities>()
    private var hmLatLng = HashMap<String, HashMap<Int, MutableList<LatLng>>>()
    private var hmBounds = HashMap<String, LatLngBounds>()

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Initialize API variables
        val retroFit = RetrofitClient.instance
        cityAPI = retroFit.create(ICity::class.java)
        countryAPI = retroFit.create(ICountry::class.java)

        //Lets get the country list
        getCountryList()

        //Lets get the city list
        getCityList()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnCameraIdleListener {
            var sLocationInformation = ""
            var sLocationTimeZone = ""
            var sLocationCurrency = ""
            Log.d(TAG, "Moving!" + mMap.cameraPosition.target)

            alCities.forEach { city ->

                //Set the respective marker9
                if(mMap.cameraPosition.zoom <= 10.0f) {
                    mMap.addMarker(CreateCityMarker(city))
                    bMapMarked = true
                }
                //Remove it if exists
                else if(bMapMarked){
                    bMapMarked = false
                    mMap.clear()
                    addPolylines()
                }


                hmLatLng[city.code]!!.forEach { locationList ->
                    if(!hmLatLng[city.code]!!.isEmpty() && PolyUtil.containsLocation(mMap.cameraPosition.target, locationList.value, true)) {
                        val currentCity = alCity.find { it -> it.code == city.code }
                        sLocationInformation = String.format("%s - (%s)", currentCity!!.name, currentCity.country_code)
                        sLocationTimeZone = currentCity.time_zone
                        sLocationCurrency = currentCity.currency
                    }
                }
            }
            tv_location_information.text = sLocationInformation
            tv_location_timezone.text = sLocationTimeZone
            tv_location_currency.text = sLocationCurrency
        }

        mMap.setOnMarkerClickListener {
            val regex = "\\((.*?)\\)".toRegex()
            val matchResult = regex.find(it.title)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(hmBounds[matchResult!!.groupValues[1]], 0))
            true
        }

        addPolylines()


        // Add a marker in Sydney and move the camera
        if(lastLocation != null && mCity.code == "") {
            mMap.isMyLocationEnabled = true
            val myLocation = LatLng(lastLocation!!.latitude, lastLocation!!.longitude)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15.0f))
        }
        else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(hmBounds[mCity.code], 0))
        }
    }

    private fun requestPermissions() {
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



    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isEmpty()) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
                //getAddress()
            } else {
                startActivityForResult(Intent(this, CitySelectorActivity::class.java)
                    .putExtra(CitySelectorActivity.ARG_CITY, alCities)
                    .putExtra(CitySelectorActivity.ARG_COUNTRIES, alCountry), ARG_REQUEST_CODE_CITY)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        mFusedLocationClient!!.lastLocation
            .addOnSuccessListener(this, OnSuccessListener<Location> { location ->
                if (location == null) {
                    Log.w(TAG, "onSuccess:null")
                    return@OnSuccessListener
                }
                lastLocation = location

                //We should check if this location is contained within Glovo's delivery range
                if(!IsContained()) {
                    startActivityForResult(Intent(this, CitySelectorActivity::class.java)
                        .putExtra(CitySelectorActivity.ARG_CITY, alCities)
                        .putExtra(CitySelectorActivity.ARG_COUNTRIES, alCountry), ARG_REQUEST_CODE_CITY)

                }

                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this@MainActivity)
            })
            .addOnFailureListener(this) { e -> Log.w(TAG, "getLastLocation:onFailure", e) }
    }

    private fun showSnackbar(mainTextStringId: Int, actionStringId: Int,
                             listener: View.OnClickListener) {
        Snackbar.make(findViewById(android.R.id.content),
            getString(mainTextStringId),
            Snackbar.LENGTH_INDEFINITE)
            .setAction(getString(actionStringId), listener).show()
    }

    private fun CreateCityMarker(iCity: Cities) : MarkerOptions {
        val oMarkerOptions = MarkerOptions()
        oMarkerOptions.position(hmBounds[iCity.code]!!.center).title(String.format("%s - (%s)", iCity.name, iCity.code))
        return oMarkerOptions
    }

    private fun getCityInformation(i_alCities: ArrayList<Cities>) {

        i_alCities.forEach { city ->

            val cityList = cityAPI.getCityDetail(city.code)
            cityList.enqueue(object: Callback<City> {
                override fun onFailure(call: Call<City>, t: Throwable) {
                    Log.d(TAG, "Clearly failed")
                }

                override fun onResponse(call: Call<City>, response: Response<City>) {
                    //Now we get each city
                    alCity.add(response.body()!!)
                }

            })

        }
    }

    private fun addPolylines() {

        alCities.forEach { city ->
            //Check each working area
            hmLatLng[city.code]!!.forEach { locationList ->
                if(locationList.value.size > 0) {
                    mMap.addPolygon(PolygonOptions()
                        .addAll(locationList.value)
                        .fillColor(0x7FFFFF7F))
                }
            }
        }
    }

    private fun decodePolylines() {
        alCities.forEach { city ->
            val latLngBuilder = LatLngBounds.builder()
            //Check each working area
            if(hmLatLng[city.code] == null) {
                hmLatLng[city.code] = HashMap()
            }
            city.working_area.forEachIndexed { index, WorkingArea ->
                //If this is a new list, just initialize it here
                hmLatLng[city.code]!![index] = PolyUtil.decode(WorkingArea)
            }

            hmLatLng[city.code]!!.forEach { list ->
                if(list.value.size>0) {
                    list.value.forEach { latLang ->
                        latLngBuilder.include(latLang)
                    }
                    hmBounds[city.code] = latLngBuilder.build()
                }
            }
        }
    }
    private fun getCountryList() {
        val countryList = countryAPI.countryList
        countryList.enqueue(object: Callback<ArrayList<Country>> {
            override fun onFailure(call: Call<ArrayList<Country>>, t: Throwable) {
                Log.d(TAG, "Clearly failed")
            }

            override fun onResponse(call: Call<ArrayList<Country>>, response: Response<ArrayList<Country>>) {
                alCountry = response.body()!!
            }
        })
    }

    private fun getCityList() {
        val cityList = cityAPI.cityList
        cityList.enqueue(object: Callback<ArrayList<Cities>> {
            override fun onFailure(call: Call<ArrayList<Cities>>, t: Throwable) {
                Log.d(TAG, "Clearly failed")
            }

            override fun onResponse(call: Call<ArrayList<Cities>>, response: Response<ArrayList<Cities>>) {
                //Now we get each city
                alCities = response.body()!!
                getCityInformation(alCities)
                decodePolylines()

                if (!Utils.checkPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    requestPermissions()
                }
                else if(lastLocation == null) {
                    getCurrentLocation()
                }
            }
        })
    }

    private fun IsContained() : Boolean {
        var bContained = false
        alCities.forEach { city ->
            hmLatLng[city.code]!!.forEach { locationList ->

                if(!hmLatLng[city.code]!!.isEmpty() && PolyUtil.containsLocation(LatLng(lastLocation!!.latitude, lastLocation!!.longitude), locationList.value, true)) {
                    bContained = true
                    return bContained
                }
            }
        }

        return bContained
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == ARG_REQUEST_CODE_CITY) {

            if(resultCode == Activity.RESULT_OK) {
                mCity = data!!.getSerializableExtra(CitySelectorActivity.ARG_CITY) as Cities

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(hmBounds[mCity.code], 0))

                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
                mapFragment.getMapAsync(this@MainActivity)
            }

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val ARG_REQUEST_CODE_CITY = 10
    }
}
