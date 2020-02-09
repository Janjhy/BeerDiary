package com.example.androiddb

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        val mList = view.findViewById<RecyclerView>(R.id.beer_list)

        mList.layoutManager = LinearLayoutManager(context)

        mList.adapter = listener?.let { MyAdapter(activity, GlobalModel.presidents, it) }

        mList.itemAnimator = DefaultItemAnimator()
        return view
    }


    override fun onDetach() {
        super.onDetach()
        listener = null
    }

}
