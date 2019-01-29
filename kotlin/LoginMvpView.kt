package com.android.pause.ui.login

import com.android.pause.ui.base.MvpView

interface LoginMvpView : MvpView {

    fun navigateToHome()
    fun onTimeout()
    fun onNetworkError()
    fun onError(message: String)
}
