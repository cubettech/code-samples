package com.android.pause.ui.home.trip

import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.android.pause.R
import com.android.pause.customviews.SwitchButton
import com.android.pause.data.model.itineraries.ItineraryListItem
import com.android.pause.data.model.itineraries.ItineraryResponse
import com.android.pause.ui.AppConstants
import com.android.pause.ui.base.BaseActivity
import com.android.pause.ui.home.map.MapActivity
import com.android.pause.ui.home.reminders.ReminderActivity
import com.android.pause.ui.home.trip.adapter.TripPagerAdapter
import com.android.pause.ui.home.trip.fragments.GuideBookFragment
import com.android.pause.ui.home.trip.fragments.ItineraryFragment
import com.android.pause.util.ConnectivityChecker
import com.android.pause.util.ObjectConverter
import com.github.florent37.viewanimator.ViewAnimator
import kotlinx.android.synthetic.main.activity_trip_details_new.*
import kotlinx.android.synthetic.main.header_trip_details.*
import kotlinx.android.synthetic.main.item_tab.view.*
import org.json.JSONObject
import javax.inject.Inject


class TripDetailsActivity : BaseActivity(), TripDetailsView, View.OnClickListener, TabLayout.OnTabSelectedListener, SwitchButton.OnCheckedChangeListener {
    override fun offlineDowmloadError(itineraryId: String?) {
        if(sthDownload.isChecked){
            sthDownload.setOnCheckedChangeListener(null)
            sthDownload.isChecked = false
            mPresenter.saveItineraryOfflineId(itineraryId)
            switchHandler.postDelayed({sthDownload.setOnCheckedChangeListener(this)}, 500)
        }
    }

    @Inject
    lateinit var mPresenter: TripPresenter
    private lateinit var itineraryListItem: ItineraryListItem
    private val keyMap:MutableList<String> by lazy { ArrayList<String>() }
    private lateinit var str:String
    private var pos = 0
    val adapter = TripPagerAdapter(supportFragmentManager)
    private val switchHandler by lazy { Handler() }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTranslucentStatusBar(window)
        //window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_details_new)
        activityComponent().inject(this)
        mPresenter.attachView(this)
        pos = intent.getIntExtra("pos", 0)
        str = intent.getStringExtra(AppConstants.BundleExtras.KEY_ITINERARY)
        Handler().post { init() }
    }

    private fun init(){
        itineraryListItem = ObjectConverter.convertToObject(str, ItineraryListItem::class.java) as ItineraryListItem
        ViewAnimator.animate(constraintLayout)
                .fadeIn()
                .decelerate()
                .duration(600)
                .start()
        tvTripTitle.text = itineraryListItem.itineraryName()

        /* Itinerary item using dynamic keys for listing activities under a date. In order to fetch the keys
         * we need to convert it to JSONObject first which will have a function to return all keys. Later we can
         * iterate through this key set */
        val jsonObject = itineraryListItem.activityList()?.get(0)
        val JSONObj = JSONObject(jsonObject?.toString())
        val it = JSONObj.keys()
        while (it.hasNext()){
            val key = it.next()
            /* Here we add all available keys in to this list(sample key: 2018-12-10).
             * This key is used to load the Horizontal calendar in ItineraryFragment  */
            keyMap.add(key)
        }

        ivBackBtn.setOnClickListener(this)
        tvMap.setOnClickListener(this)
        ivReminderBtn.setOnClickListener(this)
        tvTripTitle.setOnClickListener(this)

        /* check if download trip info button is enabled or not */
        sthDownload.isChecked = mPresenter.checkIsItineraryIsCached(itineraryListItem.itineraryId())
        sthDownload.setOnCheckedChangeListener(this)

        /* View pager set up for Itinerary and GuideBook pages */
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
        setUpTabLayout()
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val itineraryFragment = ItineraryFragment()
        val guideBookFragment = GuideBookFragment()
        val bundle = Bundle()
        bundle.putString(AppConstants.BundleExtras.KEY_ITINERARY, str)
        val  guideBundle = Bundle()
        guideBundle.putString(AppConstants.BundleExtras.KEY_ITINERARY_ID, itineraryListItem.itineraryId())
        itineraryFragment.arguments = bundle
        guideBookFragment.arguments = guideBundle
        guideBookFragment.setPresenter(mPresenter)
        adapter.addFrag(itineraryFragment, getString(R.string.itinerary))
        // adapter.addFrag(AccommodationFragment(), getString(R.string.accommodation))
        adapter.addFrag(guideBookFragment, getString(R.string.guide_book))
        viewPager.adapter = adapter
        adapter.notifyChangeInPosition(1)
        adapter.notifyDataSetChanged()
    }

    override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
        if(isChecked){
            // Download offline switch is on: Ask user for permission to download
            // reminder and activity details response
            showCustomDialog(this,"Download Itinerary Details"
            ,"Are you sure want to download itinerary details ? These details will be available offline."
                    ,"ok","cancel",AppConstants.DialogTags.KEY_OFFLINE_ON)
        } else {
            // Download offline switch is turned off now: Ask user for permission
            // to erase data from offline
            showCustomDialog(this,"Erase Itinerary Details","Are you sure want to erase saved details of this itinerary?"
                    ,"ok","cancel",AppConstants.DialogTags.KEY_OFFLINE_OFF)
        }
    }

    override fun refreshItinerary(response: ItineraryResponse?) {
        str = ""
        str = response?.itineraryList()!![pos].toString()
        tabLayout.removeAllTabs()
        tabLayout.removeOnTabSelectedListener(this)
        viewPager.removeAllViews()
        adapter.removeFrag()
        keyMap.clear()
        init()
        // viewPager.currentItem = 1
    }

    override fun showMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showProgress(visibility: Int) {
        progressBar.visibility = visibility
    }

    override fun onResume() {
        showProgress(View.GONE)
        super.onResume()
    }

    private fun setUpTabLayout(){
        var view = LayoutInflater.from(this).inflate(R.layout.item_tab, null, false)
        val tabOne = view.tab
        tabOne.text = getString(R.string.itinerary)
        view = LayoutInflater.from(this).inflate(R.layout.item_tab, null, false)
        val tabTwo = view.tab
        tabTwo.text = getString(R.string.accommodation)
        view = LayoutInflater.from(this).inflate(R.layout.item_tab, null, false)
        val tabThree = view.tab
        tabThree.text = getString(R.string.guide_book)
        tabLayout.getTabAt(0)?.customView = tabOne
        tabLayout.getTabAt(1)?.customView = tabThree
        tabLayout.addOnTabSelectedListener(this)
    }

    private fun setTranslucentStatusBar(window: Window?) {
        if (window == null) return
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(window)
        } else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setTranslucentStatusBarLollipop(window: Window?) {
        window?.statusBarColor = window?.context?.resources?.getColor(android.R.color.transparent)!!
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
         /* checks whether activity is added successfully or not */
        if(resultCode == Activity.RESULT_OK){
            // API Call to fetch updated itineraryList.
            mPresenter.fetchItineraryList()
        } else {
            // Do nothing as activity is not added successfully
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun setTranslucentStatusBarKiKat(window: Window?) {
        window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }

    override fun onClick(p0: View?) {
        when(p0?.id){
            ivBackBtn.id -> finish()

            tvMap.id -> navigateToMapPage()

            ivReminderBtn.id -> navigateToReminderPage()

            tvTripTitle.id -> navigateToReviewPage()
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        val view = tab?.customView
        val textView = view?.findViewById<TextView>(R.id.tab)
        textView?.setTextColor(Color.parseColor("#666666"))
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        val view = tab?.customView
        val textView = view?.findViewById<TextView>(R.id.tab)
        textView?.setTextColor(Color.parseColor("#db1843"))
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onDestroy() {
        mPresenter.detachView()
        super.onDestroy()
    }

    private fun navigateToMapPage(){
        if(!itineraryListItem.markersList()?.isEmpty()!!){
            val intent = Intent(this, MapActivity::class.java)
            // here str is the itinerary item string
            intent.putExtra(AppConstants.BundleExtras.KEY_ITINERARY, str)
            startActivity(intent)
        } else {
            // this condition might get modified in future user location
            // is taken
            showMessage("You don't have any activities")
        }

    }

    private fun navigateToReminderPage(){
        val intent = Intent(this, ReminderActivity::class.java)
        intent.putExtra(AppConstants.BundleExtras.KEY_ITINERARY_ID, itineraryListItem.itineraryId())
        startActivity(intent)
    }

    private fun navigateToReviewPage() {
        /*
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("pos", tvTripTitle.text)
            startActivity(intent)
        */
    }


    override fun onNegativeButton(tag: String?) {
        if(tag.equals(AppConstants.DialogTags.KEY_OFFLINE_ON)){
            sthDownload.setOnCheckedChangeListener(null)
            sthDownload.isChecked = false
            switchHandler.postDelayed({sthDownload.setOnCheckedChangeListener(this)}, 500)

        } else if(tag.equals(AppConstants.DialogTags.KEY_OFFLINE_OFF)){
            sthDownload.setOnCheckedChangeListener(null)
            sthDownload.isChecked = true
            switchHandler.postDelayed({sthDownload.setOnCheckedChangeListener(this)}, 500)
        }
    }

    override fun onPositiveButton(tag: String?) {
        /* User want to keep the itinerary data for offline mode*/
        if(tag.equals(AppConstants.DialogTags.KEY_OFFLINE_ON)){
            if(ConnectivityChecker.isInternetOn(this)){
                mPresenter.saveItineraryOfflineId(itineraryListItem.itineraryId())
                mPresenter.getOfflineReminders(itineraryListItem.itineraryId())
            } else {
                // Notify the user that he/she is not connected to the internet
                showDialog(this,null, "Please make sure you are connected to the internet before download", "OK","")

                // No connectivity, data cant be fetched. reset switch button state to default
                sthDownload.setOnCheckedChangeListener(null)
                sthDownload.isChecked = false
                // add check listener to the switch once it got reset
                switchHandler.postDelayed({sthDownload.setOnCheckedChangeListener(this)}, 500)
            }
        }

        else if(tag.equals(AppConstants.DialogTags.KEY_OFFLINE_OFF)){
            // remove itinerary id from saved itinerary list
            mPresenter.saveItineraryOfflineId(itineraryListItem.itineraryId())
        }
    }
}
