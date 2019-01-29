package com.android.pause.ui.home

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.pause.R
import com.android.pause.data.model.itineraries.ItineraryListItem
import kotlinx.android.synthetic.main.item_trips_list.view.*

class TripsListAdapter(private val mHeight: Int, private val itineraryList: List<ItineraryListItem>, private val mCallBack: MainMvpView) : RecyclerView.Adapter<CustomViewHolder>() {
    private val drawables = intArrayOf(R.drawable.dummy_1, R.drawable.dummy_2, R.drawable.dummy_3, R.drawable.dummy_1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.item_trips_list, parent, false)
        itemView.minimumHeight = mHeight
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        holder.mParentLayout?.layoutParams?.height = mHeight + itemCount / 2
        holder.mDestinationImage?.setImageResource(drawables[position])
        /*holder.mTripName?.text = itineraryList[position].itineraryName()

        *//* format date 2018-12-20 10:10:00  to 12.20.18 *//*
        var formattedDate: String? = ""
        try {
            val itineraryDate = itineraryList[position].itineraryCreatedDate()
            if (itineraryDate != null) {
                val ar = itineraryDate.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                formattedDate = String.format("CREATED ON %s.%s.%s", ar[1], ar[2].split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0], ar[0].substring(2))
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        if (formattedDate != null && !formattedDate.isEmpty()) holder.mCreatedDate!!.text = formattedDate

        holder.itemView.setOnClickListener { v -> mCallBack.onTripsItemSelected(position, holder.mDestinationImage!!) }*/
    }

    override fun getItemCount(): Int {
        return 3
    }

}

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val mParentLayout = itemView.container
    val mDestinationImage = itemView.ivDestinationImage
    val mTripName = itemView.tvTripName
    val mCreatedDate = itemView.tvCreatedDate
    val mContainer = itemView.frameLayout

}
