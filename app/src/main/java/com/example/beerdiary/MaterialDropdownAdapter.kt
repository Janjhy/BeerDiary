package com.example.beerdiary

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class MaterialDropdownAdapter<String>(context: Context, view: Int, var array: Array<String>) :
    ArrayAdapter<String>(context, view, array) {

    override fun getFilter(): Filter {
        return noFilterFilter
    }

    private val noFilterFilter = object: Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val res = FilterResults()
            res.count = array.size
            res.values = array
            return res
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}