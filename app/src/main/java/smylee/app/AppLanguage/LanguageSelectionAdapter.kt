package smylee.app.AppLanguage

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nex3z.flowlayout.FlowLayout
import smylee.app.R
import smylee.app.utils.Logger

class LanguageSelectionAdapter(context: Context, cat_files: ArrayList<String>, val manageClick: ManageClick) : RecyclerView.Adapter<LanguageSelectionAdapter.ViewHolder>() {
    var context: Context? = null
    var userID: String = ""

    private var catFiles: ArrayList<String>? = null
    private var rowIndex: Int = -1

    interface ManageClick {
        fun manageClick(catId: String?)
    }

    init {
        this.context = context
        this.catFiles = cat_files
    }

    //this method is returning the view for each item in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.adapter_language_intrest, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvCat.text = catFiles!![position]

        Logger.print("LanguageSelectionApp.languageID======================" + LanguageSelectionApp.languageID.toString())
        holder.flowLL.setOnClickListener {
            rowIndex = position
            notifyDataSetChanged()
            manageClick.manageClick(catFiles!![position])
            LanguageSelectionApp.languageID.clear()
        }

        if (LanguageSelectionApp.languageID.contains(catFiles!![position])) {
            Logger.print("contains iDS ========" + LanguageSelectionApp.languageID.toString())
            holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.white))
            holder.flowLL.background =
                ContextCompat.getDrawable(context!!, R.drawable.languge_selected_border)
        } else {
            if (rowIndex == position) {
                holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                holder.flowLL.background =
                    ContextCompat.getDrawable(context!!, R.drawable.languge_selected_border)

            } else {
                holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.light_purple))
                holder.flowLL.background =
                    ContextCompat.getDrawable(context!!, R.drawable.white_border)

            }
        }
    }

    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return catFiles!!.size
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCat = itemView.findViewById(R.id.tv_cat) as AppCompatTextView
        val flowLL = itemView.findViewById(R.id.flowLL) as FlowLayout
    }
}