package smylee.app.ui.Activity

import smylee.app.ui.base.BaseActivity
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.android.synthetic.main.display_url_activity.*
import smylee.app.R

class DisplayUrlActivity : BaseActivity() {
    private var url = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_url_activity)

        ivBack.setOnClickListener {
            onBackPressed()
        }

        if (intent != null) {
            url = intent.getStringExtra("url")!!
        }
        if (url != "" && !url.contentEquals("null")) {
            wbUrl.settings.javaScriptEnabled = true
            wbUrl.settings.domStorageEnabled = true
            wbUrl.webViewClient = WebViewClient()
            wbUrl.webChromeClient = WebChromeClient()
            progressDialog!!.show(false)
            wbUrl.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                    view.loadUrl(url)
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if (progressDialog!!.isShowing) {
                        progressDialog!!.hide()
                    }
                }

                override fun onReceivedError(view: WebView?, errorCode: Int, description: String, failingUrl: String?) {
                }
            }
            wbUrl.loadUrl(url)
        }
    }
}