package smylee.app.blockuser

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_block_user.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.BlockuserResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.CustomPaginationDialog
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


class BlockUserActivity : BaseActivity() {
    lateinit var viewModel: BlockUserViewModel
    private var list = ArrayList<BlockuserResponse>()
    private var pageCount = 1
    private var cmpagination: CustomPaginationDialog? = null

    private var isDataFinished: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var currentVisibleCount: Int = 0
    private var blockuseradapter: BlockUserAdapter? = null

    private var totalItemCount: Int = 0
    private var paginationCount: Int = 0
    private var lastVisibleItem: Int = 0
    private var visibleThreshold = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block_user)
//        supportActionBar!!.hide()
        viewModel = ViewModelProviders.of(this).get(BlockUserViewModel::class.java)

        avatar.setOnClickListener {
            onBackPressed()
        }

        llm = LinearLayoutManager(this)

        var dividerItemDecoration = DividerItemDecoration(
            rv_blockuser.context,
            llm!!.orientation
        )
        //dividerItemDecoration.setDrawable(resources.getDrawable(R.drawable.recycler_horizontal_divider))
        dividerItemDecoration.setDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.recycler_horizontal_divider
            )!!
        )

        rv_blockuser.addItemDecoration(dividerItemDecoration)

        getBlockUsersList()
        //applyPagination()
    }

    /*override fun onResume() {
        super.onResume()

    }*/

    fun blockUnblockUsers(user_ID: String) {
        val isBlock = "0"
        val hashMap = HashMap<String, String>()
        hashMap["is_block"] = isBlock
        hashMap["other_user_id"] = user_ID

//        val apiName: String = APIConstants.BLOCKUNBLOCKUSER
        viewModel.blockunblockuser(this, hashMap, true).observe(this, Observer {
            if (it != null) {
                Logger.print("BLOCKUNBLOCKUSER Response : $it")
                val jsonObject = JSONObject(it.toString())

                if (jsonObject["code"] == 1) {
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_MYPROFILE, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_CHANGE_LIKE_OR_COMMENT_OR_FOLLOW, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_DISCOVER, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_TRENDING, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FOLLOWING_, true)
                    SharedPreferenceUtils.getInstance(this)
                        .setValue(Constants.IS_ANYTHING_CHANGE_FORYOU_FOR_BLOCK, true)
                    pageCount = 1
                    getBlockUsersList()
                }
            }
        })
    }

    private fun getBlockUsersList() {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        val isProgressShow = pageCount == 1

//        val apiName: String = APIConstants.GETBLOCKUSERS
        viewModel.getBlockusers(this, hashMap, isProgressShow).observe(this, Observer {
            removeLoaderView()

            if (it != null) {
                Logger.print("GETBLOCKUSERS Response : $it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")

                if (code == 1) {
                    //  list.clear()

                    if (jsonObject.has("pagination_count")) {
                        paginationCount = jsonObject.getInt("pagination_count")
                    }

                    if (jsonObject.has("data")) {


                        val array = Gson().fromJson<ArrayList<BlockuserResponse>>(
                            jsonObject.getJSONArray("data").toString(),
                            object : TypeToken<ArrayList<BlockuserResponse>>() {}.type
                        )



                        if (pageCount == 1) {
                            list.clear()
                        }


                        if (array.isNotEmpty()) {
                            list.addAll(array)
                        } else {
                            isDataFinished = true
                        }



                        if (list.size > 0) {
                            rv_blockuser.visibility = View.VISIBLE
                            tv_nodata_block.visibility = View.GONE
                            isLoading = false
                            setAdapter()
                        } else {
                            rv_blockuser.visibility = View.GONE
                            tv_nodata_block.visibility = View.VISIBLE
                        }

                    }


                    if (list.size > 0) {
                        if (list.size < paginationCount) {

                        } else {
                            applyPagination()
                        }

                    }
                } else if (code == 0) {

                    if (pageCount == 1) {
                        tv_nodata_block.visibility = View.VISIBLE
                        rv_blockuser.visibility = View.GONE

                    }
                }
            }

        })


    }

    private fun removeLoaderView() {
        if (cmpagination != null && cmpagination!!.isShowing) {
            cmpagination!!.hide()
        }

    }


    private fun addViewLoadingView() {
        if (cmpagination == null)
            cmpagination = CustomPaginationDialog(this)
        cmpagination!!.show(false)

    }

    private fun applyPagination() {
        rv_blockuser.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentVisibleCount = llm?.childCount!!
                totalItemCount = llm?.itemCount!!
                lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
                if (!isLoading) {
                    if (!isDataFinished) {

                        if (totalItemCount - currentVisibleCount <= lastVisibleItem + visibleThreshold) {
                            isLoading = true
                            pageCount++
                            if (dy > 0) {
                                addViewLoadingView()

                            }
                            Handler().postDelayed({ getBlockUsersList() }, Constants.DELAY)
                        }

                    } else {
                        println("........finished data..........")
                    }
                }
            }
        })
    }


    private fun setAdapter() {
        if (blockuseradapter == null) {
            llm = LinearLayoutManager(this)
            rv_blockuser.layoutManager = llm
            blockuseradapter =
                BlockUserAdapter(
                    this,
                    list,
                    object :
                        BlockUserAdapter.ManageClick {
                        override fun managebuttonclick(IDp: String?) {
                            blockUnblockUsers(IDp!!)
                        }
                    })
            rv_blockuser.adapter = blockuseradapter
        } else {
            blockuseradapter?.notifyDataSetChanged()
        }


    }

}