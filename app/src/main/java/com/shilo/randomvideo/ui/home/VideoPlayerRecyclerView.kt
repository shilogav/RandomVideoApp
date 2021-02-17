package com.shilo.randomvideo.ui.home

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter


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
    private lateinit  var volumeControl:ImageView
    private lateinit var viewHolderParent : View
    private var isVideoViewAdded = false
    private lateinit var videoSurfaceView: PlayerView
    private var videoPlayer : SimpleExoPlayer
    // controlling playback state
    lateinit var volumeState: VolumeState
    private var playPosition = -1

    init {

        @Suppress("DEPRECATION")
        val display = (getContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay

        val bandwidthMeter = DefaultBandwidthMeter.Builder().build()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
        val builder = DefaultTrackSelector.ParametersBuilder()

        val trackSelectorParameters = builder.build()
        trackSelector.parameters = trackSelectorParameters
        val renderersFactory = DefaultRenderersFactory(context)

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        val videoSurfaceView = PlayerView(context)
        // Bind the player to the view.
        videoSurfaceView.setUseController(false);
        videoSurfaceView.setPlayer(videoPlayer);
        setVolumeControl(VolumeState.ON);

        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called.")
                    if (thumbnail != null) { // show the old thumbnail
                        thumbnail.setVisibility(VISIBLE)
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

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {

            }

            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent.equals(view))
                    resetVideoView()
            }

        })

        videoPlayer.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                
            }
        })
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

    }

    public fun playVideo(isEndOfList: Boolean) {

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

    }
}