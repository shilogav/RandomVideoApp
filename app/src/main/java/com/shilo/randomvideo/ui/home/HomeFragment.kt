package com.shilo.randomvideo.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.shilo.randomvideo.R
import com.shilo.randomvideo.util.Resource
import com.shilo.randomvideo.util.VerticalSpacingItemDecorator
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList


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
        val resource = Resource()
        val mediaSources = resource.MEDIA_RESOURCE.toCollection(ArrayList())
        val itemDecorator = VerticalSpacingItemDecorator(10)
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            videoAdapter = VideoAdapter(mediaSources, initGlide())
            adapter = videoAdapter
            addItemDecoration(itemDecorator)
        }

    }
    private fun initGlide(): RequestManager {
        val options: RequestOptions = RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background)
        return Glide.with(this)
                .setDefaultRequestOptions(options)
    }

    override fun onDestroy() {
        if (recycler_view != null) recycler_view.releasePlayer()
        super.onDestroy()
    }
}