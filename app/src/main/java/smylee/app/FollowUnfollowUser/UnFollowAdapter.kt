package smylee.app.FollowUnfollowUser

import smylee.app.ui.base.BaseActivity
import android.content.Intent
import android.graphics.Color
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
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.login.LoginActivity
import smylee.app.utils.Constants
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.R
import smylee.app.model.FollowerResponse

class UnFollowAdapter(
    var isShowTransparent: Boolean,
    context: BaseActivity,
    follwerList: ArrayList<FollowerResponse>?,
    follwerIdList: ArrayList<String>?,
    var USER_ID: String, var OTHER_USER_ID: String, followerMap: HashMap<String, String>,
    val manageClick: ManageClick
) : RecyclerView.Adapter<UnFollowAdapter.ViewHolder>() {

    //this method is returning the view for each item in the list

    interface ManageClick {
        //        public void manageRadioClick(int position, boolean is_check);
        fun managebuttonclick(
            IDp: String?,
            follwerList: ArrayList<FollowerResponse>?,
            OTHER_USER_ID: String,
            IS_UNFOLLOW: Boolean
        )
    }

    private var context: BaseActivity? = null

    private var follwerList: ArrayList<FollowerResponse>? = null
    private var follwerIdList: ArrayList<String>? = null
    private var followerMap: HashMap<String, String>? = null

    init {
        this.context = context
        this.follwerList = follwerList
        this.follwerIdList = follwerIdList
        this.followerMap = followerMap
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_unfollow, parent, false)
        return ViewHolder(
            v
        )
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(
            position,
            isShowTransparent,
            follwerList,
            context!!,
            manageClick,
            USER_ID, OTHER_USER_ID,
            followerMap!!
        )
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


    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return follwerList!!.size
        // return follwerIdList!!.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            pos: Int,
            isShowTransparent: Boolean,
            follwerList: ArrayList<FollowerResponse>?,
            context: BaseActivity,
            manageClick: ManageClick,
            USER_ID: String, OTHER_USER_ID: String,
            followerMap: HashMap<String, String>
        ) {
            val btn_follow = itemView.findViewById(R.id.btn_follow) as TextView
            val btn_unfollow = itemView.findViewById(R.id.btn_unfollow) as TextView
            val ivVerified = itemView.findViewById(R.id.ivVerified) as AppCompatImageView

            btn_unfollow.setOnClickListener {
                var IS_LOGIN = SharedPreferenceUtils.getInstance(context)
                    .getBoolanValue(Constants.IS_LOGIN, false)

                if (IS_LOGIN) {
                    var OtherUserId: String = ""
                    if (follwerList != null && follwerList.size > 0) {
                        OtherUserId = follwerList[pos].other_user_id.toString()
                        follwerList.remove(follwerList[pos])

                    }
                    manageClick.managebuttonclick(OtherUserId, follwerList, OTHER_USER_ID, true)

                } else {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra("screen_name", "unfollow")
                    context.startActivity(intent)
                    context.finish()
                }
            }


            val tv_name = itemView.findViewById(R.id.tv_name) as AppCompatTextView
            tv_name.setText(follwerList!!.get(pos).profile_name)

            val iv_pic = itemView.findViewById(R.id.iv_pic) as RoundedImageView


            if (follwerList[pos].mark_as_verified_badge != null && follwerList[pos].mark_as_verified_badge == 1) {
                ivVerified.visibility = View.VISIBLE
            } else {
                ivVerified.visibility = View.GONE

            }
            Glide.with(context!!)
                .load(follwerList!![pos].profile_pic)
                .error(R.drawable.profile_thumb)
                .placeholder(R.drawable.profile_thumb)
                .into(iv_pic)

            if (!USER_ID.contentEquals(follwerList[pos].other_user_id)) {
                iv_pic.setOnClickListener {
//                val intent = Intent(context, OtherProfile::class.java)
                    val intent = Intent(context, OtherUserProfileActivity::class.java)
                    intent.putExtra("OTHER_USER_ID", follwerList[pos].other_user_id)
                    context.startActivity(intent)

                }
            }




            if (isShowTransparent) {
                val llMainFollowUnfollow =
                    itemView.findViewById(R.id.llMainFollowUnfollow) as LinearLayout
                llMainFollowUnfollow.setBackgroundColor(Color.TRANSPARENT)
                llMainFollowUnfollow.setPadding(0, 0, 0, 0)
            }


        }
    }
}