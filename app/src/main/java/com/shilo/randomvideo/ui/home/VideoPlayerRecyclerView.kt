package com.shilo.randomvideo.ui.home

import android.content.Context
import android.graphics.Point
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.shilo.randomvideo.R
import com.shilo.randomvideo.model.MediaSource


class VideoPlayerRecyclerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {
    //constructor(context: Context) : super(context)
    //constructor(context: Context, atrrs : AttributeSet) : super(context, atrrs)
    enum class VolumeState {
        ON, OFF
    }
    private val TAG = "VideoPlayerRecyclerView"
    private lateinit var thumbnail: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var requestManager: RequestManager
    private lateinit var volumeControl:ImageView
    private lateinit var viewHolderParent : View
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var isVideoViewAdded = false
    private lateinit var videoSurfaceView: PlayerView
    private var videoPlayer : SimpleExoPlayer
    // controlling playback state
    lateinit var volumeState: VolumeState
    private var playPosition = -1
    private lateinit var frameLayout: FrameLayout
    private var mediaSources: ArrayList<MediaSource> = ArrayList()

    init {

        @Suppress("DEPRECATION")
        val display = (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y


        val bandwidthMeter = DefaultBandwidthMeter.Builder().build()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val builder = DefaultTrackSelector.ParametersBuilder()

        val trackSelectorParameters = builder.build()
        trackSelector.parameters = trackSelectorParameters
        val renderersFactory = DefaultRenderersFactory(context)

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        videoSurfaceView = PlayerView(context)
        // Bind the player to the view.
        videoSurfaceView.useController = false;
        videoSurfaceView.player = videoPlayer;


        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.")
                    if (::thumbnail.isInitialized) { // show the old thumbnail
                        thumbnail.visibility = VISIBLE
                    }

                    // There's a special case when the end of the list has been reached.
                    // Need to handle that with this bit of logic
                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }

        })

        setVolumeControl(VolumeState.ON)

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (::viewHolderParent.isInitialized && viewHolderParent == view)
                    resetVideoView()
            }

        })

        videoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.")
                        progressBar.visibility = VISIBLE
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "onPlayerStateChanged: Video ended.")
                        videoPlayer.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.")
                        progressBar.visibility = GONE
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {
                    }
                }
            }
        })
    }



    fun playVideo(isEndOfList: Boolean) {
        var targetPosition = 0

        if (!isEndOfList) {
            var startPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            var endPosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }

            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return;
            }

            // if there is more than 1 list-item on the screen
            if (startPosition != endPosition) {
                val startPositionVideoHeight = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition)

                targetPosition = if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                targetPosition = startPosition
            }
        } else {
            targetPosition = mediaSources.size - 1
        }

        Log.d(TAG, "playVideo: target position: " + targetPosition)

        // video is already playing so return
        if (targetPosition == playPosition) {
            return;
        }

        // set the position of the list-item that is to be played
        playPosition = targetPosition;
        if (!::videoSurfaceView.isInitialized) {
            return;
        }

        // remove any old surface views from previously playing videos
        videoSurfaceView.visibility = INVISIBLE
        removeVideoView(videoSurfaceView);

        val currentPosition = targetPosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

        val child = getChildAt(currentPosition) ?: return

        val holder: VideoAdapter.VideoPlayerViewHolder = child.tag as VideoAdapter.VideoPlayerViewHolder
        if (holder == null) {
            playPosition = -1
            return
        }

        thumbnail = holder.thumbnail
        progressBar = holder.progressBar
        volumeControl = holder.volumeControl
        viewHolderParent = holder.itemView
        requestManager = holder.requestManager
        frameLayout = holder.mediaContainer

        videoSurfaceView.player = videoPlayer

        viewHolderParent.setOnClickListener(videoViewClickListener())

        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
                context, Util.getUserAgent(context, "RecyclerView VideoPlayer"))
        val mediaUrl: String? = mediaSources[targetPosition].media_url
        if (mediaUrl != null) {
            val videoSource: ExtractorMediaSource? = ExtractorMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(Uri.parse(mediaUrl))
            videoPlayer.prepare(videoSource)
            videoPlayer.playWhenReady = true
        }
    }

    private fun videoViewClickListener() = OnClickListener { toggleVolume() }

    /**
     * Returns the visible region of the video surface on the screen.
     * if some is cut off, it will return less than the @videoSurfaceDefaultHeight
     * @param playPosition
     * @return
     */
    private fun getVisibleVideoSurfaceHeight(playPosition: Int) : Int {
        val at = playPosition - (layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: $at")

        val child = getChildAt(at) ?: return 0

        val location = IntArray(2)
        child.getLocationInWindow(location)

        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }

    private fun addVideoView() {
        frameLayout.addView(videoSurfaceView);
        isVideoViewAdded = true;
        videoSurfaceView.requestFocus();
        videoSurfaceView.visibility = VISIBLE
        videoSurfaceView.alpha = 1f;
        thumbnail.visibility = GONE
    }

    public fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView)
            playPosition = -1
            videoSurfaceView.visibility = INVISIBLE
            thumbnail.visibility = VISIBLE
        }
    }

    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as? ViewGroup

        val index = parent?.indexOfChild(videoView)
        if (index != null) {
            if (index >= 0) {
                parent.removeViewAt(index)
                isVideoViewAdded = false
                viewHolderParent.setOnClickListener(null)
            }
        }
    }

    fun releasePlayer() {
        videoPlayer.release()
        //videoPlayer = null
        //viewHolderParent = null
    }

    private fun toggleVolume() {
        if(videoPlayer != null) {
            if (volumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);
            } else if (volumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.")
                setVolumeControl(VolumeState.OFF)
            }
        }
    }

    private fun setVolumeControl(state: VolumeState) {
        volumeState = state
        if (state === VolumeState.OFF) {
            videoPlayer.volume = 0f
            animateVolumeControl()
        } else if (state === VolumeState.ON) {
            videoPlayer.volume = 1f
            animateVolumeControl()
        }
    }

    private fun animateVolumeControl() {
        if (this::volumeControl.isInitialized) {
            volumeControl.bringToFront()
            if(volumeState == VolumeState.OFF){
                requestManager.load(R.drawable.ic_baseline_volume_off_24)
                        .into(volumeControl)
            }
            else if(volumeState == VolumeState.ON){
                requestManager.load(R.drawable.ic_baseline_volume_up_24)
                        .into(volumeControl)
            }
            volumeControl.animate().cancel()

            volumeControl.alpha = 1f

            volumeControl.animate()
                    .alpha(0f)
                    .setDuration(600).startDelay = 1000
        }
    }

    fun setMediaSources(mediaSources: ArrayList<MediaSource>) {
        this.mediaSources = mediaSources
    }
}