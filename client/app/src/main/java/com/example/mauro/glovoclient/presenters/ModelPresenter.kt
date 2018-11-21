package com.example.mauro.glovoclient.presenters

import android.content.Context
import android.util.Log
import com.example.mauro.glovoclient.interfaces.ICity
import com.example.mauro.glovoclient.interfaces.ICountry
import com.example.mauro.glovoclient.model.SimpleCity
import com.example.mauro.glovoclient.model.City
import com.example.mauro.glovoclient.model.Country
import com.example.mauro.glovoclient.utility.RetrofitClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.PolyUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Name: ModelPresenter
 *
 * Purpose: Presenter for map related content, used on MainActivity
 */
class ModelPresenter: BasePresenter<ModelPresenter.View>() {
    private val TAG = "ModelPresenter"

    //Retrofit declarations
    private val retroFit = RetrofitClient.instance
    private var cityAPI: ICity = retroFit.create(ICity::class.java)
    private var countryAPI: ICountry = retroFit.create(ICountry::class.java)

    //Flags
    private var bMapMarked = false

    //Lists
    private var mAlCountry = ArrayList<Country>()
    private var mAlCities = ArrayList<SimpleCity>()
    private var mAlCity = ArrayList<City>()

    //Maps
    private var hmLatLng = HashMap<String, HashMap<Int, MutableList<LatLng>>>()
    private var hmBounds = HashMap<String, LatLngBounds>()


    /**
     * Name: hydrateModel
     *
     * Purpose: first function of a sequence, used for hydrating the whole model
     * needed for the map interaction. This part gets the Country list
     */
    fun hydrateModel() {
        Log.d(TAG, "hydrateModel")
        val countryList = countryAPI.countryList
        countryList.enqueue(object: Callback<ArrayList<Country>> {
            override fun onFailure(call: Call<ArrayList<Country>>, t: Throwable) {
                Log.w(TAG, "hydrateModel - Clearly failed")
            }

            override fun onResponse(call: Call<ArrayList<Country>>, response: Response<ArrayList<Country>>) {
                Log.i(TAG, "hydrateModel - onResponse")
                mAlCountry = response.body()!!
                hydrateSimpleCity()
            }
        })
    }

    /**
     * Name: hydrateSimpleCity
     *
     * Purpose: second function of a sequence, used for hydrating the whole model
     * needed for map interaction. This part gets the simplified city list.
     * It also decodes all polylines and stores them to be used later
     */
    fun hydrateSimpleCity() {
        Log.d(TAG, "hydrateSimpleCity")

        val cityList = cityAPI.cityList
        cityList.enqueue(object: Callback<ArrayList<SimpleCity>> {
            override fun onFailure(call: Call<ArrayList<SimpleCity>>, t: Throwable) {
                Log.w(TAG, "hydrateSimpleCity - Clearly failed")
            }

            override fun onResponse(call: Call<ArrayList<SimpleCity>>, response: Response<ArrayList<SimpleCity>>) {
                Log.i(TAG, "hydrateSimpleCity - onResponse")
                //Now we get each city
                mAlCities = response.body()!!
                retrieveCityInformation(mAlCities)
                decodePolylines()
                getCurrentLocation()
            }
        })
    }

    /**
     * Name: retrieveCityInformation
     *
     * Purpose: last part of the hydrate function, it iterates through each "SimpleCity" to fill the full city object
     */
    fun retrieveCityInformation(i_alCities: ArrayList<SimpleCity>) {
        Log.d(TAG, "retrieveCityInformation")

        i_alCities.forEach { city ->

            val cityList = cityAPI.getCityDetail(city.code)
            cityList.enqueue(object: Callback<City> {
                override fun onFailure(call: Call<City>, t: Throwable) {
                    Log.w(TAG, "retrieveCityInformation - Clearly failed")
                }
                override fun onResponse(call: Call<City>, response: Response<City>) {
                    Log.i(TAG, "retrieveCityInformation - onResponse")
                    //Now we get each city
                    mAlCity.add(response.body()!!)
                }
            })

        }
    }

    /**
     * Name: decodePolylines
     *
     * Purpose: decodes each polyline into a single object so it can be stored separately.
     * creates mapBounds for each city so it can be used later
     */
    fun decodePolylines() {
        Log.d(TAG, "decodePolylines")

        mAlCities.forEach { city ->
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

    /**
     * Name: Checks if the current locations is contained inside a working area
     */
    fun IsInsideWorkingArea(i_latitude: Double, i_longitude: Double) : Boolean {
        Log.d(TAG, "IsInsideWorkingArea")

        var bContained = false

        mAlCities.forEach { city ->
            hmLatLng[city.code]!!.forEach { locationList ->
                if(!hmLatLng[city.code]!!.isEmpty() && PolyUtil.containsLocation(LatLng(i_latitude, i_longitude), locationList.value, true)) {
                    bContained = true
                    return bContained
                }
            }
        }
        return bContained
    }

    /**
     * Name: addPolygons
     *
     * Purpose: createss the polygons object for each city and adds it to the map
     */
    fun addPolygons(i_map: GoogleMap) {
        Log.d(TAG, "addPolygons")

        mAlCities.forEach { city ->
            //Check each working area
            hmLatLng[city.code]!!.forEach { locationList ->
                if(locationList.value.size > 0) {
                    i_map.addPolygon(
                        PolygonOptions()
                            .addAll(locationList.value)
                            .fillColor(0x7FFFFF7F))
                }
            }
        }
    }

    /**
     * Name: getCurrentLocation
     *
     * Purpose: calls the getCurrentLocation of the view
     */
    fun getCurrentLocation() {
        Log.d(TAG, "getCurrentLocation")

        view?.getCurrentLocation(mAlCities, mAlCountry)
    }

    /**
     * Name: getCityList
     *
     * Purpose: answers with the member simple city list
     */
    fun getCityList() : ArrayList<SimpleCity> {
        Log.d(TAG, "getCityList")

        return mAlCities
    }

    /**
     * Name: getCountryList
     *
     * Purpose: answers with the member country list
     */
    fun getCountryList() : ArrayList<Country> {
        Log.d(TAG, "getCountryList")

        return mAlCountry
    }

    /**
     * Name: getBounds
     *
     * Purpose: answers the current city bound so it can be evaluated
     */
    fun getBounds(iCityCode: String) : LatLngBounds? {
        Log.d(TAG, "getBounds")

        return hmBounds[iCityCode]
    }

    /**
     * Name: updateMap
     *
     * Purpose: method used to update information panel and markers on the map
     */
    fun updateMap(i_map: GoogleMap) : Array<String> {
        Log.d(TAG, "updateMap")

        var sLocationInformation = ""
        var sLocationTimeZone = ""
        var sLocationCurrency = ""

        mAlCities.forEach { city ->

            if(i_map.cameraPosition.zoom <= 10.0f) {
                i_map.addMarker(CreateCityMarker(city))
                bMapMarked = true
            }
            //Remove it if exists
            else if(bMapMarked){
                bMapMarked = false
                i_map.clear()
                addPolygons(i_map)
            }

            hmLatLng[city.code]!!.forEach { locationList ->
                if(!hmLatLng[city.code]!!.isEmpty() && PolyUtil.containsLocation(i_map.cameraPosition.target, locationList.value, true)) {
                    val currentCity = mAlCity.find { it -> it.code == city.code }
                    sLocationInformation = String.format("%s - (%s)", currentCity!!.name, currentCity.country_code)
                    sLocationTimeZone = currentCity.time_zone
                    sLocationCurrency = currentCity.currency
                }
            }
        }

        return arrayOf(sLocationInformation, sLocationTimeZone, sLocationCurrency)
    }

    /**
     * Name: CreateCityMarker
     *
     * Purpose: Creates a marker for each city, called when the zoom level is too high
     */
    private fun CreateCityMarker(iCity: SimpleCity) : MarkerOptions {
        Log.d(TAG, "CreateCityMarker")

        val oMarkerOptions = MarkerOptions()
        oMarkerOptions.position(hmBounds[iCity.code]!!.center).title(String.format("%s - (%s)", iCity.name, iCity.code))
        return oMarkerOptions
    }


    /**
     * Purpose: interface methods shared between the view and the presenter
     */
    interface View {
        fun requestPermissions()
        fun getCurrentLocation(i_alCities: ArrayList<SimpleCity>, i_alCountry: ArrayList<Country>)
        fun getContext() : Context
    }
}