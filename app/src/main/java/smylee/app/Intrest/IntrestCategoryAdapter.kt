package smylee.app.Intrest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import smylee.app.R
import smylee.app.model.CategoryResponse
import smylee.app.utils.Logger

class IntrestCategoryAdapter(
    context: Context,
    cat_files: ArrayList<CategoryResponse>,
    val manageClick: ManageClick
) : RecyclerView.Adapter<IntrestCategoryAdapter.ViewHolder>() {
    var context: Context? = null
    var USER_ID: String = ""
    var selected_item = 0

    private var cat_files: ArrayList<CategoryResponse>? = null

    interface ManageClick {
        fun manageClick(catId: String?)
    }

    init {
        this.context = context
        this.cat_files = cat_files
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_category_intrest, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var isSelected: Boolean = false
        holder.tv_cat.text = cat_files!![position].category_name

        Glide.with(context!!)
            .load(cat_files!![position].category_icon)
            .into(holder.ivCatIcon)

        holder.llCat.setOnClickListener {

            if (ChooseYourCategoryIntrestActivity.intrestCatIDS.contains(cat_files!![position].category_id.toString())) {
                ChooseYourCategoryIntrestActivity.intrestCatIDS.remove(cat_files!![position].category_id)
                holder.llCat.background =
                    ContextCompat.getDrawable(context!!, R.drawable.white_border)

            } else {
                ChooseYourCategoryIntrestActivity.intrestCatIDS.add(cat_files!![position].category_id.toString())
                holder.llCat.background =
                    ContextCompat.getDrawable(context!!, R.drawable.thik_purple_border)


            }
            Logger.print("Intrest Category IDS =================" + ChooseYourCategoryIntrestActivity.intrestCatIDS)

            manageClick.manageClick(cat_files!![position].category_id)
        }


    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return cat_files!!.size
    }


    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_cat = itemView.findViewById(R.id.tv_cat) as AppCompatTextView
        var cardCategory = itemView.findViewById(R.id.cardCategory) as CardView
        var llCat = itemView.findViewById(R.id.llCat) as LinearLayout
        val ivCatIcon = itemView.findViewById(R.id.ivCatIcon) as ImageView


        fun bindItems(
            pos: Int,
            cat_files: ArrayList<CategoryResponse>?,
            context: Context?, manageClick: ManageClick, rowIndex: Int
        ) {

        }
    }
}