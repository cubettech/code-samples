package com.hrmfitclub.android.view.home

import android.Manifest
import android.app.Activity
import android.content.*
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.hrmfitclub.android.R
import com.hrmfitclub.android.ble.services.BluetoothLeWorkoutService
import com.hrmfitclub.android.ble.services.LocalWorkoutBinder
import com.hrmfitclub.android.ble.services.State
import com.hrmfitclub.android.ble.util.BleDeviceDto
import com.hrmfitclub.android.ble.util.BluetoothAdapterWrapper
import com.hrmfitclub.android.data.remote.dto.ActivitiesItem
import com.hrmfitclub.android.misc.Status
import com.hrmfitclub.android.misc.ext.get
import com.hrmfitclub.android.misc.ext.showAlertDialog
import com.hrmfitclub.android.view.ScanningDevices
import com.hrmfitclub.android.view.BaseActivity
import com.hrmfitclub.android.view.ViewConstants
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_COMMUNITY_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_LEADER_BOARD_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_LEADER_BOARD_INNER_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_LEADER_BOARD_VIEW_ALL_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_PICK_ACTIVITY_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_TEST_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_WORKOUT_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_WORKOUT_HISTORY_FRAGMENT
import com.hrmfitclub.android.view.ViewConstants.Companion.TAG_WORKOUT_POST_FRAGMENT
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.app_bar.*
import timber.log.Timber
import uk.co.alt236.bluetoothlelib.device.BluetoothLeDevice


class HomeActivity : BaseActivity() {

    private lateinit var workoutHistoryFragment: WorkoutHistoryFragment
    private lateinit var leaderBoardFragment: LeaderBoardFragment
    private lateinit var communityFragment: CommunityFragment
    private lateinit var workOutFragment: WorkoutFragment
    private lateinit var testFragment: TestFragment
    private lateinit var active: Fragment

    private var leaderBoardViewAllFragment: LeaderBoardViewAllFragment? = null
    private var leaderBoardInnerFragment: LeaderBoardInnerFragment? = null
    private var pickActivityFragment: PickActivityFragment? = null
    private var workoutPostFragment: WorkoutPostFragment? = null

    private var mBluetoothLeService: BluetoothLeWorkoutService? = null
    private val fm: FragmentManager = supportFragmentManager
    private var connectionState: State = State.DISCONNECTED
    private var mDevice: BluetoothLeDevice? = null
    private var deviceAddress: String? = null
    private var deviceName: String? = null

    private lateinit var selectedActivity: ActivitiesItem
    private lateinit var anim: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        anim = rootContainer.background as AnimationDrawable
        anim.setEnterFadeDuration(5000)
        anim.setExitFadeDuration(5000)

        val navigation = findViewById<View>(R.id.nav_view) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        navigation.selectedItemId = R.id.navigation_menu_3

        ibDeviceState.setOnClickListener {
            showDeviceConnectPage()
        }

        val localStoredDevice = getSavedDevice()
        if (localStoredDevice != null) {
            starBlePrepare(localStoredDevice)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter())
        anim.start()
    }

    override fun onPause() {
        unregisterReceiver(mGattUpdateReceiver)
        anim.stop()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mBluetoothLeService != null) {
            unbindService(mServiceConnection)
            mBluetoothLeService = null
        }
    }

    override fun onBackPressed() {
        val fragmentWorkout = supportFragmentManager.findFragmentByTag(TAG_WORKOUT_FRAGMENT)
        val fragmentPostWorkout = supportFragmentManager.findFragmentByTag(TAG_WORKOUT_POST_FRAGMENT)
        val fragmentPickActivity = supportFragmentManager.findFragmentByTag(TAG_PICK_ACTIVITY_FRAGMENT)
        val fragmentLeaderBoardInner = supportFragmentManager.findFragmentByTag(TAG_LEADER_BOARD_INNER_FRAGMENT)
        val fragmentLeaderBoardViewAll = supportFragmentManager.findFragmentByTag(TAG_LEADER_BOARD_VIEW_ALL_FRAGMENT)

        if (fragmentPickActivity != null && fragmentPickActivity.isVisible) {
            removeFragmentFromStack(fragmentPickActivity)
            pickActivityFragment = null
            showThirdTab()
        } else if (isWorkOutStarted() or isWorkOutPaused()) {
            showAlertDialog("Workout in progress. Please finish your workout to exit the app", Status.ERROR)
        } else if (fragmentWorkout != null && workOutFragment.isVisible && workOutFragment.needsNavigateBack()) {
            workOutFragment.showPreviousSection()
        } else if (fragmentPostWorkout != null && fragmentPostWorkout.isVisible) {
            (fragmentPostWorkout as WorkoutPostFragment).handleDiscard()
        } else if (fragmentLeaderBoardInner != null && fragmentLeaderBoardInner.isVisible) {
            removeFragmentFromStack(fragmentLeaderBoardInner)
            leaderBoardInnerFragment = null
            showSecondTab()
        } else if (fragmentLeaderBoardViewAll != null && fragmentLeaderBoardViewAll.isVisible) {
            removeFragmentFromStack(fragmentLeaderBoardViewAll)
            leaderBoardViewAllFragment = null
            openLeaderBoardInnerPage(null)
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ViewConstants.RC_SELECT_HRM_DEVICE -> {
                if (resultCode == Activity.RESULT_OK) {
                    mDevice = data?.getParcelableExtra(ViewConstants.PARAM_HRM_DEVICE)
                    mDevice?.let { connectHRMDevice(mDevice?.address) }
                }
            }
        }
    }

    private val mOnNavigationItemSelectedListener: BottomNavigationView.OnNavigationItemSelectedListener =
        object : BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.navigation_menu_1 -> {
                        return showFirstTab()
                    }
                    R.id.navigation_menu_2 -> {
                        return showSecondTab()
                    }
                    R.id.navigation_menu_3 -> {
                        return showThirdTab()
                    }
                    R.id.navigation_menu_4 -> {
                        return showFourthTab()
                    }
                    R.id.navigation_menu_5 -> {
                        return showFifthTab()
                    }
                }
                return false
            }
        }

    private fun showFirstTab(): Boolean {
        return if (::communityFragment.isInitialized) {
            showFragment(communityFragment, getString(R.string.community))
        } else {
            communityFragment = CommunityFragment()
            addFragment(communityFragment, TAG_COMMUNITY_FRAGMENT, getString(R.string.community))
        }
    }

    private fun showSecondTab(): Boolean {
        return if (::leaderBoardFragment.isInitialized) {
            clearLeaderboardInnerFragments()
            showFragment(leaderBoardFragment, getString(R.string.leader_board))
        } else {
            clearLeaderboardInnerFragments()
            leaderBoardFragment = LeaderBoardFragment()
            addFragment(leaderBoardFragment, TAG_LEADER_BOARD_FRAGMENT, getString(R.string.leader_board))
        }
    }

    fun showThirdTab(): Boolean {
        if (::workOutFragment.isInitialized) {
            fm.beginTransaction().hide(active).show(workOutFragment).commit()
        } else {
            workOutFragment = WorkoutFragment()
            fm.beginTransaction().add(R.id.container, workOutFragment, TAG_WORKOUT_FRAGMENT).commit()
        }
        active = workOutFragment
        tvTilte.text = getString(R.string.Workout)
        return true
    }

    private fun showFourthTab(): Boolean {
        return if (::workoutHistoryFragment.isInitialized) {
            showFragment(workoutHistoryFragment, getString(R.string.history))
        } else {
            workoutHistoryFragment = WorkoutHistoryFragment()
            addFragment(workoutHistoryFragment, TAG_WORKOUT_HISTORY_FRAGMENT, getString(R.string.history))
        }
    }

    private fun showFifthTab(): Boolean {
        return if (::testFragment.isInitialized) {
            showFragment(testFragment, getString(R.string.Workout))
        } else {
            testFragment = TestFragment()
            addFragment(testFragment, TAG_TEST_FRAGMENT, getString(R.string.Workout))
        }
    }

    private fun showFragment(fragment: Fragment, title: String): Boolean {
        fm.beginTransaction().hide(active).show(fragment).commit()
        active = fragment
        tvTilte.text = title
        return true
    }

    private fun addFragment(fragment: Fragment, tag: String, title: String): Boolean {
        fm.beginTransaction().hide(active).add(R.id.container, fragment, tag).commit()
        active = fragment
        tvTilte.text = title
        return true
    }

    private fun removeFragmentFromStack(fragment: Fragment) {
        fm.beginTransaction().remove(fragment).commit()
    }

    private fun clearLeaderboardInnerFragments() {
        val fragmentLeaderBoardInner = supportFragmentManager.findFragmentByTag(TAG_LEADER_BOARD_INNER_FRAGMENT)
        val fragmentLeaderBoardViewAll = supportFragmentManager.findFragmentByTag(TAG_LEADER_BOARD_VIEW_ALL_FRAGMENT)
        if (fragmentLeaderBoardInner != null) {
            removeFragmentFromStack(fragmentLeaderBoardInner)
            leaderBoardInnerFragment = null
        }
        if (fragmentLeaderBoardViewAll != null) {
            removeFragmentFromStack(fragmentLeaderBoardViewAll)
            leaderBoardViewAllFragment = null
        }
    }

    private fun connectHRMDevice(address: String?) {
        Timber.d("connectHRMDevice")
        if (address != null && address.isNotEmpty()) {
            if (mBluetoothLeService == null) {
                val gattServiceIntent = Intent(this, BluetoothLeWorkoutService::class.java)
                bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
            } else {
                val result = mBluetoothLeService?.connect(address)
                Timber.d("Connect request result= $result")
            }
        } else {
            showAlertDialog("Unable to connect to device. Please try again...", Status.ERROR)
        }
    }

    // Code to manage Service lifecycle.
    private val mServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected")
            mBluetoothLeService = (service as LocalWorkoutBinder).service
            if (!mBluetoothLeService?.initialize()!!) {
                Timber.e("Unable to initialize Bluetooth")
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (mDevice == null) {
                Timber.e("Device address local : $deviceAddress")
                mBluetoothLeService?.connect(deviceAddress)
            } else {
                Timber.e("Device address : " + mDevice?.address)
                mBluetoothLeService?.connect(mDevice?.address)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mBluetoothLeService = null
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_GATT_CONNECTING)
        intentFilter.addAction(BluetoothLeWorkoutService.ACTION_WORKOUT_RESULT)
        return intentFilter
    }

    private val mGattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothLeWorkoutService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(State.CONNECTED)
            } else if (BluetoothLeWorkoutService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(State.DISCONNECTED)
            } else if (BluetoothLeWorkoutService.ACTION_GATT_CONNECTING.equals(action)) {
                updateConnectionState(State.CONNECTING)
            } else if(BluetoothLeWorkoutService.ACTION_WORKOUT_RESULT.equals(action)) {
                updateUI(intent)
            }
        }
    }

    private fun updateConnectionState(state: State) {
        connectionState = state
        runOnUiThread {
            when (state) {
                State.CONNECTED -> {
                    ibDeviceState.setImageResource(R.drawable.ic_app_icon3)
                }
                State.DISCONNECTED -> {
                    ibDeviceState.setImageResource(R.drawable.ic_device_disconnect)
                    if (mDevice == null) {
                        showAlertDialog("Couldn't connect to $deviceName device. Please switch on your HRM device to start workout", Status.ERROR)
                    } else {
                        showAlertDialog("${mDevice?.name} device disconnected", Status.ERROR)
                    }
                    if (::workOutFragment.isInitialized && workOutFragment.isAdded && workOutFragment.isWorkOutStarted()) {
                        workOutFragment.handlePauseWorkOut()
                    }
                }
                State.CONNECTING -> {
                    ibDeviceState.setImageResource(R.drawable.ic_device_disconnect)
                }
            }
        }
    }

    private fun updateUI(intent: Intent) {
        if (::workOutFragment.isInitialized && workOutFragment.isAdded) {
            workOutFragment.updateUI(intent)
        }
    }

    private fun isWorkOutPaused(): Boolean {
        if (::workOutFragment.isInitialized && workOutFragment.isAdded) {
            return workOutFragment.isWorkOutPaused()
        }
        return false
    }

    private fun getSavedDevice(): BleDeviceDto? {
        val pref = getSharedPreferences(ViewConstants.APP_PREF, Context.MODE_PRIVATE)
        val dataString = pref.get(ViewConstants.PREF_DEVICE_LOCAL, "")
        if (dataString!!.isNotEmpty()) {
            return Gson().fromJson(dataString, BleDeviceDto::class.java)
        }
        return null
    }

    private fun starBlePrepare(localStoredDevice: BleDeviceDto) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permission: String
            val message: String
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permission = Manifest.permission.ACCESS_FINE_LOCATION
                message = "permission_not_granted_fine_location"
            } else {
                permission = Manifest.permission.ACCESS_COARSE_LOCATION
                message = "permission_not_granted_coarse_location"
            }
            Dexter.withContext(this)
                .withPermission(permission)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        connectToLocalSavedDevice(localStoredDevice)
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        //todo show rationale dialog
                        token?.continuePermissionRequest()
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                }).check()
        } else {
            connectToLocalSavedDevice(localStoredDevice)
        }
    }

    private val mBluetoothAdapterWrapper: BluetoothAdapterWrapper by lazy {
        BluetoothAdapterWrapper(this.applicationContext)
    }
    private fun connectToLocalSavedDevice(localStoredDevice: BleDeviceDto) {
        Timber.d( "startScan")
        val isBluetoothOn = mBluetoothAdapterWrapper.isBluetoothOn
        val isBluetoothLePresent = mBluetoothAdapterWrapper.isBluetoothLeSupported
        if (!isBluetoothLePresent) {
            Toast.makeText(
                this,
                "This device does not support BTLE. Cannot scan...",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        mBluetoothAdapterWrapper.askUserToEnableBluetoothIfNeeded(this)
        if (isBluetoothOn) {
            if (mBluetoothLeService == null) {
                deviceAddress = localStoredDevice.address
                deviceName = localStoredDevice.name
                connectHRMDevice(localStoredDevice.address)
            }
        }
    }

    fun isHRMDeviceAvailable(): Boolean {
        return connectionState == State.CONNECTED
        //return mDevice != null
    }

    fun showDeviceConnectPage() {
        if (::workOutFragment.isInitialized) {
            if (workOutFragment.isWorkOutStarted() || workOutFragment.isWorkOutPaused()) {
                showAlertDialog(
                    "You cannot change the HRM device while you are working out. Try after finishing the session",
                    Status.ERROR
                )
                return
            }
        }
        if ((connectionState == State.CONNECTED) or (connectionState == State.CONNECTING)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("You are already connected to a device. Do you want to disconnect?")
                .setPositiveButton("Yes") { dialog, which -> disconnectDevice() }
                .setNegativeButton("Cancel") { dialog, which ->  }
                .show()
        } else {
            val intent = Intent(this, ScanningDevices::class.java)
            intent.putExtra(ViewConstants.PARAM_FROM_WORKOUT, true)
            ActivityCompat.startActivityForResult(this, intent, ViewConstants.RC_SELECT_HRM_DEVICE, null)
        }
    }

    private fun disconnectDevice() {
        mBluetoothLeService?.disconnectAndStopNotification()
        //mBluetoothLeService?.close(false)
    }

    fun openPickActivityPage() {
        pickActivityFragment = PickActivityFragment()
        fm.beginTransaction().hide(active)
            .add(R.id.container, pickActivityFragment!!, TAG_PICK_ACTIVITY_FRAGMENT).commit()
        active = pickActivityFragment!!
    }

    fun openWorkoutPostPage(bundle: Bundle) {
        workoutPostFragment = WorkoutPostFragment()
        workoutPostFragment?.arguments = bundle
        fm.beginTransaction().hide(active)
            .add(R.id.container, workoutPostFragment!!, TAG_WORKOUT_POST_FRAGMENT).commit()
        active = workoutPostFragment!!
    }

    fun openLeaderBoardInnerPage(bundle: Bundle?) {
        if (leaderBoardInnerFragment == null) {
            leaderBoardInnerFragment = LeaderBoardInnerFragment()
            leaderBoardInnerFragment?.arguments = bundle
            fm.beginTransaction().hide(active)
                .add(R.id.container, leaderBoardInnerFragment!!, TAG_LEADER_BOARD_INNER_FRAGMENT).commit()
        } else {
            fm.beginTransaction().hide(active).show(leaderBoardInnerFragment!!).commit()
        }
        active = leaderBoardInnerFragment!!
    }

    fun openLeaderBoardViewAllPage(bundle: Bundle?) {
        leaderBoardViewAllFragment = LeaderBoardViewAllFragment()
        leaderBoardViewAllFragment?.arguments = bundle
        fm.beginTransaction().hide(active)
            .add(R.id.container, leaderBoardViewAllFragment!!, TAG_LEADER_BOARD_VIEW_ALL_FRAGMENT).commit()
        active = leaderBoardViewAllFragment!!
    }

    fun startWorkOut(): Boolean {
        return if (connectionState == State.CONNECTED) {
            mBluetoothLeService?.workOutStarted = true
            true
        } else {
            showAlertDialog("Device not yet connected", Status.ERROR)
            false
        }
    }

    fun pauseWorkOut() {
        mBluetoothLeService?.workOutStarted = false
    }

    fun isWorkOutStarted(): Boolean {
        return mBluetoothLeService?.workOutStarted ?: false
    }

    fun setSelectedActivity(activity: ActivitiesItem) {
        this.selectedActivity = activity
        if (::workOutFragment.isInitialized && workOutFragment.isAdded) {
            workOutFragment.setActivity(activity)
        }
    }

    fun getSelectedActivity(): ActivitiesItem? {
        if (::selectedActivity.isInitialized) {
            return selectedActivity
        }
        return null
    }

    fun resetWorkOutData() {
        mBluetoothLeService?.resetWorkOutValues()
    }

    fun clearWorkoutStats() {
        removeFragmentFromStack(workoutPostFragment!!)
        workoutPostFragment = null
        showThirdTab()
        if (::workOutFragment.isInitialized && workOutFragment.isAdded) {
            workOutFragment.showPreviousSection()
        }
    }

    fun showLottieProgressDialog() {
        showProgressDialog()
    }

    fun hideLottieProgressDialog() {
        hideProgressDialog()
    }

}


