package com.blair.twits

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blair.twits.databinding.FragmentInfoSettingBinding

class InfoSettingFragment : Fragment() {

    private lateinit var binding: FragmentInfoSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



    }

}






//        binding.iNavigate.setOnClickListener {
//            val intent = Intent(activity, MainActivity::class.java)
//                        startActivity(intent)
//        }