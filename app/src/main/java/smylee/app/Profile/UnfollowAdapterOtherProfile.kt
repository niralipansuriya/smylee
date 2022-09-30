package smylee.app.Profile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.makeramen.roundedimageview.RoundedImageView
import smylee.app.model.FollowerResponse
import smylee.app.login.LoginActivity
import smylee.app.utils.Constants
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.R

class UnfollowAdapterOtherProfile(
    private var isShowTransparent: Boolean, context: Context,
    followerList: ArrayList<FollowerResponse>?,
    var USER_ID: String,
    followerMap: HashMap<String, String>,
    val manageClick: ManageClick
) : RecyclerView.Adapter<UnfollowAdapterOtherProfile.ViewHolder>() {

    interface ManageClick {
        fun managebuttonclick(IDp: String?, IS_UNFOLLOW: Boolean)
    }

    private var context: Context? = null
    private var follwerList: ArrayList<FollowerResponse>? = null
    private var followerMap: HashMap<String, String>? = null

    init {
        this.context = context
        this.follwerList = followerList
        this.followerMap = followerMap
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_follower_layout, parent, false)
        return ViewHolder(v)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(position, isShowTransparent, follwerList, context!!, manageClick, USER_ID, followerMap!!)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return follwerList!!.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(pos: Int, isShowTransparent: Boolean, followerList: ArrayList<FollowerResponse>?,
                      context: Context, manageClick: ManageClick, USER_ID: String, followerMap: HashMap<String, String>) {
            val btnFollow = itemView.findViewById(R.id.btn_follow) as TextView
            val btnUnfollow = itemView.findViewById(R.id.btn_unfollow) as TextView
            val ivVerified = itemView.findViewById(R.id.ivVerified) as AppCompatImageView

            if (followerList!![pos].mark_as_verified_badge != null && followerList[pos].mark_as_verified_badge == 1) {
                ivVerified.visibility = View.VISIBLE
            } else {
                ivVerified.visibility = View.GONE

            }
            if (!USER_ID.contentEquals("")) {
                if (USER_ID.contentEquals(followerList!![pos].other_user_id)) {
                    btnFollow.visibility = View.GONE
                    btnUnfollow.visibility = View.GONE
                }

                if (!USER_ID.contentEquals(followerList[pos].other_user_id)) {
                    var isFollowing = ""
                    for (key in followerMap.keys) {
                        isFollowing = followerMap[followerList[pos].other_user_id]!!
                    }
                    Log.d("IS_following====", isFollowing)
                    when {
                        isFollowing.contentEquals("1") -> {
                            btnFollow.visibility = View.GONE
                            btnUnfollow.visibility = View.VISIBLE
                        }
                        isFollowing.contentEquals("0") -> {
                            btnFollow.visibility = View.VISIBLE
                            btnUnfollow.visibility = View.GONE
                        }
                        else -> {
                            btnFollow.visibility = View.GONE
                            btnUnfollow.visibility = View.GONE
                        }
                    }
                }
            }

            btnFollow.setOnClickListener {
                val isLogin = SharedPreferenceUtils.getInstance(context).getBoolanValue(Constants.IS_LOGIN, false)
                if (isLogin) {
                    btnUnfollow.visibility = View.VISIBLE
                    btnFollow.visibility = View.GONE
                    followerMap[followerList!![pos].other_user_id] = "1"
                    manageClick.managebuttonclick(followerList[pos].other_user_id, false)
                } else {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra("screen_name", "unfollow")
                    context.startActivity(intent)
                }
            }

            btnUnfollow.setOnClickListener {
                val isLogin = SharedPreferenceUtils.getInstance(context).getBoolanValue(Constants.IS_LOGIN, false)
                if (isLogin) {
                    btnUnfollow.visibility = View.GONE
                    btnFollow.visibility = View.VISIBLE
                    followerMap[followerList!![pos].other_user_id] = "0"
                    manageClick.managebuttonclick(followerList[pos].other_user_id, true)
                } else {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra("screen_name", "unfollow")
                    context.startActivity(intent)
                }
            }

            val tvName = itemView.findViewById(R.id.tv_name) as AppCompatTextView
            tvName.text = followerList!![pos].profile_name
//            val tv_active = itemView.findViewById(R.id.tv_active) as TextView
            val ivPic = itemView.findViewById(R.id.iv_pic) as RoundedImageView

            if (!USER_ID.contentEquals(followerList[pos].other_user_id)) {
                ivPic.setOnClickListener {
                    val intent = Intent(context, OtherUserProfileActivity::class.java)
                    intent.putExtra("OTHER_USER_ID", followerList[pos].other_user_id)
                    context.startActivity(intent)
                }
            }

            Glide.with(context)
                .load(followerList[pos].profile_pic)
                .error(R.drawable.profile_thumb)
                .placeholder(R.drawable.profile_thumb)
                .into(ivPic)

            if (isShowTransparent) {
                val llMainFollowUnfollow = itemView.findViewById(R.id.llMainFollowUnfollow) as LinearLayout
                llMainFollowUnfollow.setBackgroundColor(Color.TRANSPARENT)
                llMainFollowUnfollow.setPadding(0, 0, 0, 0)
            }
        }
    }
}