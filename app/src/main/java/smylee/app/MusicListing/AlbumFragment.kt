package smylee.app.MusicListing

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_album.*
import kotlinx.android.synthetic.main.fragment_album_type.*
import org.json.JSONObject
import smylee.app.R
import smylee.app.model.AlbumForYouResponse
import smylee.app.model.MusicAlbumResponse
import smylee.app.ui.base.BaseActivity
import smylee.app.utils.Logger
import smylee.app.utils.Utils


class AlbumFragment : Fragment() {

    private lateinit var musicListingViewModel: MusicListingViewModel
    lateinit var activity: MusicListingForYouAlbumActivity
    private var listMusicAlbum = ArrayList<MusicAlbumResponse>()
    private var audioList: java.util.ArrayList<AlbumForYouResponse> = java.util.ArrayList()

    private var isDataFinished: Boolean = false
    private var isFirstTimeView: Boolean = true
    var adapter: ViewPagerAdapter? = null

//    var currenttab: Int = 0

    companion object {
        var page_count = 1
        var currentAlbumTab: Int = 0
        var AlBumID = ArrayList<String>()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        musicListingViewModel = ViewModelProviders.of(this).get(MusicListingViewModel::class.java)

        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                Log.i("AlbumFragment", "onPageSelected position $position")
                val fragmentPage = adapter?.getItem(position) as AlbumTypeFragment
                audioList = listMusicAlbum[position].audio_list!!
                if (audioList.size > 0) {
                    fragmentPage.onTabChanged(audioList, listMusicAlbum[position].album_id)
                } else {
                    fragmentPage.onTabChanged(listMusicAlbum[position].album_id)
                }
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MusicListingForYouAlbumActivity
    }

    fun searchText(searchText: String) {
        Logger.print("searchText !!!!!!!!!!!!!!!!$searchText")

        BaseActivity.cancelPreparingAudio()
        BaseActivity.stopAudio()
        if (adapter != null && adapter?.getItem(currentAlbumTab) != null) {
            val fragmentPage = adapter?.getItem(currentAlbumTab) as AlbumTypeFragment
            audioList = listMusicAlbum[currentAlbumTab].audio_list!!
            if (audioList.size > 0) {
                fragmentPage.OnSearch(searchText)
            } else {
                fragmentPage.OnSearch(searchText)
            }
        }
    }


    fun onTabChanged() {
        BaseActivity.stopAudio()
        if (list != null && list.adapter != null) {
            val musicAlbumAdapter: MusicAlbumAdapter = list.adapter as MusicAlbumAdapter
            musicAlbumAdapter.onTabChange()
            list.adapter!!.notifyDataSetChanged()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser && isFirstTimeView) {
            adapter = null
            isFirstTimeView = false
            setUpViewPager(viewpager)
            tabs.setupWithViewPager(viewpager)
            if (tabs != null && viewpager != null) {
                tabs.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(viewpager) {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        super.onTabSelected(tab)
                        BaseActivity.stopAudio()

                        currentAlbumTab = tab.position
                        Logger.print("onTabSelected ==============" + tab.position)
                        if (adapter != null && adapter?.getItem(tab.position) != null) {
                            val fragmentPage = adapter?.getItem(tab.position) as AlbumTypeFragment
                            audioList = listMusicAlbum[tab.position].audio_list!!
                            if (audioList.size > 0) {
                                fragmentPage.onTabChanged(audioList, listMusicAlbum[tab.position].album_id)
                            } else {
                                fragmentPage.onTabChanged(listMusicAlbum[tab.position].album_id)
                            }
                        }
                    }

                    /*override fun onTabSelected(tab: TabLayout.Tab?) {
                        super.onTabSelected(tab)
                        BaseActivity.stopAudio()
                        *//*if (list.adapter != null) {
                            val musicAlbumAdapter: MusicAlbumAdapter = list.adapter as MusicAlbumAdapter
                            musicAlbumAdapter.onTabChange()
                            list.adapter!!.notifyDataSetChanged()
                        }*//*
                        currentAlbumTab = tab!!.position
                        Logger.print("onTabSelected ==============" + tab.position)

                        if (adapter != null && adapter?.getItem(tab.position) != null) {
                            val fragmentPage = adapter?.getItem(tab.position) as AlbumTypeFragment
                            audioList = listMusicAlbum[tab.position].audio_list!!

                            if (audioList != null && audioList.size > 0) {
                                fragmentPage.onTabChanged(
                                    audioList,
                                    listMusicAlbum[tab.position].album_id
                                )
                            } else {
                                fragmentPage.onTabChanged(listMusicAlbum[tab.position].album_id)
                            }
                        }
                    }*/
                })
            }
        }
    }

    private fun setUpViewPager(viewpager: ViewPager?) {
        val hashMap = HashMap<String, String>()
        hashMap["page_no"] = page_count.toString()
        hashMap["search_text"] = ""
        val isProgressShow = page_count == 1

        musicListingViewModel.observeAlbumAudioListing(activity, hashMap, isProgressShow).observe(this, Observer {
            if (it != null) {
                Logger.print("AlbumListing For You response =====$it")
                val jsonObject = JSONObject(it.toString())
                val code = jsonObject.getInt("code")
                if (code == 1) {
                    adapter = ViewPagerAdapter(fragmentManager!!)
//                    adapter = ViewPagerAdapter(childFragmentManager)
                    if (jsonObject.has("data")) {
                        val jsonobj1 = jsonObject.getJSONArray("data")
                        val array = Gson().fromJson<ArrayList<MusicAlbumResponse>>(jsonobj1.toString(),
                                object : TypeToken<ArrayList<MusicAlbumResponse>>() {}.type)

                        if (page_count == 1) {
                            listMusicAlbum.clear()
                        }
                        if (array.isNotEmpty()) {
                            listMusicAlbum.addAll(array)
                        } else {
                            isDataFinished = true
                        }

                        for (i in listMusicAlbum.indices) {
                            AlBumID.add(listMusicAlbum[i].album_id)
                            val album: String = listMusicAlbum[i].album_name

                            audioList = listMusicAlbum[i].audio_list!!
                            val bundl = Bundle()
                            bundl.putSerializable("album_list", audioList)
                            bundl.putString("album_ID", listMusicAlbum[i].album_id)
                            bundl.putString("searchText", "")
                            val albumTypeFrg = AlbumTypeFragment()
                            albumTypeFrg.arguments = bundl
                            adapter?.addFragment(albumTypeFrg, album)
                        }
                        viewpager!!.adapter = adapter
                    }
                } else if (code == 0) {
                    if (page_count == 1) {
                        Utils.showToastMessage(context = activity, message = jsonObject["message"].toString())
                    }
                } else {
                    if (page_count == 1) {
                        Utils.showToastMessage(context = activity, message = jsonObject["message"].toString())
                    }
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        page_count = 1
        return inflater.inflate(R.layout.fragment_album, container, false)
    }

//    class ViewPagerAdapter(manager: FragmentManager) : FragmentPagerAdapter(manager) {
    class ViewPagerAdapter(manager: FragmentManager) : FragmentStatePagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val mFragmentList: MutableList<Fragment> = ArrayList()
        private val mFragmentTitleList: MutableList<String> = ArrayList()

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }
}