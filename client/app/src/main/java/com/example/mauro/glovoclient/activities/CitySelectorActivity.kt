package com.example.mauro.glovoclient.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ExpandableListView
import com.example.mauro.glovoclient.model.Cities
import com.example.mauro.glovoclient.model.Country
import com.example.mauro.glovoclient.R
import com.example.mauro.glovoclient.adapters.ExpandableListAdapter
import kotlinx.android.synthetic.main.activity_city_selector.*

@Suppress("UNCHECKED_CAST")
class CitySelectorActivity : AppCompatActivity(), ExpandableListView.OnChildClickListener {

    private var mCity = Cities()
    private var mAdapter: ExpandableListAdapter ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_city_selector)

        if(intent.extras != null) {
            var alCities = ArrayList<Cities>()
            var alCountries = ArrayList<Country>()
            val hmContent = HashMap<String, List<Cities>>()

            if(intent.hasExtra(ARG_CITY)) {
                alCities = intent.getSerializableExtra(ARG_CITY) as ArrayList<Cities>
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

            elv_city.setOnChildClickListener(this)

            bt_ok.setOnClickListener {
                setResult(Activity.RESULT_OK, Intent().putExtra(ARG_CITY, mCity))
                finish()
            }
        }
    }

    override fun onChildClick(parent: ExpandableListView?, v: View?, groupPosition: Int, childPosition: Int, id: Long): Boolean {
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
