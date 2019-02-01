package com.android.pause.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Toast
import com.android.pause.R
import com.android.pause.ui.base.BaseActivity
import com.android.pause.ui.home.MainActivity
import com.android.pause.ui.signup.SignUpActivity
import com.android.pause.util.CommonUtil
import com.android.pause.util.SnackBarFactory
import kotlinx.android.synthetic.main.activity_login.*
import javax.inject.Inject

class LoginActivity : BaseActivity(), LoginMvpView, View.OnClickListener {

    @Inject
    lateinit var mPresenter:LoginPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        activityComponent()?.inject(this)
        mPresenter.attachView(this)
        init()
    }

    private fun init(){
        btnLogin.setOnClickListener(this)
        btnForgotPassword.setOnClickListener(this)
        btnSignUp.setOnClickListener(this)
        imageView2.setOnClickListener(this)
        textView4.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            btnLogin.id -> onLogin()

            btnSignUp.id -> onSignUp()

            textView4.id -> openWeb("www.google.com")

            imageView2.id -> CommonUtil.hideKeyboard(this)
        }

    }

    private fun onLogin(){
        val emailAddress = etEmail.text
        val password = etPassword.text
        if (isCredentialsValid(emailAddress.toString(), password.toString())) {
            progressBar.visibility = View.VISIBLE
            mPresenter.login(emailAddress.toString(), password.toString(), "")
        }
        //navigateToHome()
    }

    private fun onSignUp(){
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }


    override fun onPositiveButton(tag: String?) { }

    override fun onNegativeButton(tag: String?) { }

    override fun navigateToHome() {
        progressBar.visibility = View.GONE
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onTimeout() {
        progressBar.visibility = View.GONE
        SnackBarFactory.showSnackBar(login_container, R.string.timeout_error)
    }

    override fun onNetworkError() {
        progressBar.visibility = View.GONE
        SnackBarFactory.showSnackBar(login_container, R.string.no_internet_error)
    }

    override fun onError(message: String) {
        progressBar.visibility = View.GONE
        SnackBarFactory.showSnackBar(login_container, message)
        etEmail.setText("")
        etPassword.setText("")    
    }


    override fun onDestroy() {
        mPresenter.detachView()
        super.onDestroy()
    }

    private fun isCredentialsValid(email: String, password: String): Boolean {
        if (email.trim().isEmpty()) {
            SnackBarFactory.showSnackBar(login_container, R.string.email_invalid)
            return false
        }
        if (password.trim().isEmpty()) {
            SnackBarFactory.showSnackBar(login_container, R.string.password_invalid)
            return false
        }
        return true
    }

    private fun openWeb(website: String) {
        val intent = Intent(Intent.ACTION_VIEW)
                .setData(Uri.parse(website))
        startActivity(intent)
    }
}
