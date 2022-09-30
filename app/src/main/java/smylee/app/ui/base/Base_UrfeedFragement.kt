package smylee.app.ui.base

import smylee.app.api.APIClient
import smylee.app.api.APIConst
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import smylee.app.utils.CustomLoaderDialog
import com.urfeed.listener.OnResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import smylee.app.R

open class Base_UrfeedFragement : Fragment() {

    var progressDialog: CustomLoaderDialog? = null
    private var isCallRefreshToken = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (progressDialog == null) {
            progressDialog = CustomLoaderDialog(context)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_base__urfeed_fragement, container, false)
    }

    fun callAPI(url: String, requestType: Int,
        progress: Boolean, isProgressCancelable: Boolean, params: HashMap<String, Any?>, onResponse: OnResponse) {
        if (!hasInternet()) {
            Toast.makeText(context, R.string.no_internet_available, Toast.LENGTH_SHORT).show()
            if (progressDialog?.isShowing!!) {
                progressDialog?.hide()
            }
            return
        }

        if (progress) {
            if (progressDialog?.isShowing!!) {
                progressDialog?.hide()
            }
            progressDialog?.show(isProgressCancelable)
        }

        val headerMap = HashMap<String, Any>()
        headerMap["language"] = "en"
        headerMap["auth_token"] = APIConst.BASIC_AUTH
        headerMap["Content-Type"] = "application/json"

        val call: Call<String>?
        call = when (requestType) {
            APIConst.POST -> {
                APIClient.getClient()?.postRequest(url, headerMap, JSONObject(params).toString())
            }
            APIConst.PUT -> {
                APIClient.getClient()?.putRequest(url, headerMap, JSONObject(params).toString())
            }
            else -> {
                APIClient.getClient()?.getRequest(url, headerMap, params)
            }
        }

        if (isCallRefreshToken) {
            progressDialog?.hide()
            return
        }

        call?.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                progressDialog?.hide()
                onResponse.onFailure(call, t)
            }

            override fun onResponse(call: Call<String>, response: Response<String>) {
                try {
                    progressDialog?.hide()
                    onResponse.onSuccess(response.code(), response.body().toString())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        })
    }

    private fun hasInternet(): Boolean {
        return true
    }
}