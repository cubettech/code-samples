package com.android.pause.ui.login

import com.android.pause.data.DataManager
import com.android.pause.data.model.UserResponse
import com.android.pause.ui.base.BasePresenter
import com.android.pause.util.RxUtil

import org.json.JSONException
import org.json.JSONObject

import java.io.IOException
import java.net.SocketTimeoutException

import javax.inject.Inject

import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.ResponseBody
import retrofit2.HttpException
import timber.log.Timber

class LoginPresenter @Inject
constructor(private val mDataManager: DataManager) : BasePresenter<LoginMvpView>() {
    private var mDisposable: Disposable? = null

    override fun detachView() {
        super.detachView()
        if (mDisposable != null) mDisposable!!.dispose()
    }

    /* API response handling is removed for demonstration purpose */
    internal fun login(email: String, password: String, fcmToken: String) {
        checkViewAttached()
        RxUtil.dispose(mDisposable)
        mDataManager.login(email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : Observer<UserResponse> {
                    override fun onSubscribe(@NonNull d: Disposable) {
                        mDisposable = d
                    }

                    override fun onNext(@NonNull response: UserResponse) {
                        mvpView?.navigateToHome()
                    }

                    override fun onError(@NonNull e: Throwable) {
                        mvpView?.navigateToHome()
                    }

                    override fun onComplete() {

                    }
                })
    }

    private fun handleError(e: Throwable) {
        if (e is HttpException) {
            //ResponseBody responseBody = ((HttpException)e).response().errorBody();
            //getMvpView().onUnknownError(getErrorMessage(responseBody));
        } else if (e is SocketTimeoutException) {
            mvpView?.onTimeout()
        } else if (e is IOException) {
            mvpView?.onNetworkError()
        } else {
            mvpView?.onError(e.message.toString())
        }
    }
}
