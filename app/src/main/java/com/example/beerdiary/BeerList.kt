package com.example.beerdiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.beer_list_fragment.*

class BeerListFragment : Fragment() {

    companion object {
        fun newInstance(): BeerListFragment {
            return BeerListFragment()
        }
    }

    private var listener: BeerFragmentListener? = null
    private lateinit var mList: RecyclerView
    private lateinit var ump: BeerModel
    private val sortItems: Array<String> = arrayOf("Name", "Brewer", "Score")

    interface BeerFragmentListener {
        fun onButtonClick(position: Int)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BeerFragmentListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement BeerFragmentListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.beer_list_fragment, container, false)
        val activityTemp = activity as Context
        mList = view.findViewById(R.id.beer_list)
        mList.layoutManager = LinearLayoutManager(context)

        //Setup, viewmodel, livedata and adapter
        activity?.let {
            ump = ViewModelProviders.of(it).get(BeerModel::class.java)
        }

        ump.getBeers().observe(this, Observer {
            mList.adapter =
                listener?.let { it1 -> BeerListAdapter(it, activityTemp, it1) }
        })

        setAdapter()

        mList.itemAnimator = DefaultItemAnimator()

        setHasOptionsMenu(true)

        //Setup dropdown menu with custom adapter that does no filtering
        val arrayAdapter = activity?.applicationContext?.let {
            MaterialDropdownAdapter(
                it,
                R.layout.sort_dropdown_pop_item,
                sortItems
            )
        }
        val filledExposedDropdown =
            view.findViewById<AutoCompleteTextView>(R.id.filled_exposed_dropdown)
        filledExposedDropdown.setAdapter(arrayAdapter)
        filledExposedDropdown.setOnItemClickListener { _, _, position, _ ->
            onSortSelected(
                position
            )
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Set the actionbar for this activity
        val activity = (requireActivity() as AppCompatActivity)
        activity.setSupportActionBar(bar_list)
    }

    //Setup the bottom options menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.list_bottom_menu, menu)
        Log.d("inflate", "menu inflated")
        bar_list.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.app_bar_add -> {
                    val intent = Intent(activity, BeerNew::class.java)
                    startActivity(intent)
                    true
                }
                R.id.app_bar_map -> {
                    val intent = Intent(activity, LocationAll::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun onSortSelected(position: Int) {
        ump.sortBeers(sortItems[position])
    }

    private fun setAdapter() {
        val activity = activity as Context
        if (BeerDB.get(activity).beerDao().getBeersAndReviews().value == null) {
            mList.adapter = listener?.let {
                val temp: List<BeerAndReviews> = emptyList()
                BeerListAdapter(
                    temp, activity,
                    it
                )
            }
        } else {
            val ump = ViewModelProviders.of(this).get(BeerModel::class.java)
            ump.getBeers().observe(this, Observer {
                mList.adapter =
                    listener?.let { it1 -> BeerListAdapter(it, activity, it1) }
            })
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}