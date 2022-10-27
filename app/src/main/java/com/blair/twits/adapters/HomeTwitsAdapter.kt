package com.blair.twits.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.blair.twits.databinding.TwitItemLayoutBinding

class HomeTwitsAdapter(private val fragment : Fragment, private val twitsArray : ArrayList<String>) :
    RecyclerView.Adapter<HomeTwitsAdapter.ViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding : TwitItemLayoutBinding = TwitItemLayoutBinding.inflate(
                        LayoutInflater.from(fragment.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val twitItems = twitsArray[position]

        Log.i("Array Items", "$twitItems")


        holder.username.text = twitItems

    }

    override fun getItemCount(): Int {
        return twitsArray.size
    }

    class ViewHolder(view : TwitItemLayoutBinding) : RecyclerView.ViewHolder(view.root) {
        // Defining the views
        val username = view.userName
    }

}