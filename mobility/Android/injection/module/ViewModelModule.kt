package com.hrmfitclub.android.injection.module

import com.hrmfitclub.android.view.comments.CommentsViewModel
import com.hrmfitclub.android.view.configuration.ConfigurationViewModel
import com.hrmfitclub.android.view.home.HomeViewModel
import com.hrmfitclub.android.view.login.LoginViewModel
import com.hrmfitclub.android.view.signup.SignupViewModel
import com.hrmfitclub.android.view.splash.SplashViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { LoginViewModel(get(), get(), get()) }
    viewModel { SignupViewModel(get(), get(), get()) }
    viewModel { ConfigurationViewModel(get(), get(), get()) }
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SplashViewModel(get(), get(), get()) }
    viewModel { CommentsViewModel(get(), get(), get()) }
}