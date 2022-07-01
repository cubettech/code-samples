package com.hrmfitclub.android.view.login

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.hrmfitclub.android.R
import com.hrmfitclub.android.misc.Status
import com.hrmfitclub.android.misc.ext.makeStatusBarTransparent
import com.hrmfitclub.android.misc.ext.showAlertDialog
import com.hrmfitclub.android.view.BaseActivity
import com.hrmfitclub.android.view.ViewConstants
import kotlinx.android.synthetic.main.activity_reset_password.*
import kotlinx.android.synthetic.main.activity_reset_password.iv_back
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber

class ResetPassword : BaseActivity(), CustomAlertView.OnAlertClickListener{

    private val viewModel: LoginViewModel by viewModel()
    private var anim: AnimationDrawable? = null
    private var email: String = ""

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        makeStatusBarTransparent(darkStatusBarIcons = false)
        ViewCompat.setOnApplyWindowInsetsListener(root_reset_password) { _, insets ->
            val marginTop = insets.systemWindowInsetTop
            root_reset_password.updatePadding(top = marginTop)
            insets.consumeSystemWindowInsets()
        }

        anim = root_reset_password.background as AnimationDrawable
        anim!!.setEnterFadeDuration(6000)
        anim!!.setExitFadeDuration(5000)
        anim!!.start()

        email = intent.getStringExtra(ViewConstants.PARAM_EMAIL)!!
        var message = ""
        if(email != null){
            message = "Please enter the OTP you received on $email and then please enter your new " +
                    "password and then confirm it. Remember your password should match each other."
        } else {
           message = "Please enter the OTP you received and then please enter your new password and" +
                   " then confirm it. Remember your password should match each other."
        }
        txt_description_reset_password.text = message

        setUpViews()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setUpViews() {
        btn_reset_password.setOnClickListener {
            if(isAllFieldsValid()){
//                showProgressDialog()
//                Handler().postDelayed(Runnable {
//                    hideProgressDialog()
//                    showAlertDialog(description = getString(R.string.password_reset_successful), theme = Status.SUCCESS)
//                }, Validators.animationDuration)
                callResetPassword()
            }
        }

        btn_resend_otp.setOnClickListener {
            callResendOtp()
        }

        ib__reset_password.setOnClickListener {
            if (et_reset_password.transformationMethod is PasswordTransformationMethod) {
                et_reset_password.transformationMethod = SingleLineTransformationMethod()
                ib__reset_password.setImageDrawable(getDrawable(R.drawable.ic_hidden))
            } else {
                et_reset_password.transformationMethod = PasswordTransformationMethod()
                ib__reset_password.setImageDrawable(getDrawable(R.drawable.ic_visibility_button))
            }
            et_reset_password.setSelection(et_reset_password.text.length)
        }
        ib_confirm_reset_password.setOnClickListener {
            if (et_confirm_reset_password.transformationMethod is PasswordTransformationMethod) {
                et_confirm_reset_password.transformationMethod = SingleLineTransformationMethod()
                ib_confirm_reset_password.setImageDrawable(getDrawable(R.drawable.ic_hidden))
            } else {
                et_confirm_reset_password.transformationMethod = PasswordTransformationMethod()
                ib_confirm_reset_password.setImageDrawable(getDrawable(R.drawable.ic_visibility_button))
            }
            et_confirm_reset_password.setSelection(et_confirm_reset_password.text.length)

        }
        iv_back.setOnClickListener { onBackPressed() }
    }

    private fun isAllFieldsValid(): Boolean {
        var message = "";
        if(et_otp.text.isEmpty()){
            message = getString(R.string.empty_otp_validation)
        }else if(et_reset_password.text.isEmpty()){
            message = getString(R.string.empty_password_validation)
        }else if(et_confirm_reset_password.text.isEmpty()){
            message = getString(R.string.empty_confirm_password_validation)
        }else if(et_reset_password.text.toString() != et_confirm_reset_password.text.toString()) {
            message = getString(R.string.password_match_validation)
        }

        return if(message.isEmpty()){
            true
        }else{
            this.showAlertDialog(message, Status.ERROR )
            false
        }
    }

    private fun callResetPassword() {
        viewModel.resetPassword(email, et_otp.text.toString(), et_reset_password.text.toString())
            .observe(this, Observer {
                it?.let {state ->
                    when (state.status) {
                        Status.SUCCESS -> {
                            hideProgressDialog()
                            state.data?.let {dto ->
                                if (dto.status!!) {
                                    showAlertDialog(description = dto.message?:
                                    getString(R.string.password_reset_successful), theme = Status.SUCCESS, onClickListener = this)
                                } else {
                                    showAlertDialog(dto.message!!, Status.ERROR)
                                }
                            }
                        }
                        Status.ERROR -> {
                            Timber.d("error response: ${state.message}")
                            hideProgressDialog()
                            showAlertDialog(state.message?:
                            getString(R.string.password_reset_unsuccessful), Status.ERROR)
                        }
                        Status.LOADING -> {
                            showProgressDialog()
                        }
                    }
                }
            })
    }

    private fun callResendOtp() {
        viewModel.sendOtp(email)
            .observe(this, Observer {
                it?.let {state ->
                    when (state.status) {
                        Status.SUCCESS -> {
                            hideProgressDialog()
                            state.data?.let {dto ->
                                if (dto.status!!) {
                                    showAlertDialog(dto.message!!, Status.SUCCESS)
                                } else {
                                    showAlertDialog(dto.message!!, Status.ERROR)
                                }
                            }
                        }
                        Status.ERROR -> {
                            Timber.d("error response: ${state.message}")
                            hideProgressDialog()
                            showAlertDialog(state.message?: "API Error", Status.ERROR)
                        }
                        Status.LOADING -> {
                            showProgressDialog()
                        }
                    }
                }
            })
    }

    private fun clearAllAnimation() {
        //  btn_join_now.stopAnimation()
        anim!!.stop()
        btn_reset_password.dispose()

    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllAnimation()
    }

    override fun onClickDone() {
        navigateToSignIn()
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        ActivityCompat.startActivity(this, intent, null)
        finish()
    }
}