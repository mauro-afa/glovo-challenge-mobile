package com.example.mauro.glovoclient.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import com.example.mauro.glovoclient.Model.Cities
import com.example.mauro.glovoclient.Model.Country
import android.view.LayoutInflater
import android.widget.TextView
import com.example.mauro.glovoclient.R


class ExpandableListAdapter(private var i_Context: Context,
                            private var i_AlCountries: ArrayList<Country>,
                            private var i_HmData: HashMap<String, List<Cities>>) : BaseExpandableListAdapter() {

    override fun getChild(groupPosition: Int, childPosition: Int): Cities {
        return i_HmData[i_AlCountries[groupPosition].name]!![childPosition]
    }

    override fun getGroup(groupPosition: Int): Country {
        return i_AlCountries[groupPosition]
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView

        if (convertView == null) {
            val inflater = i_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.group_country, null)
        }

        val tvGroup = convertView!!.findViewById<TextView>(R.id.tv_group_country)
        tvGroup.setTypeface(null, Typeface.BOLD)
        tvGroup.text = getGroup(groupPosition).name

        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return i_HmData[i_AlCountries[groupPosition].name]!!.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {

        var convertView = convertView

        if (convertView == null) {
            val inflater = i_Context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_city, null)
        }

        val tvGroup = convertView!!.findViewById<TextView>(R.id.tv_item_city)
        tvGroup.setTypeface(null, Typeface.BOLD)
        tvGroup.text = getChild(groupPosition, childPosition).name

        return convertView
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getGroupCount(): Int {
        return i_AlCountries.size
    }
}