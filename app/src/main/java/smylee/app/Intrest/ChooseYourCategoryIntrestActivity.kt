package smylee.app.Intrest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_choose_your_category_intrest.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.custom.FlowLayout
import smylee.app.home.HomeActivity
import smylee.app.model.CategoryResponse
import smylee.app.retrofitclient.APIConstants
import smylee.app.selectcategory.SelectCategoryViewModel
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class ChooseYourCategoryIntrestActivity : BaseActivity(), View.OnClickListener {

    //    private var intrestCategoryAdapter: IntrestCategoryAdapter? = null
    private lateinit var viewModel: SelectCategoryViewModel
    private var list = ArrayList<CategoryResponse>()

    //    private var llm: GridLayoutManager? = null
    private var isShow: Boolean = false
    private var isVisible: String = ""
    private var postId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_your_category_intrest)
        viewModel = ViewModelProviders.of(this).get(SelectCategoryViewModel::class.java)

        if (intent != null) {
            isShow = intent.getBooleanExtra("is_show", false)
            if (intent.getStringExtra("is_visible") != null && !intent.getStringExtra("is_visible")!!
                    .contentEquals("")
            ) {
                isVisible = intent.getStringExtra("is_visible")!!
                Logger.print("is_visible===$isVisible")
            }
            if (intent.hasExtra("postId")) {
                postId = intent.getStringExtra("postId")!!
            }
        }

        if (isShow) {
            btn_submit.text = resources.getString(R.string.submit)

        }
        intrestCatIDS.clear()
        getCategorySelection()

        val spacing = 20 // 50px

        val includeEdge = false
        rvIntrestCategory.addItemDecoration(GridSpacingItemDecoration(2, spacing, includeEdge))

        btn_submit.setOnClickListener {
            Log.d("ADDED ID CAT", intrestCatIDS.toString())
            if (intrestCatIDS.size <= 0) {
                Utils.showAlert(this, "", resources.getString(R.string.select_category))
            } else {
                var finalCatIds: String
                finalCatIds = intrestCatIDS.toString()
                if (finalCatIds.contains("[")) {
                    finalCatIds = finalCatIds.replace("[", "")
                }
                if (finalCatIds.contains("]")) {
                    finalCatIds = finalCatIds.replace("]", "")
                }
                Logger.print("FINAL CATEGORY ID", finalCatIds)
                getUpdatedUserCategorySelection(finalCatIds)
            }
        }
    }

    private fun getUpdatedUserCategorySelection(category_list: String) {
        val hashMap = HashMap<String, String>()
        hashMap["category_list"] = category_list

        val apiName: String = APIConstants.UPDATEUSERCATEGORY
        viewModel.getUpdatedUserCategory(this, hashMap)
            .observe(this, Observer {
                if (it != null) {
                    Logger.print("UPDATEUSERCATEGORY Response : $it")
                    val jsonObject = JSONObject(it.toString())
                    when (jsonObject.getInt("code")) {
                        1 -> {
                            if (jsonObject.has("data")) {
                                val jsonObj1 = jsonObject.getJSONObject("data")
                                val categoryUpdated: String = jsonObj1.getString("category_list")
                                Logger.print("category_updated==================$categoryUpdated")
                                SharedPreferenceUtils.getInstance(this)
                                    .setValue(Constants.CATEGORY_PREF, categoryUpdated)
                            }

                            if (isVisible.contentEquals("0")) {
                                onBackPressed()
                            } else {
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.putExtra("screenName", "")
                                intent.putExtra("postId", postId)
                                startActivity(intent)
                                finish()
                            }

                        }
                        0 -> {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                        else -> {
                            Utils.showToastMessage(this, jsonObject["message"].toString())
                        }
                    }
                }
            })
    }

    private fun getCategorySelection() {
        val hashMap = HashMap<String, String>()
        val apiName: String = APIConstants.USERCATEGORY
        viewModel.getCategoryForUser(this, hashMap)
            .observe(this, Observer {
                if (it != null) {
                    Logger.print("getCategorySelection Response : $it")
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")

                    if (code == 1) {
                        list.clear()
                        list = Gson().fromJson(
                            jsonObject.getJSONArray("data").toString(),
                            object : TypeToken<ArrayList<CategoryResponse>>() {
                            }.type
                        )
                        if (list.size > 0) {
                            tv_nodata.visibility = View.GONE
                            rvIntrestCategory.visibility = View.VISIBLE
                            btn_submit.visibility = View.VISIBLE
                            if (isShow) {
                                for (i in list.indices) {
                                    if (list[i].is_selected == 1) {
                                        // intrestCatIDS.clear()
                                        intrestCatIDS.add(list[i].category_id.toString())
                                    }
                                }

                            } else {
                                for (i in list.indices) {
                                    intrestCatIDS.add(list[i].category_id!!)

                                }

                            }

                            setAdapter()
                        } else {
                            tv_nodata.visibility = View.VISIBLE
                            rvIntrestCategory.visibility = View.GONE
                            btn_submit.visibility = View.GONE
                        }
                    } else {
                        Utils.showToastMessage(this, jsonObject["message"].toString())
                    }
                }
            })
    }

    private fun setAdapter() {
        flowLayout.removeAllViews()
        val layoutParam = FlowLayout.LayoutParams(20, 10)

        Logger.print("intrestCatIDS !!!!!!!!!!!!!${intrestCatIDS.toString()}")
        for (i in 0 until list.size) {


            val view = LayoutInflater.from(this).inflate(R.layout.adapter_category_intrest, null)
            view.id = i
            val tvCat = view.findViewById(R.id.tv_cat) as AppCompatTextView
            val ivCatIcon = view.findViewById(R.id.ivCatIcon) as ImageView
            val llCate = view.findViewById(R.id.llCat) as LinearLayout
            val ivTick = view.findViewById(R.id.ivTick) as AppCompatImageView

            tvCat.text = list[i].category_name
            Glide.with(this)
                .load(list[i].category_icon)
                .placeholder(R.drawable.no_image_category)
                .error(R.drawable.no_image_category)
                .into(ivCatIcon)

            Logger.print("intrestCatIDS===================" + intrestCatIDS)
            if (intrestCatIDS.contains(list!![i].category_id.toString())) {
                //  llCate.background = resources.getDrawable(R.drawable.thik_purple_border, theme)
                llCate.background = ContextCompat.getDrawable(this, R.drawable.thik_purple_border)
                tvCat.setTextColor(ContextCompat.getColor(this, R.color.purple))
                ivTick.visibility = View.VISIBLE
                //tvCat. setTextColor(resources.getColor(R.color.purple))

            }

            view.setOnClickListener(this)
            view.layoutParams = layoutParam
            flowLayout.addView(view)
        }

    }

    companion object {
        var intrestCatIDS = ArrayList<String>()
    }

    override fun onClick(v: View?) {
        val llCate = v?.findViewById(R.id.llCat) as LinearLayout
        val tvCat = v?.findViewById(R.id.tv_cat) as AppCompatTextView
        val ivTick = v?.findViewById(R.id.ivTick) as AppCompatImageView
        if (intrestCatIDS.contains(list[v.id].category_id.toString())) {
            intrestCatIDS.remove(list[v.id].category_id)
            // llCate.background = resources.getDrawable(R.drawable.white_border, theme)
            llCate.background = ContextCompat.getDrawable(this, R.drawable.white_border)
            tvCat.setTextColor(ContextCompat.getColor(this, R.color.light_purple))
            ivTick.visibility = View.INVISIBLE
        } else {
            intrestCatIDS.add(list[v.id].category_id.toString())
            //llCate.background = resources.getDrawable(R.drawable.thik_purple_border, theme)
            llCate.background = ContextCompat.getDrawable(this, R.drawable.thik_purple_border)
            tvCat.setTextColor(ContextCompat.getColor(this, R.color.purple))
            ivTick.visibility = View.VISIBLE

        }
    }
}