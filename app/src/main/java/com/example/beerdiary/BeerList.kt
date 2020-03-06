package com.example.beerdiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class BeerListFragment : Fragment() {

    companion object {
        fun newInstance(): BeerListFragment {
            return BeerListFragment()
        }
    }

    private var listener: BeerFragmentListener? = null

    interface BeerFragmentListener {
        fun onButtonClick(position: Int)
    }

    private lateinit var mList: RecyclerView

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
        val activity = activity as Context
        mList = view.findViewById(R.id.beer_list)
        mList.layoutManager = LinearLayoutManager(context)
        val ump = ViewModelProviders.of(this).get(BeerModel::class.java)
        ump.getBeers().observe(this, Observer {
            mList.adapter =
                listener?.let { it1 -> BeerListAdapter(it, activity, it1) }
        })
        setAdapter()
        mList.itemAnimator = DefaultItemAnimator()
        val buttonAdd = view.findViewById<Button>(R.id.btn_addBeer)
        buttonAdd.setOnClickListener {
            val intent = Intent(activity, BeerNew::class.java)
            startActivity(intent)
        }
        return view
    }

    private fun setAdapter() {
        val activity = activity as Context
        if (BeerDB.get(activity).beerDao().getBeersAndReviews().value == null) {
            Log.d("query", " is null")
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