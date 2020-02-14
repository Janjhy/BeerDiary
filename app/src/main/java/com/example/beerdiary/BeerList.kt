package com.example.beerdiary

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    interface BeerFragmentListener {
        fun onButtonClick(position: Int)

    }

    private lateinit  var mList: RecyclerView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BeerFragmentListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement BeerFragmentListener")
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
                listener?.let { it1 -> MyAdapter(it, activity, it1) }
        })

        mList.itemAnimator = DefaultItemAnimator()
        return view


    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn_addBeer.setOnClickListener {
            val beerName = edit_name.text.toString()
            val brewerName = edit_brewer.text.toString()
            Log.d("info", "name and brewer $beerName $brewerName")
            val beer = Beer(0, beerName, brewerName)
            val db = context?.let { it1 -> BeerDB.get(it1) }
            Thread(Runnable {
                val id = db?.beerDao()?.insertBeer(beer)
                Log.d("insert", "inserted beer id: $id")
                //db?.clearAllTables()
                if (db != null) {
                    Log.d("contents", "beer size " + (db.beerDao().getBeersR() ))
                }
                this.activity?.runOnUiThread {
                    mList.adapter?.notifyDataSetChanged()
                }
            }).start()

        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}
