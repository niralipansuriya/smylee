package smylee.app.ui.Activity

import smylee.app.ui.base.BaseActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.RawResourceDataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_login.*
import smylee.app.retrofitclient.APIConstants
import smylee.app.ChangePassword.ChangePasswordViewModel
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.ivVideoImage
import kotlinx.android.synthetic.main.activity_main.playerView
import org.json.JSONObject
import smylee.app.R

class ChangeMobile : BaseActivity() {

    private var changePasswordViewModel: ChangePasswordViewModel? = null
    private var simpleExoPlayer: SimpleExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        changePasswordViewModel =
            ViewModelProviders.of(this).get(ChangePasswordViewModel::class.java)
        btnChange.setOnClickListener {
            if (Utils.isTextEmpty(edtPhone)) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.change_phonne_validation)
                )
            } else if (!Utils.isValidMobile(edtPhone.text.toString().trim())) {
                Utils.showAlert(
                    context = this,
                    title = "",
                    message = getString(R.string.validate_number)
                )
            } else {
                changeMobileNumber()
            }
        }

        simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(this, DefaultTrackSelector())
        try {
            val defaultDataSourceFactory =
                DefaultDataSourceFactory(this, Util.getUserAgent(this, "smylee"))
            val extractorMediaSource: ExtractorMediaSource =
                ExtractorMediaSource.Factory(defaultDataSourceFactory).createMediaSource(
                    RawResourceDataSource.buildRawResourceUri(R.raw.splash_video_final)
                )

            simpleExoPlayer?.prepare(extractorMediaSource)
            playerView?.player = simpleExoPlayer
            simpleExoPlayer!!.playWhenReady = true
            simpleExoPlayer?.seekTo(0)
            simpleExoPlayer?.repeatMode = Player.REPEAT_MODE_ONE
            simpleExoPlayer?.addListener(listener)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            ivVideoImage.setImageResource(R.drawable.login_logo_final)
        }
    }

    var listener = object : Player.EventListener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.i("PlayerFragment", "isPlaying $isPlaying")
            if (isPlaying) {
                ivVideoImage.visibility = View.GONE
                // simpleExoPlayer?.seekTo(0)
            }
        }

        override fun onPlayerError(error: ExoPlaybackException) {
            error.printStackTrace()
        }
    }

    private fun changeMobileNumber() {
        val hashMap = HashMap<String, String>()
        hashMap["country_code"] = APIConstants.COUNTRY_CODE
        hashMap["phone_number"] = edtPhone.text.toString()
        changePasswordViewModel?.observeChangeMobile(this, hashMap)?.observe(this, Observer {
            if (it != null) {
                Logger.print("change mobile number response == >$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                    if (jsonObject.has("data")) {
                        val jsonObj1 = jsonObject.getJSONObject("data")
                        val countryCode = jsonObj1.getString("country_code")
                        val phoneNumber = jsonObj1.getString("phone_number")

                        if (!countryCode.contentEquals("")) {
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.changeCountryCode, countryCode)
                        }

                        if (!phoneNumber.contentEquals("")) {
                            SharedPreferenceUtils.getInstance(this)
                                .setValue(Constants.CHANGEDMOBILENUMBER, phoneNumber)
                        }

                        val intent = Intent(this, VerifyChangeMobile::class.java)
                        intent.putExtra("phone_number", edtPhone.text.toString())
                        startActivity(intent)
                        finish()
                    }
                } else if (code == 0) {
                    Utils.showToastMessage(this, jsonObject.getString("message"))
                }
            }
        })
    }
}