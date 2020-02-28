package com.example.beerdiary

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.beer_review_info.*

class FragmentBeerInfo : Fragment() {

    companion object {
        @JvmStatic
        fun newInstance(position: Int): FragmentBeerInfo {
            val detailFragment = FragmentBeerInfo()
            val args = Bundle()
            args.putInt("pos", position)
            detailFragment.arguments = args
            return detailFragment
        }
    }

    private var position: Int = 0

    override fun onAttach(context: Context) {
        context.let { super.onAttach(it) }
        arguments?.getInt("pos")?.let { position = it }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.beer_review_info, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btn_add_score_comment.setOnClickListener {
            val score = Integer.parseInt(edit_score.text.toString())
            val comment = edit_comment.text.toString()
            val db = context?.let { it2 -> BeerDB.get(it2) }
            Thread(Runnable {
                val beerId = db?.beerDao()?.getBeersR()?.get(position)?.beerId
                val review = beerId?.let { it1 -> Review(0, score, comment, it1) }
                val id = review?.let { it1 -> db.beerDao().insertReview(it1) }
                Log.d("insert", "inserted review id: $id")
                update()
            }).start()
        }
        update()
    }

    private fun update() {
        val db = context?.let { it -> BeerDB.get(it) }

        Thread(Runnable {
            val beer = db?.beerDao()?.getBeersR()?.get(position)
            val review = beer?.beerId?.let { db.beerDao().getBeerReview(it) }
            if (beer != null) {
                Log.d("beer name", beer.beerName)
                this.activity?.runOnUiThread {
                    beer_name.text = beer.beerName
                    beer_brewer.text = beer.brewer
                }

            }
            if (review != null) {
                this.activity?.runOnUiThread {
                    beer_score.text = review.score.toString()
                    beer_comment.text = review.comment
                }

            }
        }).start()
    }

}