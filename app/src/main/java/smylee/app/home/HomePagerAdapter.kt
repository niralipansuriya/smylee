package smylee.app.home

import android.os.Parcelable
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import smylee.app.utils.Logger

//class HomePagerAdapter (fm: FragmentManager?, var tabCount: Int) : FragmentStatePagerAdapter(fm!!) {
class HomePagerAdapter(fm: FragmentManager?, private var tabCount: Int, private var forYou_frg: String,
    private var following_frg: String, var postId: String) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var forYouFragmentHome: ForYouFragmentHome? = null
    private var followingFragment: FollowingFragment? = null
    private var tabHeight: Int = 0

    companion object {
        var isCallOnResume = 0
        var currentTab = -1
    }

    override fun getItem(position: Int): Fragment {
        Log.i("HomePagerAdapter", "getItem $position")
        Log.i("HomePagerAdapter", "postId $postId")
        when (position) {
            0 -> {
                if (forYouFragmentHome == null) {
                    forYouFragmentHome = ForYouFragmentHome()
                    forYouFragmentHome!!.setTabHeight(tabHeight)
                    forYouFragmentHome!!.getPostId(postId)
                } else {
                    forYouFragmentHome?.onResume()
                }
                return forYouFragmentHome!!
            }
            1 -> {
                if (followingFragment == null) {
                    followingFragment = FollowingFragment()
                    followingFragment!!.setTabHeight(tabHeight)
                } else {
                    followingFragment?.onResume()
                }
                return followingFragment!!
            }
            else -> {
                if (forYouFragmentHome == null) {
                    forYouFragmentHome = ForYouFragmentHome()
                    forYouFragmentHome!!.setTabHeight(tabHeight)
                    forYouFragmentHome!!.getPostId(postId)

                } else {
                    forYouFragmentHome?.onResume()
                }
                return forYouFragmentHome!!
            }
        }
    }

    override fun saveState(): Parcelable? {
        return null
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    fun onLoad(index: Int) {
        currentTab = index
        if (index == 0 && forYouFragmentHome != null) {
            forYouFragmentHome?.onResume()
        } else if (index == 1 && followingFragment != null) {
            followingFragment?.onResume()
        }
    }

    fun onStop(index: Int) {
        try {
            if (index == 0 && forYouFragmentHome != null) {
                forYouFragmentHome?.onStop()
            } else if (index == 1 && followingFragment != null) {
                followingFragment?.onStop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun callOnBackPressed(index: Int, activity: HomeActivity) {
        if (index == 0 && forYouFragmentHome != null) {
//            Logger.print("callOnBackPressed!!!!!!!!!!!!! forYouFragmentHome?.callOnBackPressed()")
            forYouFragmentHome?.onBackPressedCall(activity)
        } else if (index == 1 && followingFragment != null) {
            followingFragment?.onBackPressedCall(activity)
        }
    }

    fun callForBlock(index: Int) {
        if (index == 0 && forYouFragmentHome != null) {
            Logger.print("callForBlock!!!!!!!!!!!!! forYouFragmentHome?.callForBlock()")
            forYouFragmentHome?.callForBlock()
        } else if (index == 1 && followingFragment != null) {
            followingFragment?.callForBlockFollowing()
        }
    }

    fun playVideo(index: Int, isLoadData: Boolean) {
        if (index == 0 && forYouFragmentHome != null) {
//            Log.i("HomePagerAdapter", "playVideo $isLoadData")
            forYouFragmentHome?.playVideo(isLoadData, postId)
        } else if (index == 1 && followingFragment != null) {
            followingFragment?.playVideo()
        }
    }

    fun onStop() {
        try {
            if (forYouFragmentHome != null) {
                forYouFragmentHome?.onStop()
            }
            if (followingFragment != null) {
                followingFragment?.onStop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTabHeight(tabHeight: Int) {
//        Log.i("HomePagerAdapter", "Tab bar height $tabHeight")
        this.tabHeight = tabHeight
        if (forYouFragmentHome != null) {
            forYouFragmentHome?.setTabHeight(tabHeight)
        }
        if (followingFragment != null) {
            followingFragment?.setTabHeight(tabHeight)
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return forYou_frg
            1 -> return following_frg
        }
        return null
    }

    override fun getCount(): Int {
        return tabCount
    }
}