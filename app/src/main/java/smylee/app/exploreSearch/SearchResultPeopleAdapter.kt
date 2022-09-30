package smylee.app.exploreSearch

import smylee.app.ui.base.BaseActivity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.login.LoginActivity
import smylee.app.utils.Constants
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.R
import smylee.app.model.SearchPeopleResponse

class SearchResultPeopleAdapter(
    var context: BaseActivity,
    var USER_ID: String,
    var searchResultList: ArrayList<SearchPeopleResponse>?,
    var followerMap: HashMap<String, String>,
    var manageClick: ManageClick
) : RecyclerView.Adapter<SearchResultPeopleAdapter.ViewHolder>() {

    interface ManageClick {
        fun onClick(User_Id: String?, IS_UNFOLLOW: Boolean)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(
            position: Int,
            searchResultList: ArrayList<SearchPeopleResponse>?,
            context: BaseActivity,
            manageClick: ManageClick,
            USER_ID: String,
            followerMap: HashMap<String, String>
        ) {
            val tv_name = itemView.findViewById(R.id.tv_name) as AppCompatTextView
            val btn_follow = itemView.findViewById(R.id.btn_follow) as AppCompatTextView
            val btn_unfollow = itemView.findViewById(R.id.btn_unfollow) as AppCompatTextView
            val ivVerified = itemView.findViewById(R.id.ivVerified) as AppCompatImageView
            val iv_pic = itemView.findViewById(R.id.iv_pic) as RoundedImageView
            val llMainFollowUnfollow =
                itemView.findViewById(R.id.llMainFollowUnfollow) as LinearLayout

            if (searchResultList != null && searchResultList.size > 0) {
                tv_name.text = searchResultList[position].profile_name.toString().trim()
                Glide.with(context)
                    .load(searchResultList[position].profile_pic)
                    .error(R.drawable.profile_thumb)
                    .placeholder(R.drawable.profile_thumb)
                    .into(iv_pic)

                if (searchResultList[position].mark_as_verified_badge != null && searchResultList[position].mark_as_verified_badge == 1) {
                    ivVerified.visibility = View.VISIBLE
                } else {
                    ivVerified.visibility = View.GONE

                }
                if (USER_ID.contentEquals(searchResultList[position].user_id.toString())) {
                    btn_unfollow.visibility = View.GONE
                    btn_follow.visibility = View.GONE
                } else {
                    var isFollowing = ""
                    for (key in followerMap.keys) {
                        println("Element at key $key : ${followerMap[key]}")
                        Log.d(
                            "likeunlike key===",
                            followerMap[searchResultList[position].user_id]!!
                        )
                        isFollowing = followerMap[searchResultList[position].user_id]!!
                    }
                    Log.d("IS_following====", isFollowing)

                    if (isFollowing.contentEquals("1")) {
                        btn_follow.visibility = View.GONE
                        btn_unfollow.visibility = View.VISIBLE
                    } else if (isFollowing.contentEquals("0")) {
                        btn_follow.visibility = View.VISIBLE
                        btn_unfollow.visibility = View.GONE
                    } else {
                        btn_follow.visibility = View.GONE
                        btn_unfollow.visibility = View.GONE
                    }
                }

                btn_unfollow.setOnClickListener {
                    val IS_LOGIN = SharedPreferenceUtils.getInstance(context)
                        .getBoolanValue(Constants.IS_LOGIN, false)

                    if (IS_LOGIN) {
                        btn_unfollow.visibility = View.GONE
                        btn_follow.visibility = View.VISIBLE
                        followerMap[searchResultList[position].user_id!!] = "0"
                        manageClick.onClick(searchResultList[position].user_id, true)
                    } else {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.putExtra("screen_name", "search")
                        context.startActivityForResult(
                            intent,
                            Constants.LOGIN_ACTIVITY_REQUEST_CODE
                        )
                    }
                }

                btn_follow.setOnClickListener {
                    var IS_LOGIN = SharedPreferenceUtils.getInstance(context)
                        .getBoolanValue(Constants.IS_LOGIN, false)

                    if (IS_LOGIN) {
                        btn_unfollow.visibility = View.VISIBLE
                        btn_follow.visibility = View.GONE

                        followerMap.put(searchResultList.get(position).user_id!!, "1")

                        manageClick.onClick(searchResultList.get(position).user_id, false)

                    } else {
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.putExtra("screen_name", "search")
                        context.startActivityForResult(
                            intent,
                            Constants.LOGIN_ACTIVITY_REQUEST_CODE
                        )
                    }

                }

                if (!USER_ID.contentEquals(searchResultList[position].user_id.toString())) {
                    llMainFollowUnfollow.setOnClickListener {
                        val intent = Intent(context, OtherUserProfileActivity::class.java)
                        intent.putExtra("OTHER_USER_ID", searchResultList[position].user_id)
                        intent.putExtra("is_search", true)
                        context.startActivity(intent)
                    }
                    /*iv_pic.setOnClickListener {
                        iv_pic.setOnClickListener {
                            if(context is HomeActivity) {
                                val homeActivity: HomeActivity = context
                                homeActivity.isAttachFragmentAgain = false
                            }
                            val intent = Intent(context, OtherUserProfileActivity::class.java)
                            intent.putExtra("OTHER_USER_ID",searchResultList[position].user_id)
                            intent.putExtra("is_search",true)
                            context.startActivity(intent)
                        }
                    }*/
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.adapter_search_people, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return searchResultList!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(position, searchResultList, context, manageClick, USER_ID, followerMap)
    }
}