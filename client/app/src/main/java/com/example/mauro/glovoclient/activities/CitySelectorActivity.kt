package com.example.mauro.glovoclient.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.View
import android.widget.ExpandableListView
import com.example.mauro.glovoclient.model.SimpleCity
import com.example.mauro.glovoclient.model.Country
import com.example.mauro.glovoclient.R
import com.example.mauro.glovoclient.adapters.ExpandableListAdapter
import kotlinx.android.synthetic.main.activity_city_selector.*

@Suppress("UNCHECKED_CAST")
class CitySelectorActivity : AppCompatActivity(), ExpandableListView.OnChildClickListener {

    private var mCity = SimpleCity()
    private var mAdapter: ExpandableListAdapter ?= null
    private val TAG = "CitySelectorActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selector)

        if(intent.extras != null) {
            Log.i(TAG, "We got extras!")

            var alCities = ArrayList<SimpleCity>()
            var alCountries = ArrayList<Country>()
            val hmContent = HashMap<String, List<SimpleCity>>()

            if(intent.hasExtra(ARG_CITY)) {
                alCities = intent.getSerializableExtra(ARG_CITY) as ArrayList<SimpleCity>
            }
            if(intent.hasExtra(ARG_COUNTRIES)) {
                alCountries = intent.getSerializableExtra(ARG_COUNTRIES) as ArrayList<Country>
            }

            //Separate each country with each city
            alCountries.forEach { country ->
                val city = alCities.filter { city -> city.country_code == country.code }
                hmContent[country.name] = city
            }

            //Now that we got all sorted out, lets start the adapter
            mAdapter = ExpandableListAdapter(applicationContext, alCountries, hmContent)
            elv_city.setAdapter(mAdapter)

            //Setting child click listener
            elv_city.setOnChildClickListener(this)

            //Setting OK Button click listener, if the city is empty, a actionable snackbar will be shown to inform
            //the user why nothing happened
            bt_ok.setOnClickListener {
                if(mCity.code != "") {
                    setResult(Activity.RESULT_OK, Intent().putExtra(ARG_CITY, mCity))
                    finish()
                }
                else {
                    Snackbar.make(findViewById(R.id.ly_selector), getString(R.string.selector_rationale), Snackbar.LENGTH_INDEFINITE).setAction("Dismiss") {
                        fun Onclick(v: View) {
                        }
                    }.show()
                }
            }
        }
    }

    /**
     * Name: onChildClick
     *
     * Purpose: marks selected item so it can be highlighted
     */
    override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
        Log.d(TAG, "onChildClick")
        val index = parent!!.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition))
        parent.setItemChecked(index, true)
        mCity = mAdapter!!.getChild(groupPosition, childPosition)

        return true
    }

    companion object {
        const val ARG_CITY = "item_city"
        const val ARG_COUNTRIES = "item_countries"
    }
}
