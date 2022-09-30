package smylee.app.home

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import smylee.app.utils.Methods
import kotlinx.android.synthetic.main.bottom_sheet_comment_layout.*
import org.json.JSONObject
import smylee.app.Profile.MyProfileFragment
import smylee.app.Profile.OtherUserProfileActivity
import smylee.app.R
import smylee.app.adapter.BottomSheetAdapter
import smylee.app.listener.OnCommentAdded
import smylee.app.login.LoginActivity
import smylee.app.model.CommentResponse
import smylee.app.playvideo.VideoDetailActivity
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Constants
import smylee.app.utils.Logger
import smylee.app.utils.SharedPreferenceUtils
import smylee.app.utils.Utils

class CommentBottomSheet(private val onCommentAdded: OnCommentAdded): BottomSheetDialogFragment() {

    lateinit var viewModel: HomeViewModel
    lateinit var activity: BaseActivity
    private var pageCount = 1
    private var commentLikeMap: HashMap<String, String> = HashMap()
    private var commentHasLikeMap: HashMap<String, String> = HashMap()
    private var commentsList = ArrayList<CommentResponse>()

    private var isDataFinishedComment: Boolean = false
    private var isLoading: Boolean = false
    private var llm: LinearLayoutManager? = null
    private var lastVisibleItem: Int = 0
    var userId: String = ""
    private var bottomSheetAdapter: BottomSheetAdapter? = null
    private var hasLikedComment: Int = 2
    var commentSpamID: String = ""
    private var currentVisibleCount: Int = 0
    private var totalItemCount: Int = 0
    private var postId: Int = 0
    private var commentsCount: String = "0"

    private var isAddComment: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_comment_layout, container)
        viewModel = ViewModelProviders.of(activity).get(HomeViewModel::class.java)

        if(arguments != null) {
            userId = arguments!!.getString("userId", "")!!
            Logger.print("userId comment bottomsheet ============$userId")
            postId = arguments!!.getInt("postId")
            commentsCount = arguments!!.getString("commentsCount", "")
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getComments(rv_comments, postId, false)

        Methods.showCommentCountsDialog(commentsCount, comments, activity)
        iv_send.setOnClickListener {
            val isLogin = SharedPreferenceUtils.getInstance(activity).getBoolanValue(Constants.IS_LOGIN, false)
            if (Utils.isTextEmpty(edt_comment)) {
                Utils.showAlert(activity, "", resources.getString(R.string.enter_comment))
            } else {
                if (isLogin) {
                    if (edt_comment != null && postId != 0) {
                        addComments(edt_comment!!.text.toString().trim(), edt_comment!!, postId)
                    }
                } else {
                    if(activity is HomeActivity) {
                        val homeActivity = activity as HomeActivity
                        homeActivity.isCallLogin = true
                    }
                    val intent = Intent(getActivity(), LoginActivity::class.java)
                    intent.putExtra("screen_name", "forYou")
                    activity.startActivityForResult(intent, Constants.LOGIN_ACTIVITY_REQUEST_CODE)
                }
            }
        }

        iv_close.setOnClickListener {
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog: BottomSheetDialog =
            super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        bottomSheetDialog.setOnShowListener {
            val bottomSheet: FrameLayout = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from<View>(bottomSheet)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return bottomSheetDialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as BaseActivity
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Logger.print("CommentBottomSheet onCancel")
        onCommentAdded.onDialogDismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Logger.print("CommentBottomSheet onDismiss")
        onCommentAdded.onDialogDismiss()
    }

    private fun getComments(rv_comments: RecyclerView, Post_ID: Int, isAddComment: Boolean) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = pageCount.toString()
        hashMap["post_id"] = Post_ID.toString()

        viewModel.getComments(activity, hashMap, false).observe(this, Observer {
            if (it != null) {
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    if (jsonObject.has("data")) {
                        val jsonArray = jsonObject.getJSONArray("data")
                        val array = Gson().fromJson<ArrayList<CommentResponse>>(jsonArray.toString(),
                                object : TypeToken<ArrayList<CommentResponse>>() {}.type)
                        if (pageCount == 1) {
                            commentsList.clear()
                            commentLikeMap.clear()
                            commentHasLikeMap.clear()
                        }
                        if (array.isNotEmpty()) {
                            isDataFinishedComment = false
                            array.reverse()
                            commentsList.addAll(0, array)

                            for (i in commentsList.indices) {
                                if (commentsList[i].comment_like_count == null) {
                                    commentLikeMap[commentsList[i].post_comment_id.toString()] = "0"
                                } else {
                                    commentLikeMap[commentsList[i].post_comment_id.toString()] = commentsList[i].comment_like_count.toString()
                                }
                                if (commentsList[i].post_comment_id != null && commentsList[i].has_liked != null) {
                                    commentHasLikeMap[commentsList[i].post_comment_id.toString()] = commentsList[i].has_liked.toString()
                                }
                            }
                        } else {
                            isDataFinishedComment = true
                        }

                        if (commentsList.size > 0) {
                            rv_comments.visibility = View.VISIBLE
                            isLoading = false
                            setAdapter(rv_comments, Post_ID, commentHasLikeMap, commentLikeMap, userId, isAddComment)
                        } else {
                            rv_comments.visibility = View.GONE
                        }
                    }

                    if (commentsList.size < jsonObject.getInt("pagination_count")) {
                        isDataFinishedComment = true
                    } else {
                        isDataFinishedComment = false
                        applyPagination(rv_comments, Post_ID)
                    }
                } else if (code == 0) {
                    if (pageCount == 1) {
                        rv_comments.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun setAdapter(rv_comments: RecyclerView, Post_ID: Int?, commentHasLikeMap: HashMap<String, String>,
                           commentLikeMap: HashMap<String, String>, userId: String, isAddComment: Boolean) {
        if (bottomSheetAdapter == null) {
            llm = LinearLayoutManager(context)
            rv_comments.layoutManager = llm
            bottomSheetAdapter = BottomSheetAdapter(activity, Post_ID, userId, commentHasLikeMap, commentLikeMap,
                commentsList, object : BottomSheetAdapter.ManageClick {
                    override fun likeUnlike(IDp: Int?, like_counts: String?, has_liked: Int?, Post_ID: Int?, like_img: ImageView,
                                            tv_like_count: TextView, commentLikecountsInitial: String, CommentHasLikeInitial: String, position: Int) {
                        var commentLikeCountsInitial = ""
                        for (key in commentLikeMap.keys) {
                            commentLikeCountsInitial = commentLikeMap[commentsList[position].post_comment_id.toString()]!!
                        }
                        var commentHasLikeInitial = ""
                        for (key in commentHasLikeMap.keys) {
                            commentHasLikeInitial = commentHasLikeMap[commentsList[position].post_comment_id.toString()]!!
                        }

                        if (commentHasLikeInitial.contentEquals("0")) {
                            hasLikedComment = 1
                        } else if (commentHasLikeInitial.contentEquals("1")) {
                            hasLikedComment = 0
                        }

                        if (hasLikedComment == 1) {
                            like_img.setImageResource(R.drawable.likecomment)
                            var likeCountComments: Int = commentLikeCountsInitial.toInt()
                            likeCountComments += 1
                            if (likeCountComments.toString() != "") {
                                Methods.showCommentLikeCounts(likeCountComments.toString(), tv_like_count)
                            }
                            commentLikeMap[commentsList[position].post_comment_id.toString()] = likeCountComments.toString()
                            commentHasLikeMap[commentsList[position].post_comment_id.toString()] = "1"
                        }

                        if (hasLikedComment == 0) {
                            like_img.setImageResource(R.drawable.unlikecomment)
                            if (like_counts != null) {
                                var likeCountComments: Int = commentLikeCountsInitial.toInt()
                                if (likeCountComments != 0) {
                                    likeCountComments -= 1
                                }
                                if (likeCountComments.toString() != "") {
                                    Methods.showCommentLikeCounts(likeCountComments.toString(), tv_like_count)
                                }
                                commentLikeMap[commentsList[position].post_comment_id.toString()] = likeCountComments.toString()
                                commentHasLikeMap[commentsList[position].post_comment_id.toString()] = "0"
                            }
                        }
                        likeUnlikeComments(IDp, hasLikedComment)
                    }
                },
                object : BottomSheetAdapter.ManageClickProfile {
                    override fun clickUserProfile(userId: String) {
                        if (!this@CommentBottomSheet.userId.contentEquals(userId)) {
                            val intent = Intent(activity, OtherUserProfileActivity::class.java)
                            intent.putExtra("OTHER_USER_ID", userId)
                            startActivity(intent)
                        } else {
                            /* if (VideoDetailActivity.videoDetailActivity!=null)
                             {
                                 VideoDetailActivity.videoDetailActivity!!.finish()
                             }*/
                            if (this@CommentBottomSheet != null && this@CommentBottomSheet.isVisible) {
                                this@CommentBottomSheet.dismiss()
                            }

                            onCommentAdded.onSelfProfileClick()

                            /*if (activity is HomeActivity) {
                                val homeActivity = activity as HomeActivity
                                homeActivity.hideTabBar()
                                homeActivity.setTabUI(false)
                                homeActivity.setSelected()
                            }
                            val fragment: Fragment
                            fragment = MyProfileFragment()
                            fragmentManager!!.beginTransaction().replace(R.id.container, fragment)
                                .commit()*/
                        }
                    }

                    override fun spamComment(commentId: String) {
                        commentSpamID = commentId
                        onCommentAdded.onSpamComment(commentId)
//                        ForYouFragmentHome.reportBehaviour?.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                })
            rv_comments.adapter = bottomSheetAdapter

            if (isAddComment) {
                rv_comments.scrollToPosition(commentsList.size - 1)
            }
        } else {
            bottomSheetAdapter?.notifyDataSetChanged()
            if (isAddComment) {
                rv_comments.scrollToPosition(commentsList.size - 1)
            }
        }
    }

    private fun applyPagination(rv_comments: RecyclerView, Post_ID: Int) {
        rv_comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                currentVisibleCount = llm?.childCount!!
                totalItemCount = llm?.itemCount!!
                lastVisibleItem = llm?.findFirstVisibleItemPosition()!!
                if (!isLoading) {
                    if (!isDataFinishedComment) {
                        if (lastVisibleItem == 0) {
                            isLoading = true
                            pageCount++
                            addViewLoadingView(rv_comments)
                            Handler().postDelayed({
                                getComments(rv_comments, Post_ID, false)
                            }, Constants.DELAY)
                        }
                    }
                }
            }
        })
    }

    private fun addViewLoadingView(rv_comments: RecyclerView) {
        if (commentsList.size > 0) {
            rv_comments.post {
                if (bottomSheetAdapter != null) {
                    bottomSheetAdapter?.notifyDataSetChanged()
                }
            }
        }
    }

    private fun likeUnlikeComments(IDp: Int?, has_liked_comment: Int?) {
        val hashMap = HashMap<String, String>()
        hashMap["is_like"] = has_liked_comment.toString()
        hashMap["post_id"] = postId.toString()
        hashMap["comment_id"] = IDp.toString()

        viewModel.LikeUnlikecomment(activity, hashMap, false).observe(
            this, Observer {
                if (it != null) {
                    val jsonObject = JSONObject(it.toString())
                    val code = jsonObject.getInt("code")
                    if (code == 0) {
                        Utils.showAlert(activity, "", jsonObject["message"].toString())
                    }
                }
            }
        )
    }

    private fun addComments(comment: String, edt_comment: AppCompatEditText, Post_ID: Int) {
        val hashMap = HashMap<String, String>()
        hashMap["comment_text"] = comment
        hashMap["post_id"] = Post_ID.toString()
        viewModel.AddComments(activity, hashMap, true).observe(this, Observer {
            if (it != null) {
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    edt_comment.setText("")
                    if (jsonObject.has("data")) {
                        val commentsCount = Integer.parseInt(commentsCount) + 1
                        Methods.showCommentCountsDialog(commentsCount.toString(),comments!!,context)
                        onCommentAdded.onCommentAdded(postId, comments!!)
                    }
                    isAddComment = true
                    pageCount = 1
                    getComments(rv_comments!!, Post_ID, isAddComment)
                } else if (code == 0) {
                    isAddComment = false
                    Utils.showAlert(activity, "", jsonObject["message"].toString())
                    edt_comment.setText("")
                }
            }
        })
    }
    
    companion object {
        fun newInstance(userId: String, postId: Int, commentsCount: String, onCommentAdded: OnCommentAdded): CommentBottomSheet {
            val args = Bundle()
            args.putString("userId", userId)
            args.putInt("postId", postId)
            args.putString("commentsCount", commentsCount)
            val fragment = CommentBottomSheet(onCommentAdded)
            fragment.arguments = args
            return fragment
        }
    }
}