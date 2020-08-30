package net.xblacky.animexstream.ui.main.player

import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.fragment_video_player.*
import net.xblacky.animexstream.MainActivity
import net.xblacky.animexstream.R
import net.xblacky.animexstream.utils.model.Content
import timber.log.Timber
import java.lang.Exception

class VideoPlayerActivity : AppCompatActivity(), VideoPlayerListener {

    private lateinit var viewModel: VideoPlayerViewModel
    private var episodeNumber: String? = ""
    private var animeName: String? = ""
    private lateinit var content: Content
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)
        viewModel = ViewModelProvider(this).get(VideoPlayerViewModel::class.java)
        getExtra(intent)
//        (playerFragment as VideoPlayerFragment).updateContent(Content(
//            url = url,
//            episodeNumber = "153"
//        ))
        setObserver()
        goFullScreen()
    }

    override fun onNewIntent(intent: Intent?) {
        (playerFragment as VideoPlayerFragment).playOrPausePlayer(
            playWhenReady = false,
            loseAudioFocus = false
        )
        (playerFragment as VideoPlayerFragment).saveWatchedDuration()
        getExtra(intent)
        super.onNewIntent(intent)

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPipMode()
    }


    override fun onResume() {
        super.onResume()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            goFullScreen()
        }
    }

    private fun getExtra(intent: Intent?) {
        val url = intent?.extras?.getString("episodeUrl")
        episodeNumber = intent?.extras?.getString("episodeNumber")
        animeName = intent?.extras?.getString("animeName")
        viewModel.updateEpisodeContent(
            Content(
                animeName = animeName ?: "",
                episodeUrl = url,
                episodeName = animeName!! + " (" + episodeNumber!! + ")",
                url = ""
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }

    @Suppress("DEPRECATION")
    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && hasPipPermission()
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val params = PictureInPictureParams.Builder()
                this.enterPictureInPictureMode(params.build())
            } else {
                this.enterPictureInPictureMode()
            }
        }
    }

    override fun onStop() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
            &&  hasPipPermission()
        ) {
            finishAndRemoveTask()
        }
        super.onStop()
    }

    override fun finish() {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            finishAndRemoveTask()
        }
        super.finish()
    }

    fun enterPipModeOrExit() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
            && packageManager
                .hasSystemFeature(
                    PackageManager.FEATURE_PICTURE_IN_PICTURE
                )
            && (playerFragment as VideoPlayerFragment).isVideoPlaying()
            && hasPipPermission()
        ) {
            try{

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val params = PictureInPictureParams.Builder()
                    this.enterPictureInPictureMode(params.build())
                } else {
                    this.enterPictureInPictureMode()
                }
            }catch (ex:Exception){
                Timber.e(ex.message)
            }

        } else {
            finish()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration?
    ) {
        exoPlayerView.useController = !isInPictureInPictureMode
        if (isInPictureInPictureMode) {
            videoPlayerCardView.radius = 0f
        } else {
            val scale = resources.displayMetrics.density
            videoPlayerCardView.radius = (8.0f * scale + 0.5f)
        }
    }

    private fun hasPipPermission(): Boolean {
        val appsOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                appsOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                appsOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    packageName
                ) == AppOpsManager.MODE_ALLOWED
            }
            else -> {
                false
            }
        }
    }

    private fun setObserver() {
        viewModel.liveContent.observe(this, Observer {
            this.content = it
            it?.let {
                if (!it.url.isNullOrEmpty()) {
                    (playerFragment as VideoPlayerFragment).updateContent(it)
                }
            }
        })
        viewModel.isLoading.observe(this, Observer {
            (playerFragment as VideoPlayerFragment).showLoading(it.isLoading)
        })
        viewModel.errorModel.observe(this, Observer {
            (playerFragment as VideoPlayerFragment).showErrorLayout(
                it.show,
                it.errorMsgId,
                it.errorCode
            )
        })
    }

    override fun onBackPressed() {
        enterPipModeOrExit()
    }

    private fun goFullScreen() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    override fun updateWatchedValue(content: Content) {
        viewModel.saveContent(content)
    }

    override fun playNextEpisode() {
        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.nextEpisodeUrl,
                episodeName = "$animeName (EP ${incrimentEpisodeNumber(content.episodeName!!)})",
                url = "",
                animeName = content.animeName
            )
        )
        viewModel.fetchEpisodeMediaUrl()

    }

    override fun playPreviousEpisode() {

        viewModel.updateEpisodeContent(
            Content(
                episodeUrl = content.previousEpisodeUrl,
                episodeName = "$animeName (EP ${decrimentEpisodeNumber(content.episodeName!!)})",
                url = "",
                animeName = content.animeName
            )
        )
        viewModel.fetchEpisodeMediaUrl()
    }

    private fun incrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber++
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    private fun decrimentEpisodeNumber(episodeName: String): String {
        return try {
            val episodeString = episodeName.substring(
                episodeName.lastIndexOf(' ') + 1,
                episodeName.lastIndexOf(')')
            )
            var episodeNumber = Integer.parseInt(episodeString)
            episodeNumber--
            episodeNumber.toString()

        } catch (obe: ArrayIndexOutOfBoundsException) {
            ""
        }
    }

    fun refreshM3u8Url() {

        viewModel.fetchEpisodeMediaUrl(fetchFromDb = false)
    }

}