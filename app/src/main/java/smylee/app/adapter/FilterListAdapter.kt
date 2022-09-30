package smylee.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import smylee.app.camerfilters.OnFilterSelectListener
import smylee.app.model.FilterModel
import smylee.app.R

class FilterListAdapter(val context: Context, private val filterList: List<FilterModel>) :
    RecyclerView.Adapter<FilterListAdapter.ViewHolder>() {

    private var onFilterSelectListener: OnFilterSelectListener? = null
    var requestOptions = RequestOptions()

    init {
        requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(5));
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFilter = itemView.findViewById<AppCompatImageView>(R.id.imgFilter)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.filter_item_layout, null)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return filterList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(filterList[position].drawableId).apply(requestOptions)
            .into(holder.imgFilter)

        holder.imgFilter.setOnClickListener {
            if (onFilterSelectListener != null) {
                onFilterSelectListener?.onFilterSelected(position, filterList[position].filterType)
            }
        }
    }

    fun setOnFilterSelectListener(onFilterSelectListener: OnFilterSelectListener) {
        this.onFilterSelectListener = onFilterSelectListener
    }
}