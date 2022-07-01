package com.hrmfitclub.android.view

import CustomAlertView
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.hrmfitclub.android.R
import com.hrmfitclub.android.misc.Status

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {

    private  var progressDialog : Dialog? = null

    fun BaseActivity.showProgressDialog() {
        if(progressDialog != null){
            if(!progressDialog!!.isShowing){
                progressDialog!!.show()
            }
        } else {
            progressDialog = Dialog(this)
            progressDialog?.setContentView(R.layout.dialog_progress)
            progressDialog?.setCancelable(false)

            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(this.window?.attributes)
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            progressDialog?.window?.attributes = layoutParams
            progressDialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            val progressImage : LottieAnimationView = progressDialog!!.findViewById(R.id.progress)
            progressImage.playAnimation()

            progressDialog!!.show()
        }
    }


    fun Activity.hideProgressDialog() {
        if(progressDialog != null){
            progressDialog!!.dismiss()
        }
    }


}