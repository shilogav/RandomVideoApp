package com.shilo.randomvideo.ui.home

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.shilo.randomvideo.R
import com.shilo.randomvideo.model.MediaSource


class VideoAdapter(mediaSources: ArrayList<MediaSource>, requestManager: RequestManager):
    RecyclerView.Adapter<VideoAdapter.VideoPlayerViewHolder>() {
    private var mediaSources: ArrayList<MediaSource> = mediaSources
    private var requestManager: RequestManager = requestManager
    companion object{
        const val VIDEO_SAMPLE = "funny_joke.mp4"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoPlayerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_row_item, parent, false)
        Log.i("VideoAdapter", "onCreateViewHolder run")
        return VideoPlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoPlayerViewHolder, position: Int) {
        holder.onBind(mediaSources[position],requestManager)
        Log.i("VideoAdapter", "onBindViewHolder run")
    }

    override fun getItemCount() = mediaSources.size

    /**
     * view holder
     */
    class VideoPlayerViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var title: TextView = view.findViewById(R.id.title)
        var mediaContainer : FrameLayout = view.findViewById(R.id.media_container)
        var thumbnail : ImageView = view.findViewById(R.id.thumbnail)
        var progressBar : ProgressBar = view.findViewById(R.id.progressBar)
        var volumeControl : ImageView = view.findViewById(R.id.volume_control)
        lateinit var requestManager : RequestManager
        val parent : View = view

        fun onBind(mediaSource : MediaSource, requestManager: RequestManager) {
            this.requestManager = requestManager
            parent.tag = this
            title.text = mediaSource.title
            this.requestManager
                    .load(mediaSource.thumbnail)
                    .into(thumbnail)
        }


    }
}