package smylee.app.AppLanguage

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_language_selection_app.*
import smylee.app.R
import smylee.app.VideoLanguageSelection.VideoSelectionViewModel
import smylee.app.home.HomeActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*

class LanguageSelectionApp : BaseActivity() {

    private lateinit var viewModel: VideoSelectionViewModel
    private var llm: GridLayoutManager? = null
    private var languageSelectionAdapter: LanguageSelectionAdapter? = null

    var selectedAppLanguage: String = ""

    private val appLanguageList = ArrayList<String>()
    private val appLanguageIDList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language_selection_app)
        viewModel = ViewModelProviders.of(this).get(VideoSelectionViewModel::class.java)

        appLanguageList.add("English")
        appLanguageList.add("Hindi")
        appLanguageList.add("Gujarati")
        appLanguageList.add("Marathi")
        appLanguageList.add("Odia")
        appLanguageList.add("Telugu")

        appLanguageIDList.add("1")
        appLanguageIDList.add("2")
        appLanguageIDList.add("3")
        appLanguageIDList.add("4")
        appLanguageIDList.add("5")
        appLanguageIDList.add("6")

        val appLanguageIDS = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                .getStringValue(Constants.LANGUAGE_APP_ID, "")!!

        languageID.add(appLanguageIDS)
        val spacing = 40 // 50px
        val includeEdge = false
        rvAppLanguage.addItemDecoration(GridSpacingItemDecoration(2, spacing, includeEdge))

        if (appLanguageList.size > 0) {
            btn_submit.visibility = View.VISIBLE
            tv_nodata.visibility = View.GONE
        } else {
            btn_submit.visibility = View.GONE
            tv_nodata.visibility = View.VISIBLE
        }

        setAdapter()
        val selectedLangIDPref = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
            .getStringValue(Constants.LANGUAGE_APP_ID, "")!!
        selectedAppLanguage = selectedLangIDPref
        btn_submit.setOnClickListener {
            if (!selectedAppLanguage.contentEquals("")) {
                SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_APP_ID, selectedAppLanguage)
                val selectedLangIDPref = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                        .getStringValue(Constants.LANGUAGE_APP_ID, "")!!

                languageID.clear()
                languageID.add(selectedLangIDPref)
                if (selectedAppLanguage.contentEquals("Telugu")) {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "TE")
                    val currentLanguage: String =
                        SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
                    Logger.print("currentLanguage===================$currentLanguage")
                    applyLocalization(currentLanguage)
                }

                //else if (SELECTED_APP_LANGUAGE.contentEquals("5"))
                else if (selectedAppLanguage.contentEquals("Marathi")) {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "MR")
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
                    Logger.print("currentLanguage===================$currentLanguage")
                    applyLocalization(currentLanguage)
                }
                // else if (SELECTED_APP_LANGUAGE.contentEquals("9"))
                else if (selectedAppLanguage.contentEquals("Odia")) {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "ORY")
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!
                    Logger.print("currentLanguage===================$currentLanguage")
                    applyLocalization(currentLanguage)
                } else if (selectedAppLanguage.contentEquals("Hindi")) {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "HI")
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!

                    Logger.print("currentLanguage===================$currentLanguage")
                    applyLocalization(currentLanguage)
                } else if (selectedAppLanguage.contentEquals("Gujarati")) {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "GU")
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!

                    Logger.print("currentLanguage===================$currentLanguage")
                    applyLocalization(currentLanguage)
                } else {
                    SharedPreferenceUtils.getInstance(this@LanguageSelectionApp).setValue(Constants.LANGUAGE_CODE_PREF, "EN")
                    val currentLanguage: String = SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                            .getStringValue(Constants.LANGUAGE_CODE_PREF, "")!!

                    val myLocale = Locale(currentLanguage)
                    val res: Resources = resources
                    val dm: DisplayMetrics = res.displayMetrics
                    val conf: Configuration = res.configuration
                    conf.locale = myLocale
                    Locale.setDefault(conf.locale)
                    res.updateConfiguration(conf, dm)

                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("screenName", "profile+settings")
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            } else {
                Utils.showToastMessage(this, resources.getString(R.string.select_language))
            }
        }
    }


    private fun applyLocalization(currentLanguage: String) {
        val myLocale = Locale(currentLanguage)
        val res: Resources = resources
        val dm: DisplayMetrics = res.displayMetrics
        val conf: Configuration = res.configuration
        conf.locale = myLocale
        res.updateConfiguration(conf, dm)
        /*if (SettingActivity.settingActivity != null) {
            SettingActivity.settingActivity?.finish()
        }
        val refresh = Intent(this, SettingActivity::class.java)
        finish()
        startActivity(refresh)*/

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("screenName", "profile+settings")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    /*override fun onBackPressed() {
        super.onBackPressed()
        val refresh = Intent(this, SettingActivity::class.java)
        finish()
        startActivity(refresh)
    }*/

    /*private fun getVideoSelectionLanguage() {
        val hashMap = HashMap<String, String>()
        viewModel.getVideoSelectionlanguage(this, hashMap).observe(this, Observer {
            if (it != null) {
                Logger.print("App language selection  Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    languageList.clear()
                    languageList = Gson().fromJson(
                        jsonObject.getJSONArray("data").toString(),
                        object : TypeToken<ArrayList<Videolanguageresponse>>() {}.type
                    )
                    if (languageList.size > 0) {
                        tv_nodata.visibility = View.GONE
                        rvAppLanguage.visibility = View.VISIBLE
                        btn_submit.visibility = View.VISIBLE
                        val selectedLangIDPref =
                            SharedPreferenceUtils.getInstance(this@LanguageSelectionApp)
                                .getStringValue(Constants.LANGUAGE_APP_ID, "")!!
                        languageID.clear()
                        languageID.add(selectedLangIDPref)

                        setAdapter()
                    } else {
                        tv_nodata.visibility = View.VISIBLE
                        rvAppLanguage.visibility = View.GONE
                        btn_submit.visibility = View.GONE
                    }
                } else {
                    Utils.showToastMessage(this, jsonObject["message"].toString())
                }
            }
        })
    }*/

    private fun setAdapter() {
        if (languageSelectionAdapter == null) {
            llm = GridLayoutManager(this, 2)
            rvAppLanguage.layoutManager = llm
            languageSelectionAdapter = LanguageSelectionAdapter(this, appLanguageList, object :
                LanguageSelectionAdapter.ManageClick {
                override fun manageClick(catId: String?) {
                    Logger.print("signle selected language *******************$catId")
                    selectedAppLanguage = catId!!
                }
            })
            rvAppLanguage.adapter = languageSelectionAdapter
        } else {
            languageSelectionAdapter?.notifyDataSetChanged()
        }
    }
//    private fun setAdapter() {
//        if (languageSelectionAdapter == null) {
//            llm = GridLayoutManager(this, 2)
//            rvAppLanguage.layoutManager = llm
//            languageSelectionAdapter = LanguageSelectionAdapter(this, languageList, object :
//                LanguageSelectionAdapter.ManageClick {
//                override fun manageClick(catId: String?) {
//                    Logger.print("signle selected language *******************$catId")
//                    SELECTED_APP_LANGUAGE = catId!!
//                }
//            })
//            rvAppLanguage.adapter = languageSelectionAdapter
//        } else {
//            languageSelectionAdapter?.notifyDataSetChanged()
//        }
//    }

    companion object {
        var languageID = ArrayList<String>()
    }
}