package com.hrmfitclub.android.view.signup

import android.Manifest
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import com.hrmfitclub.android.utils.Validators
import com.example.easywaylocation.EasyWayLocation
import com.example.easywaylocation.EasyWayLocation.LOCATION_SETTING_REQUEST_CODE
import com.example.easywaylocation.Listener
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
import com.hrmfitclub.android.view.login.SignInActivity
import com.hrmfitclub.android.view.BaseActivity
import com.hrmfitclub.android.view.ViewConstants
import com.hrmfitclub.android.view.configuration.Configuration
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.CompositePermissionListener
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.activity_sign_up.*
import kotlinx.android.synthetic.main.activity_sign_up.btnFb
import org.koin.android.viewmodel.ext.android.viewModel
import timber.log.Timber


class SignUpActivity : BaseActivity(), Listener {

    private val viewModel: SignupViewModel by viewModel()
    var mLocation: Location? = null
    private lateinit var easyWayLocation: EasyWayLocation
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mCallbackManager: CallbackManager
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        makeStatusBarTransparent(darkStatusBarIcons = false)
        ViewCompat.setOnApplyWindowInsetsListener(root_signUp) { _, insets ->
            val marginTop = insets.systemWindowInsetTop
            root_signUp.updatePadding(top = marginTop)
            insets.consumeSystemWindowInsets()
        }

        val anim = root_signUp.background as AnimationDrawable
        anim.setEnterFadeDuration(6000)
        anim.setExitFadeDuration(5000)
        anim.start()

        setupFacebookLogin()
        setupGoogleSignIn()
        setupViews()
        checkLocationPermission()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LOCATION_SETTING_REQUEST_CODE -> easyWayLocation.onActivityResult(resultCode)
            ViewConstants.RC_GOOGLE_SIGN_IN -> {
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
            }
        }
    }

    private fun setupFacebookLogin() {
        firebaseAuth = FirebaseAuth.getInstance()
        mCallbackManager = CallbackManager.Factory.create()

        btnFb.setPermissions("email", "public_profile")
        btnFb.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                Timber.d("facebook:onSuccess: $result")
                handleFacebookAccessToken(result.accessToken)
            }
            override fun onCancel() {
                Timber.d("onCanceled FB")
            }
            override fun onError(error: FacebookException?) {
                showAlertDialog("Facebook sign up failed", Status.ERROR)
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
        startActivityForResult(signInIntent, ViewConstants.RC_GOOGLE_SIGN_IN)
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
                    callSignUp(user?.displayName!!, "", email, "", "facebook")
                } else {
                    Timber.w(task.exception, "signInWithCredential:failure")
                    handleFirebaseException(
                        this,
                        task
                    )
                }
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
                    callSignUp(user?.displayName!!, "", email, "", "google")
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

    private fun setupViews() {
        ib_password.setOnClickListener {
            if (et_password.transformationMethod is PasswordTransformationMethod) {
                et_password.transformationMethod = SingleLineTransformationMethod()
                ib_password.setImageDrawable(getDrawable(R.drawable.ic_hidden))
            } else {
                et_password.transformationMethod = PasswordTransformationMethod()
                ib_password.setImageDrawable(getDrawable(R.drawable.ic_visibility_button))
            }
            et_password.setSelection(et_password.text.length)
        }
        ib_confirm_password.setOnClickListener {
            if (et_confirm_password.transformationMethod is PasswordTransformationMethod) {
                et_confirm_password.transformationMethod = SingleLineTransformationMethod()
                ib_confirm_password.setImageDrawable(getDrawable(R.drawable.ic_hidden))
            } else {
                et_confirm_password.transformationMethod = PasswordTransformationMethod()
                ib_confirm_password.setImageDrawable(getDrawable(R.drawable.ic_visibility_button))
            }
            et_confirm_password.setSelection(et_confirm_password.text.length)

        }
        btn_sign_up.setOnClickListener {
            if (fieldsValid()) {
                if (mLocation != null) {
                    val firstName = et_firstName.text.trim().toString()
                    val lastName = et_lastName.text.trim().toString()
                    val email = editText.text.trim().toString()
                    val password = et_password.text.trim().toString()
                    callSignUp(firstName, lastName, email, password, "email")
                } else {
                    checkLocationPermission()
                }
            }
        }
        imageButton2.setOnClickListener {
            if (mLocation != null) {
                val account = GoogleSignIn.getLastSignedInAccount(this)
                if (account != null) mGoogleSignInClient.signOut()
                googleSignIn()
            } else {
                checkLocationPermission()
            }
        }
        imageView.setOnClickListener {
            if (mLocation != null) {
                LoginManager.getInstance().logOut()
                btnFb.performClick()
            } else {
                checkLocationPermission()
            }
        }
        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus && Validators.isValidEmail(editText.text.toString())) {
                checkEmailExist(editText.text.toString())
            }
        }
        iv_back.setOnClickListener {
            onBackPressed()
        }
    }

    private fun fieldsValid(): Boolean {
        val firstName = et_firstName.text.trim()
        val email = editText.text.trim()
        val password = et_password.text.trim()
        val confirmPassword = et_confirm_password.text.trim()

        var message = ""
        if(firstName.isEmpty()){
            message = getString(R.string.name_empty_validation)
        } else if (email.isEmpty()) {
            message =  getString(R.string.empty_email_validation)
        } else if(!Validators.isValidEmail(email)) {
            message = getString(R.string.email_format_validation)
        } else if(password.isEmpty()) {
            message = getString(R.string.empty_password_validation)
        } else if(confirmPassword.toString().isEmpty()) {
            message = getString(R.string.empty_confirm_password_validation)
        } else if(confirmPassword.toString() != password.toString()) {
            message = getString(R.string.password_match_validation)
        }
        return if(message.isEmpty()) {
            true
        } else {
            showAlertDialog(message, Status.ERROR)
            false
        }
    }

    private fun callSignUp(firstName: String, lastName: String, email: String, password: String, mode :String){
        showProgressDialog()
        viewModel.signupUser(firstName, lastName, email,password,
            mLocation?.latitude.toString(), mLocation?.longitude.toString(), mode)
            .observe(this, Observer {
                it?.let {response ->
                    when (response.status) {
                        Status.SUCCESS -> {
                            //hide progress loader
                            hideProgressDialog()
                            response.data?.let {
                                val state = response.data.status?: false
                                if (state) {
                                    showVerificationAlert(email, mode)
                                } else {
                                    showAlertDialog(response.data.message?: "API Error", Status.ERROR)
                                }
                            }
                        }
                        Status.ERROR -> {
                            hideProgressDialog()
                            Timber.d("response: ${response.message}")
                            showAlertDialog(response.message?: "Something went wrong", Status.ERROR)
                        }
                        Status.LOADING -> {
                            showProgressDialog()
                        }
                    }
                }
            })
    }

    private fun showVerificationAlert(email: String, mode: String) {
        if (mode == "email") {
            showAlertDialog("We have send you a verification link to your email" +
                    " id $email please click and verify to continue login", Status.SUCCESS, object : CustomAlertView.OnAlertClickListener {
                override fun onClickDone() {
                    openSignInPage()
                }
            })
        } else {
            val intent = Intent(this@SignUpActivity, Configuration::class.java)
            ActivityCompat.startActivity(this@SignUpActivity, intent, null)
            finish()
        }
    }

    private fun checkEmailExist(email: String) {
        viewModel.checkEmail(email)
            .observe(this, Observer {
                it?.let {state ->
                    when (state.status) {
                        Status.SUCCESS -> {
                            state.data?.let {
                                showAlertDialog(getString(R.string.emailIdExist), Status.ERROR)
                                editText.setText("")
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

    private fun openSignInPage() {
        btn_sign_up.startAnimation()
        Handler().postDelayed(Runnable {
            val intent = Intent(this@SignUpActivity, SignInActivity::class.java)
            intent.putExtra(ViewConstants.PARAM_SHOW_VERIFICATION, true)
            ActivityCompat.startActivity(this@SignUpActivity, intent, null)
            btn_sign_up.revertAnimation()
        }, Validators.animationDuration)
    }

    private fun checkLocationPermission() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(CompositePermissionListener(object : PermissionListener {
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
                        .withContext(this@SignUpActivity)
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

    private fun clearAllAnimation() {
        btn_sign_up.dispose()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearAllAnimation()
    }

    override fun locationCancelled() {
        Timber.d("Location CANCELLED")
    }

    override fun locationOn() {
        Timber.d("Location ON")
    }

    override fun currentLocation(location: Location?) {
        mLocation = location
        easyWayLocation.endUpdates()
        Timber.d("Location Received: ${mLocation?.latitude} ${mLocation?.longitude}")
    }

}
