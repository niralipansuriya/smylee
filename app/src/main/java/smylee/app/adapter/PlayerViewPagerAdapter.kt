package smylee.app.adapter

import smylee.app.ui.base.BaseActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import smylee.app.CallBacks.DoubleTapCallBacks
import smylee.app.model.ForYouResponse
import smylee.app.listener.OnVideoPlayingListener
import smylee.app.ui.Fragment.PlayerFragment
import smylee.app.utils.Logger

class PlayerViewPagerAdapter:FragmentStateAdapter {

    private val onVideoPlayingListener: OnVideoPlayingListener
    private val doubleTapCallBacks: DoubleTapCallBacks

    constructor(activity: BaseActivity, onVideoPlayingListener: OnVideoPlayingListener, doubleTapCallBacks: DoubleTapCallBacks) : super(activity) {
        this.onVideoPlayingListener = onVideoPlayingListener
        this.doubleTapCallBacks = doubleTapCallBacks
    }

    constructor(activity: Fragment, onVideoPlayingListener: OnVideoPlayingListener, doubleTapCallBacks: DoubleTapCallBacks) : super(activity) {
        this.onVideoPlayingListener = onVideoPlayingListener
        this.doubleTapCallBacks = doubleTapCallBacks
    }

    var fragments = ArrayList<PlayerFragment>()

    override fun getItemCount(): Int {
        Logger.print("Adapter getItemCount ${fragments.size}")
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        Logger.print("Adapter createFragment $position")
        return fragments[position]
    }

    /*fun addFragment(list: ArrayList<ForYouResponse>?) {
        list?.forEachIndexed { index, topic ->
            run {
                fragments.add(
                    PlayerFragment.newInstance(
                        index,
                        topic,
                        onVideoPlayingListener,
                        doubleTapCallBacks
                    )
                )
            }
        }
    }*/

    /*fun removeFragment(index: Int) {
        fragments.removeAt(index)
        notifyDataSetChanged()
    }*/

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun addFragment1(list: ArrayList<ForYouResponse>?): Int {
        val lastSize = fragments.size
        list?.forEachIndexed { index, any ->
            run {
                fragments.add(PlayerFragment.newInstance(index, any, onVideoPlayingListener, doubleTapCallBacks))
            }
        }
        return lastSize
    }

    fun removeAllFragment() {
        fragments.clear()
        notifyDataSetChanged()
    }
}