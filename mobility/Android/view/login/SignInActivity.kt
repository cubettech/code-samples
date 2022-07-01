package com.hrmfitclub.android.view.login

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.app.hrmapp.screens.BluetoothConnectionPage
import com.hrmfitclub.android.view.home.HomeActivity
import com.app.hrmapp.utils.MyBounceInterpolator
import com.hrmfitclub.android.utils.Validators
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.hrmfitclub.android.R
import com.hrmfitclub.android.misc.Status
import com.hrmfitclub.android.misc.ext.makeStatusBarTransparent
import com.hrmfitclub.android.misc.ext.showAlertDialog
import com.hrmfitclub.android.utils.handleFirebaseException
import com.hrmfitclub.android.view.BaseActivity
import com.hrmfitclub.android.view.ViewConstants
import com.hrmfitclub.android.view.ViewConstants.Companion.RC_GOOGLE_SIGN_IN
import com.hrmfitclub.android.view.configuration.Configuration
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.iv_back
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class SignInActivity : BaseActivity() {

    private val viewModel: LoginViewModel by viewModel()
    private var anim: AnimationDrawable? = null

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mCallbackManager: CallbackManager
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        makeStatusBarTransparent(darkStatusBarIcons = false)
        ViewCompat.setOnApplyWindowInsetsListener(root_signIn) { _, insets ->
            val marginTop = insets.systemWindowInsetTop
            root_signIn.updatePadding(top = marginTop)
            insets.consumeSystemWindowInsets()
        }

        anim = root_signIn.background as AnimationDrawable
        anim!!.setEnterFadeDuration(6000)
        anim!!.setExitFadeDuration(5000)
        anim!!.start()

        setupFacebookLogin()
        setupGoogleSignIn()
        setupViews()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    Timber.d("firebaseAuthWithGoogle: ${account.id}")
                    Timber.d("firebaseAuthWithGoogle: ${account.displayName}")
                    Timber.d("firebaseAuthWithGoogle: ${account.email}")
                    firebaseAuthWithGoogle(account.idToken)
                } catch (e: ApiException) {
                    // Google Sign In failed, update UI appropriately
                    Timber.w(e, "Google sign in failed")
                }
            } else {
                Timber.d("Google sign in canceled")
            }
        }
    }

    private fun setupViews() {
        viewModel.getRegisteredEmail()?.let {
            et_email_sign_in.setText(it)
            if (intent.getBooleanExtra(ViewConstants.PARAM_SHOW_VERIFICATION, false)) {
                showVerificationAlert(it)
            }
        }

        btnFb.setLoginText("")
        ibtFbCustom.setOnClickListener {
            LoginManager.getInstance().logOut()
            //fbButton.loginBehavior = LoginBehavior.WEB_VIEW_ONLY
            btnFb.performClick()
        }
        btnGoogleSignIn.setOnClickListener {
            val account = GoogleSignIn.getLastSignedInAccount(this)
            if (account != null) mGoogleSignInClient.signOut()
            googleSignIn()
        }

        btn_forgot_password.setOnClickListener {
            showDialog()
        }

        btn_sign_in.setOnClickListener {
            if(isAllFieldsValid()){
              //  btn_sign_in.startAnimation()
                callSignInApi(et_email_sign_in.text.toString(),et_password_sign_in.text.toString(), "email")
//                Handler().postDelayed(Runnable {
//                    //navigateToHomeScreen()
//                 //   btn_sign_in.revertAnimation()
//                }, Validators.animationDuration)
            }
        }
        iv_back.setOnClickListener { onBackPressed() }
    }

    private fun isAllFieldsValid(): Boolean {
        var message = "";
        if(et_email_sign_in.text.isEmpty()){
            message = getString(R.string.empty_email_validation)
        } else if(!Validators.isValidEmail(et_email_sign_in.text)){
            message = getString(R.string.email_format_validation)
        } else if(et_password_sign_in.text.isEmpty()){
            message = getString(R.string.empty_password_validation)
        }
        return if(message.isEmpty()){
            true
        } else {
            showAlertDialog(message, Status.ERROR)
            false
        }
    }

    private fun setupFacebookLogin() {
        firebaseAuth = FirebaseAuth.getInstance()
        mCallbackManager = CallbackManager.Factory.create()

        btnFb.setPermissions("email", "public_profile");
        btnFb.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Timber.d("facebook:onSuccess: $result")
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                Timber.d("onCanceled FB")
            }

            override fun onError(error: FacebookException?) {
                showAlertDialog("Facebook sign in failed", Status.ERROR)
                Timber.e(error)
            }
        })
    }

    private fun setupGoogleSignIn() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Timber.d("handleFacebookAccessToken:onSuccess: $token")
        //showProgressBar()
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Timber.d("signInWithCredential:success")
                    val user = firebaseAuth.currentUser
                    var email = ""
                    if (user?.email == null) {
                        user?.providerData?.map { provider ->
                            provider.email?.let { email = it }
                        }
                    } else {
                        email = user.email!!
                    }
                    Timber.d(" : ${user?.displayName}")
                    Timber.d(" : ${user?.email}")
                    Timber.d(" : $email")
                    callSignInApi(email, "", "facebook")
                } else {
                    Timber.w(task.exception, "signInWithCredential:failure")
                    handleFirebaseException(
                        this,
                        task
                    )
                }
                //hideProgressBar()
            }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Timber.d("signInWithCredential: success")
                    val user = firebaseAuth.currentUser
                    var email = ""
                    if (user?.email == null) {
                        user?.providerData?.map { provider ->
                            provider.email?.let { email = it }
                        }
                    } else {
                        email = user.email!!
                    }
                    Timber.d(" : ${user?.displayName}")
                    Timber.d(" : ${user?.email}")
                    Timber.d(" : $email")
                    callSignInApi(email, "", "google")
                } else {
                    // If sign in fails, display a message to the user.
                    Timber.w(task.exception,"signInWithCredential: failure")
                    handleFirebaseException(
                        this,
                        task
                    )
                }
            }
    }

    private fun callSignInApi(email: String, password: String, mode: String) {
        if (mode == "email") {
            viewModel.signIn(email, password)
                .observe(this, Observer {
                    it?.let {response ->
                        when (response.status) {
                            Status.SUCCESS -> {
                                hideProgressDialog()
                                response.data?.let {
                                    if (response.data.user?.userName == null || response.data.user.userName.isEmpty()) {
                                        navigateToConfigPage()
                                    } else {
                                        navigateToBlueToothConnectionPage()
                                    }
                                }
                            }
                            Status.ERROR -> {
                                Timber.d("reponse: ${response.message}")
                                hideProgressDialog()
                                showAlertDialog(response.message?: "API Error", Status.ERROR)
                            }
                            Status.LOADING -> {
                                showProgressDialog()
                            }
                        }
                    }
                })
        } else {
            callSocialLogin(email)
        }
    }

    private fun callSocialLogin(email: String) {
        viewModel.checkEmail(email)
            .observe(this, Observer {
                it?.let {response ->
                    when (response.status) {
                        Status.SUCCESS -> {
                            hideProgressDialog()
                            response.data?.let {
                                if (response.data.user?.userName == null || response.data.user.userName.isEmpty()) {
                                    navigateToConfigPage()
                                } else {
                                    navigateToBlueToothConnectionPage()
                                }
                            }
                        }
                        Status.ERROR -> {
                            Timber.d("reponse: ${response.message}")
                            hideProgressDialog()
                            showAlertDialog("User not found. Please try signing up for an account", Status.ERROR)
                        }
                        Status.LOADING -> {
                            showProgressDialog()
                        }
                    }
                }
            })
    }

    private fun navigateToBlueToothConnectionPage() {
        val intent = Intent(this@SignInActivity, BluetoothConnectionPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityCompat.startActivity(this@SignInActivity, intent, null)
        finish()
    }

    private fun navigateToConfigPage() {
       val intent = Intent(this@SignInActivity, Configuration::class.java)
        ActivityCompat.startActivity(this@SignInActivity, intent, null)
        finish()
    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_alert_popup)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val sendOtp : Button = dialog.findViewById(R.id.btn_send_otp)
        val cancel : Button = dialog.findViewById(R.id.btn_cancel)
        val mainLayer : FrameLayout = dialog.findViewById(R.id.fl_main)
        val ib_clear : ImageView = dialog.findViewById(R.id.ib_clear)
        val et_email : EditText = dialog.findViewById(R.id.et_email)

        ib_clear.setOnClickListener {
            et_email.setText("")
        }
        sendOtp.setOnClickListener {
            var message = "";
            if(et_email.text.isEmpty()){
                message = getString(R.string.empty_email_validation)
            }else if(!Validators.isValidEmail(et_email.text)){
                message = getString(R.string.email_format_validation)
            }

            if(message.isEmpty()){
                sendOtpApi(dialog, et_email.text.toString())
            } else {
                showAlertDialog(message, Status.ERROR)
            }
        }

       val  myAnim1 = AnimationUtils.loadAnimation(this, R.anim.strip_anim)

        val interpolator = MyBounceInterpolator(0.2, 20.0)
        myAnim1!!.interpolator = interpolator
        mainLayer.visibility = View.VISIBLE
        mainLayer.startAnimation(myAnim1)

        cancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun sendOtpApi(dialog: Dialog, email: String) {
        viewModel.sendOtp(email)
            .observe(this, Observer {
                it?.let {state ->
                    when (state.status) {
                        Status.SUCCESS -> {
                            hideProgressDialog()
                            state.data?.let {dto ->
                                if (dto.status!!) {
                                    navigateToResetPassword(email)
                                    dialog.dismiss()
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

    private fun navigateToHomeScreen() {
        val intent = Intent(this, HomeActivity::class.java)
        ActivityCompat.startActivity(this, intent, null)
        finish()
    }

    private fun navigateToResetPassword(email: String) {
        val intent = Intent(this, ResetPassword::class.java)
        intent.putExtra(ViewConstants.PARAM_EMAIL, email)
        ActivityCompat.startActivity(this, intent, null)
    }

    private fun showVerificationAlert(email: String) {
        showAlertDialog("Please click the verify link sent to your mail id $email to continue login", Status.SUCCESS)
    }

    private fun clearAllAnimation() {
        //  btn_join_now.stopAnimation()
        anim!!.stop()
        btn_sign_in.dispose()

    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllAnimation()
    }
}