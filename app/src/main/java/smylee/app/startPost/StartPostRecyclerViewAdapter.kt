package smylee.app.startPost

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nex3z.flowlayout.FlowLayout
import smylee.app.model.CategoryResponse
import smylee.app.R

class StartPostRecyclerViewAdapter(context: Context, cat_files: ArrayList<CategoryResponse>,
    val manageClick: ManageClick) : RecyclerView.Adapter<StartPostRecyclerViewAdapter.ViewHolder>() {

    var context: Context? = null
    var userId: String = ""
    private var rowIndex1: Int = -1
    private var catFiles: ArrayList<CategoryResponse>? = null

    interface ManageClick {
        fun manageClick(catId: String?, cat_name: String?)
    }

    init {
        this.context = context
        this.catFiles = cat_files
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_start_post_category, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCat.text = catFiles!![position].category_name
        holder.tvCat.setOnClickListener {
            rowIndex1 = position
            notifyDataSetChanged()
            manageClick.manageClick(catFiles!![position].category_id, catFiles!![position].category_name)
        }

        if (rowIndex1 == position) {
            holder.flowLL.setBackgroundColor(ContextCompat.getColor(context!!, R.color.purple))
            holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.white))
        } else {
            holder.flowLL.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
            holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return catFiles!!.size
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    //the class is hodling the list view
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCat = itemView.findViewById(R.id.tv_cat) as AppCompatTextView
        val flowLL = itemView.findViewById(R.id.flowLL) as FlowLayout

        /*fun bindItems(pos: Int, cat_files: ArrayList<CategoryResponse>?, context: Context?, manageClick: ManageClick, rowIndex: Int) {

        }*/
    }
}