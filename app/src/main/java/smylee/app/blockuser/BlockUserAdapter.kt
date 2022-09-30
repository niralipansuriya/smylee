package smylee.app.blockuser

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_other_user_profile.*
import smylee.app.R
import smylee.app.model.BlockuserResponse
import smylee.app.ui.Activity.FullScreenImage

class BlockUserAdapter(
    context: Context,
    blockuserlist: ArrayList<BlockuserResponse>?,
    val manageClick: ManageClick
) :
    RecyclerView.Adapter<BlockUserAdapter.ViewHolder>() {
    interface ManageClick {
        //        public void manageRadioClick(int position, boolean is_check);
        fun managebuttonclick(IDp: String?)
    }

    private var context: Context? = null

    private var blockuserlist: ArrayList<BlockuserResponse>? = null

    init {
        this.context = context
        this.blockuserlist = blockuserlist
    }


    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.adapter_blockuser, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.bindItems(position, blockuserlist, context!!, manageClick)
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return blockuserlist!!.size
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindItems(
            pos: Int,
            blockuserlist: ArrayList<BlockuserResponse>?,
            context: Context,
            manageClick: ManageClick
        ) {
            val tv_name = itemView.findViewById(R.id.tv_name) as TextView
            val btn_unblock = itemView.findViewById(R.id.btn_unblock) as TextView
            val ivVerified = itemView.findViewById(R.id.ivVerified) as AppCompatImageView
            btn_unblock.setOnClickListener {

                manageClick.managebuttonclick(blockuserlist!!.get(pos).getuser_id())

            }
            if (blockuserlist!![pos].getmark_as_verified_badge() != null && blockuserlist!![pos].getmark_as_verified_badge() == 1) {
                ivVerified.visibility = View.VISIBLE
            } else {
                ivVerified.visibility = View.GONE

            }
            val iv_pic = itemView.findViewById(R.id.iv_pic) as ImageView
            tv_name.setText(blockuserlist!![pos].getprofile_name())
            Glide.with(context!!)
                .load(blockuserlist[pos].getprofile_pic())
                .error(R.drawable.profile_thumb)
                .placeholder(R.drawable.profile_thumb)
                .into(iv_pic)


            iv_pic.setOnClickListener {
                if (blockuserlist[pos].getprofile_pic_compres() != null && blockuserlist[pos].getprofile_pic_compres() != "") {
                    val intent = Intent(context, FullScreenImage::class.java)
                    intent.putExtra("imageUrl", blockuserlist[pos].getprofile_pic_compres())
                    context.startActivity(intent)
                }
            }

        }


    }
}