package smylee.app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import smylee.app.MusicListing.AlbumFragment
import smylee.app.MusicListing.ForYouFragment

class PagerAlmubMusicListingAdapter(fm: FragmentManager?,var tabCount: Int, var forYouFrg: String, var albumfrg: String) : FragmentStatePagerAdapter(fm!!) {

    private var forYouFragment: ForYouFragment? = null
    private var albumFragment: AlbumFragment? = null

    //Overriding method getItem
    override fun getItem(position: Int): Fragment {
        //Returning the current tabs
        when (position) {
            0 -> {
                forYouFragment = ForYouFragment()
                return forYouFragment!!
            }
            1 -> {
                albumFragment = AlbumFragment()
                return albumFragment!!
            }
            else -> null
        }!!
    }

    override fun getPageTitle(position: Int): CharSequence? {
        //this is where you set the titles
        when (position) {
            0 -> return forYouFrg
            1 -> return albumfrg
        }
        return null
    }

    fun onSearch(searchText: String, position: Int) {
        if (position == 0) {
            if (forYouFragment != null) {
                forYouFragment!!.searchText(searchText)
            }
        } else {
            if (albumFragment != null) {
                albumFragment!!.searchText(searchText)
            }
        }
    }

    fun onTabChanged(oldTabPosition: Int) {
        if (oldTabPosition == 0) {
            if (forYouFragment != null) {
                forYouFragment!!.onTabChanged()
            }
        } else {
            if (albumFragment != null) {
                albumFragment!!.onTabChanged()
            }
        }
    }

    override fun getCount(): Int {
        return tabCount
    }
}