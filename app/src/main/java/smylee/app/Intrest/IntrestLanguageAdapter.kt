package smylee.app.Intrest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.FirebaseMessaging
import com.nex3z.flowlayout.FlowLayout
import smylee.app.R
import smylee.app.model.Videolanguageresponse
import smylee.app.utils.Logger

class IntrestLanguageAdapter(
    context: Context,
    cat_files: ArrayList<Videolanguageresponse>,
    val manageClick: IntrestLanguageAdapter.ManageClick
) : RecyclerView.Adapter<IntrestLanguageAdapter.ViewHolder>() {
    var context: Context? = null
    var USER_ID: String = ""
    var selected_item = 0

    private var cat_files: ArrayList<Videolanguageresponse>? = null

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
            .inflate(R.layout.adapter_language_intrest, parent, false)
        return ViewHolder(v)
    }

    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var isSelected: Boolean = false
        holder.tvCat.text = cat_files!![position].language_name

        if (ChooseYourLanguageIntrest.intrestLanguageId.contains(cat_files!![position].language_id.toString())) {
            holder.flowLL.background = ContextCompat.getDrawable(context!!, R.drawable.languge_selected_border)
            holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.white))
        }

        holder.flowLL.setOnClickListener {
            if (ChooseYourLanguageIntrest.intrestLanguageId.contains(cat_files!![position].language_id.toString())) {
                ChooseYourLanguageIntrest.intrestLanguageId.remove(cat_files!![position].language_id)
                holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.light_purple))

                holder.flowLL.background =
                    ContextCompat.getDrawable(context!!, R.drawable.white_border)

                FirebaseMessaging.getInstance()
                    .unsubscribeFromTopic(cat_files!![position].language_name.toString())
                    .addOnCompleteListener { task -> Logger.print("unsubscribeFromTopic !!!!!!!!!${task.isSuccessful}") }

                if (ChooseYourLanguageIntrest.intrestLanguageName.contains(cat_files!![position].language_name.toString())) {
                    ChooseYourLanguageIntrest.intrestLanguageName.remove(cat_files!![position].language_name)
                    ChooseYourLanguageIntrest.unSubscribeTopicList.add(cat_files!![position].language_name!!)
                }
            } else {
                ChooseYourLanguageIntrest.intrestLanguageId.add(cat_files!![position].language_id.toString())
                ChooseYourLanguageIntrest.intrestLanguageName.add(cat_files!![position].language_name.toString())
                holder.tvCat.setTextColor(ContextCompat.getColor(context!!, R.color.white))

                FirebaseMessaging.getInstance().subscribeToTopic("Android")
                    .addOnCompleteListener {
                    }
                FirebaseMessaging.getInstance()
                    .subscribeToTopic(cat_files!![position].language_name.toString())
                    .addOnCompleteListener { task ->
                        Logger.print("subscribeToTopic !!!!!!!!!${task.isSuccessful}")

                    }
                holder.flowLL.background =
                    ContextCompat.getDrawable(context!!, R.drawable.languge_selected_border)
            }

            manageClick.manageClick(cat_files!![position].language_id)
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
        val tvCat = itemView.findViewById(R.id.tv_cat) as AppCompatTextView
        val flowLL = itemView.findViewById(R.id.flowLL) as FlowLayout
//        var cardCategory = itemView.findViewById(R.id.cardCategory) as CardView

        /*fun bindItems(pos: Int, cat_files: ArrayList<CategoryResponse>?, context: Context?, manageClick: ManageClick, rowIndex: Int) {
        }*/
    }
}