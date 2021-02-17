package com.shilo.randomvideo.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.shilo.randomvideo.MainActivity
import com.shilo.randomvideo.R
import com.shilo.randomvideo.model.MediaSource
import kotlinx.android.synthetic.main.fragment_home.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var videoAdapter: VideoAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            text_home.text = it
        })
        initRecyclerView()


    }

    private fun initRecyclerView(){
        val arrayList = ArrayList<String>()
        arrayList.add("hello")
        arrayList.add("world")
        arrayList.add("hello1")
        arrayList.add("world1")
        arrayList.add("hello2")
        arrayList.add("world2")
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            videoAdapter = VideoAdapter(arrayList, requireActivity())
            adapter = videoAdapter
        }

        var media : MediaSource = MediaSource("","","","")

    }


}