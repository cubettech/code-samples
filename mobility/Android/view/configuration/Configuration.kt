package com.hrmfitclub.android.view.configuration

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import br.com.simplepass.loadingbutton.customViews.ProgressButton
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import com.app.hrmapp.screens.BluetoothConnectionPage
import com.app.hrmapp.utils.CountryUtil
import com.hrmfitclub.android.misc.customviews.popupView.CustomPopUpView
import com.hrmfitclub.android.misc.customviews.popupView.OnDialogClickListeners
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.Listener
import com.hrmfitclub.android.BuildConfig
import com.hrmfitclub.android.R
import com.hrmfitclub.android.data.remote.dto.LocationsItem
import com.hrmfitclub.android.misc.Status
import com.hrmfitclub.android.misc.customviews.datepicker.DatePickerDialogFragment
import com.hrmfitclub.android.misc.ext.getHeightListInCM
import com.hrmfitclub.android.misc.ext.getHeightListInFeet
import com.hrmfitclub.android.misc.ext.makeStatusBarTransparent
import com.hrmfitclub.android.misc.ext.showAlertDialog
import com.hrmfitclub.android.utils.getAvailableTimeZones
import com.hrmfitclub.android.utils.Validators
import com.hrmfitclub.android.view.BaseActivity
import com.hrmfitclub.android.view.ViewConstants
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_configeration.*
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class Configuration : BaseActivity(), Listener {

    private val viewModel: ConfigurationViewModel by viewModel()
    private var cameraUri: Uri? = null
    private var resultUri: Uri? = null
    private var heightSelectedPosition = -1
    private var weightSelectedPosition = -1
    private var countrySelectedPosition = -1
    private var timeZoneSelectedPosition = -1
    private var locationSelectedPosition = -1

    private var locationList: List<LocationsItem?>? = listOf()
    private lateinit var easyWayLocation: EasyWayLocation
    private var mLocation: Location? = null

    private lateinit  var  anim : AnimationDrawable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configeration)
        makeStatusBarTransparent(darkStatusBarIcons = false)
        ViewCompat.setOnApplyWindowInsetsListener(root_config) { _, insets ->
            val marginTop = insets.systemWindowInsetTop
            root_config.updatePadding(top = marginTop)
            insets.consumeSystemWindowInsets()
        }

        anim = root_configuration.background as AnimationDrawable
        anim.setEnterFadeDuration(6000)
        anim.setExitFadeDuration(5000)
        anim.start()

        txt_country.setOnClickListener {
            showCountryPopUp(CountryUtil.getCountries(getCurrentCountry(false)))
        }
        txt_height.setOnClickListener {
           showHeightPopUp(getHeightList())
        }
        txt_weight.setOnClickListener {
           showWeightPopUp(getWeightList())
        }
        txt_time_zone.text = getCurrentTimeZone()
        txt_time_zone.setOnClickListener {
            showTimeZonePopUp(
                getAvailableTimeZones(
                    getCurrentTimeZone()
                )
            )
        }
        txt_location_look_up.setOnClickListener {
            showLocationPopUp()
        }
        txt_dob.setOnClickListener {
            val datePickerDialogFragment = DatePickerDialogFragment()
            datePickerDialogFragment.setOnDateChooseListener(object : DatePickerDialogFragment.OnDateChooseListener {
                override fun onDateChoose(year: Int, month: Int, day: Int) {
                    val date = "$day/$month/$year"
                    Timber.d("selected date $date")
                    txt_dob.text = date
                }
            })
            datePickerDialogFragment.show(supportFragmentManager, "DatePickerDialogFragment")
        }
        imageView2.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setMessage("Pick your avatar image for HRM Fit Club")
                .setPositiveButton("GALLERY") { dialog, which -> onGalleryClick() }
                .setNegativeButton("CAMERA") { dialog, which -> onCameraClick() }
                .setNeutralButton("CANCEL", null)
                .show()
        }

        btn_save_continue.setOnClickListener {
            if (cb_agreeTerms.isChecked) {
                if (checkValidation()) {
                    uploadImageIfNeeded()
                }
            } else {
                showAlertDialog("Please accept the Terms & Policy before continuing", Status.ERROR)
            }
        }
        txt_user_name.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                checkUserNameExist(txt_user_name.text.toString())
            }
        }

        locationList = viewModel.getLocationsList()

        checkLocationPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            EasyWayLocation.LOCATION_SETTING_REQUEST_CODE -> easyWayLocation.onActivityResult(resultCode)
            ViewConstants.RC_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    callCropActivity(data, true)
                }
            }
            ViewConstants.RC_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    callCropActivity(data, false)
                }
            }
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    resultUri = result.uri
                    Timber.d("croped uri: ${resultUri?.path}")
                    showProfilePic()
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    val error = result.error
                    error.printStackTrace()
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        val location = txt_location_look_up.text.trim()
        val userName = txt_user_name.text.trim()
        val dob = txt_dob.text.trim()
        val height = txt_height.text.trim()
        val weight = txt_weight.text.trim()
        val timeZone = txt_time_zone.text.trim()
        var valid = true
        if (location.toString() == getString(R.string.location_look_up)) {
            valid = false
            showAlertDialog("Please select a location", Status.ERROR)
        } else if (userName.isEmpty()) {
            valid = false
            showAlertDialog("User name required", Status.ERROR)
        } else if (dob.toString() == getString(R.string.dob)) {
            valid = false
            showAlertDialog("Date of birth required", Status.ERROR)
        } else if (height.toString() == getString(R.string.height)) {
            valid = false
            showAlertDialog("Height required", Status.ERROR)
        } else if (weight.toString() == getString(R.string.weight)) {
            valid = false
            showAlertDialog("Weight required", Status.ERROR)
        } else if (timeZone.isEmpty()) {
            valid = false
            showAlertDialog("Time Zone required", Status.ERROR)
        }
        return valid
    }

    private fun uploadImageIfNeeded() {
        if (resultUri != null) {
            showProgressDialog()
            val credentials = BasicAWSCredentials(ViewConstants.AWS_KEY, ViewConstants.AWS_SECRET)
            val s3Client = AmazonS3Client(credentials)
            s3Client.setRegion(Region.getRegion(Regions.EU_WEST_1))

            var file: File? = null
            resultUri?.let { file = File(resultUri?.path!!) }
            Timber.d(file?.path)
            Timber.d(file?.name)

            val transferUtility: TransferUtility = TransferUtility.builder()
                .context(applicationContext)
                .s3Client(s3Client)
                .build()
            val uploadObserver: TransferObserver = transferUtility.upload(ViewConstants.AWS_BUCKET_NAME, file?.name, file)
            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState) {
                    if (TransferState.COMPLETED === state) {
                        Timber.d("Upload Completed!")
                        callConfigAPI(file?.name)
                        file!!.delete()
                    } else if (TransferState.FAILED === state) {
                        Timber.d("Upload Failed!")
                        hideProgressDialog()
                        file!!.delete()
                    }
                }

                override fun onProgressChanged(
                    id: Int,
                    bytesCurrent: Long,
                    bytesTotal: Long
                ) {
                    val percentDonef = bytesCurrent.toFloat() / bytesTotal.toFloat() * 100
                    val percentDone = percentDonef.toInt()
                    Timber.d("ID:$id | bytesCurrent: $bytesCurrent|bytesTotal: $bytesTotal|$percentDone%")
                }

                override fun onError(id: Int, ex: Exception) {
                    Timber.e(ex)
                    hideProgressDialog()
                }
            })
        } else {
            callConfigAPI("")
        }
    }

    private fun callConfigAPI(imageFileName: String?){
        val userName = txt_user_name.text.trim().toString()
        val dob = txt_dob.text.trim().toString()
        val height = getHeightListInCM()[heightSelectedPosition].split(" ")[0] //txt_height.text.trim().toString()
        var weightInKg = txt_weight.text.trim().split(" ")[0]
        if (txt_country.text.toString().trim() == "United States") {
            val inPound = weightInKg.toFloat()
            weightInKg = String.format("%.0f", inPound / 2.20462262)
        }
        val weight = weightInKg
        val timeZone = txt_time_zone.text.trim().toString()
        val country = txt_country.text.trim().toString()
        var gender = 0
        if (radioMale.isChecked) gender = 0
        if (radioFemale.isChecked) gender = 1
        if (radioOther.isChecked) gender = 2
        val userId = viewModel.getUserId()
        val locationId = locationList?.get(locationSelectedPosition)?.id
        viewModel.configUser(userId!!,
            height,
            weight,
            userName,
            locationId!!,
            country,
            dob,
            gender.toString(),
            timeZone,
            imageFileName)
            .observe(this, Observer {
                it?.let {response ->
                    when (response.status) {
                        Status.SUCCESS -> {
                            hideProgressDialog()
                            response.data?.let {
                                val state = response.data.status?: false
                                if (state) {
                                    openBLEConnectionPage()
                                } else {
                                    showAlertDialog(response.data.message?: "API Error", Status.ERROR)
                                }
                            }
                        }
                        Status.ERROR -> {
                            hideProgressDialog()
                            Timber.d("response: ${response.message}")
                            showAlertDialog(response.data?.message?: "Something went wrong", Status.ERROR)
                        }
                        Status.LOADING -> {
                            showProgressDialog()
                        }
                    }
                }
            })
    }

    private fun openBLEConnectionPage() {
        btn_save_continue.startAnimation()
        Handler().postDelayed(Runnable {
            val intent = Intent(this@Configuration, BluetoothConnectionPage::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ActivityCompat.startActivity(this@Configuration, intent, null)
            finish()
            btn_save_continue.revertAnimation()

        }, Validators.animationDuration)
    }

    private fun callCropActivity(data: Intent?, isGallery: Boolean) {
        val delay = if (Build.MANUFACTURER == "samsung") {
            500L
        } else {
            0
        }
        Handler().postDelayed({
            if (!isGallery) {
                cameraUri?.let {
                    CropImage.activity(cameraUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .setAspectRatio(4, 4)
                        .setRequestedSize(600, 600)
                        .setAllowFlipping(false)
                        .setAllowRotation(false)
                        .start(this)
                }
            } else if (data != null && data.data != null) {
                CropImage.activity(data.data)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setFixAspectRatio(true)
                    .setAspectRatio(4, 4) // 3:4 600x800
                    .setRequestedSize(600, 600)
                    .setAllowFlipping(false)
                    .setAllowRotation(false)
                    .start(this)
            }
        }, delay)
    }

    private fun onGalleryClick() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, ViewConstants.RC_GALLERY)
    }

    private fun onCameraClick() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!dir!!.exists()) {
            dir.mkdirs()
        }
        val fullFile = File(dir.path + File.separator + "IMG_"+ timeStamp + ".jpg")
        cameraUri = FileProvider.getUriForFile(this,
            BuildConfig.APPLICATION_ID + ".provider", fullFile)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.addFlags(FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        startActivityForResult(intent, ViewConstants.RC_CAMERA)
    }

    private fun showProfilePic() {
        Glide.with(this).load(resultUri).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                imageView2.setPadding(0,0,0,0)
                return false
            }
        }).into(imageView2)
    }

    private fun showLocationPopUp() {
        val locatioNames = mutableListOf<String>()
        for (item in locationList!!) {
            item?.locationName?.let { locatioNames.add(it) }
        }
        val popUp = CustomPopUpView(
            title = "Locations",
            context = this@Configuration,
            itemLayoutManager = LinearLayoutManager(this@Configuration),
            dataItems = locatioNames,
            selectedPosition = locationSelectedPosition
        )
        popUp.setDialogButtonListners(object : OnDialogClickListeners {
            override fun onClickCancel() {
                popUp.dismiss()
            }
            override fun onClickDone() {
                locationSelectedPosition = popUp.getSelectedItemPosition()
                Timber.d("current selected = %s", locationSelectedPosition)
                if(locationSelectedPosition != -1) {
                    txt_location_look_up.text = locatioNames[locationSelectedPosition]
                }
                popUp.dismiss()
            }
        })
        popUp.show()
    }

    private fun showCountryPopUp(countryDummy: MutableList<String>) {
        val popUp = CustomPopUpView(
            title = "Countries",
            context = this@Configuration,
            itemLayoutManager = LinearLayoutManager(this@Configuration),
            dataItems = countryDummy,
            selectedPosition = countrySelectedPosition
        )

        popUp.setDialogButtonListners(object : OnDialogClickListeners {
            override fun onClickCancel() {
                popUp.dismiss()
            }

            override fun onClickDone() {
                countrySelectedPosition = popUp.getSelectedItemPosition()
                Timber.d("current selected === $countrySelectedPosition")
                if(countrySelectedPosition != -1){
                    txt_country.text = countryDummy[countrySelectedPosition]
                    updateWeightAndHeight()
                }
                popUp.dismiss()
            }
        })
        popUp.show()
    }

    private fun showTimeZonePopUp(list: MutableList<String>) {
        val popUp = CustomPopUpView(
            title = "Time Zone",
            context = this@Configuration,
            itemLayoutManager = LinearLayoutManager(this@Configuration),
            dataItems = list,
            selectedPosition = timeZoneSelectedPosition
        )
        popUp.setDialogButtonListners(object : OnDialogClickListeners {
            override fun onClickCancel() {
                popUp.dismiss()
            }
            override fun onClickDone() {
                timeZoneSelectedPosition = popUp.getSelectedItemPosition()
                Timber.d("current selected = %s", timeZoneSelectedPosition)
                if(timeZoneSelectedPosition != -1) {
                    txt_time_zone.text = list[timeZoneSelectedPosition]
                }
                popUp.dismiss()
            }
        })
        popUp.show()
    }


    private fun showHeightPopUp(countryDummy: MutableList<String>) {
        val popUp = CustomPopUpView(
            title = "Your Height",
            context = this@Configuration,
            itemLayoutManager = LinearLayoutManager(this@Configuration),
            dataItems = countryDummy,
            selectedPosition = heightSelectedPosition
        )

        popUp.setDialogButtonListners(object : OnDialogClickListeners {
            override fun onClickCancel() {
                popUp.dismiss()
            }

            override fun onClickDone() {
                heightSelectedPosition = popUp.getSelectedItemPosition()
                if (heightSelectedPosition != -1) {
                    txt_height.text = countryDummy[heightSelectedPosition]
                }
                popUp.dismiss()
            }

        })
        popUp.show()
    }


  private fun showWeightPopUp(countryDummy: MutableList<String>) {

      var popUp = CustomPopUpView(
          title = "Choose Weight",
          context = this@Configuration,
          itemLayoutManager = LinearLayoutManager(this@Configuration),
          dataItems = countryDummy,
          selectedPosition = weightSelectedPosition
      )

      popUp.setDialogButtonListners(object : OnDialogClickListeners {
          override fun onClickCancel() {
              popUp!!.dismiss()
          }

          override fun onClickDone() {
              weightSelectedPosition = popUp!!.getSelectedItemPosition()
              if(weightSelectedPosition != -1){
                  txt_weight.text = countryDummy[weightSelectedPosition]
              }
              popUp!!.dismiss()
          }

      }
      )

      popUp.show()
  }


    private fun ProgressButton.morphAndRevert(revertTime: Long = 5000, startAnimationCallback: () -> Unit = {}) {
        startAnimation(startAnimationCallback)
        Handler().postDelayed(::revertAnimation, revertTime)
    }

    private fun clearAllAnimation() {

        btn_save_continue.dispose()
        anim.stop()
    }

    private fun updateWeightAndHeight() {
        if (weightSelectedPosition > -1) {
            txt_weight.text = getWeightList().get(weightSelectedPosition)
        }
    }

    private fun getHeightList(): MutableList<String> {
        return getHeightListInFeet()
    }

    private fun getWeightList(): MutableList<String> {
        val list = mutableListOf<String>()
        (30..300).map {
            if (txt_country.text.toString().trim() == "United States") {
                list.add(String.format("%.2f pounds", it * 2.20462262))
            } else {
                list.add("$it kg")
            }
        }
        return list
    }

    private fun checkUserNameExist(name: String) {
        viewModel.checkUserName(name)
            .observe(this, Observer {
                it?.let {state ->
                    when (state.status) {
                        Status.SUCCESS -> {
                            state.data?.let { response ->
                                if (!response.available!!) {
                                    showAlertDialog(getString(R.string.emailIdExist), Status.ERROR)
                                    txt_user_name.setText("")
                                }
                            }
                        }
                        Status.ERROR -> {
                            Timber.d("email check error response: ${state.message}")
                        }
                        Status.LOADING -> {  }
                    }
                }
            })
    }

    private fun getCurrentCountry(updateView: Boolean): String {
        var countryName = "United States"
        mLocation?.let {
            var addresses: List<Address>? = null
            val geocoder = Geocoder(this, Locale.getDefault())
            try {
                addresses = geocoder.getFromLocation(mLocation!!.latitude, mLocation!!.longitude, 1)
                countryName = addresses[0].countryName
            } catch (e: IOException) {
                e.printStackTrace()
            }
            Timber.d("Country : $countryName")
            if (updateView) {
                txt_country.text = countryName
            }
        }
        return countryName
    }

    private fun checkLocationPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(
                CompositePermissionListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    getLocation()
                }
                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token?.continuePermissionRequest()
                }
                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {  }
            }, DialogOnDeniedPermissionListener.Builder
                .withContext(this@Configuration)
                .withTitle("Location permission")
                .withMessage("Location permission is needed to find the Gyms near you")
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build())
            ).check()
    }

    private fun getLocation() {
        easyWayLocation = EasyWayLocation(this, false, this)
        easyWayLocation.startLocation()
    }

    private fun getCurrentTimeZone(): String {
        val date: DateFormat = SimpleDateFormat("z", Locale.getDefault())
        val localTime: String = date.format(Date())
        return "($localTime) ${date.timeZone.id}"
    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllAnimation()
    }

    override fun locationCancelled() {  }

    override fun locationOn() {  }

    override fun currentLocation(location: Location?) {
        mLocation = location
        location?.let { getCurrentCountry(true) }
        easyWayLocation.endUpdates()
        Timber.d("Location Received: ${location?.latitude} ${location?.longitude}")
    }
}
