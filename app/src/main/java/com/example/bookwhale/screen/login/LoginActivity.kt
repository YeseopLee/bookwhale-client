package com.example.bookwhale.screen.login

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.example.bookwhale.databinding.ActivityLoginBinding
import com.example.bookwhale.screen.base.BaseActivity
import org.koin.android.viewmodel.ext.android.viewModel
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.bookwhale.R
import com.example.bookwhale.screen.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLogin.mOAuthLoginHandler
import com.nhn.android.naverlogin.OAuthLoginHandler
import com.nhn.android.naverlogin.data.OAuthLoginState
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {
    override val viewModel by viewModel<LoginViewModel>()

    override fun getViewBinding(): ActivityLoginBinding = ActivityLoginBinding.inflate(layoutInflater)

    private lateinit var mOAuthLoginModule : OAuthLogin

    override fun initViews(): Unit = with(binding) {

        setSignInNaver()
        setSignInKaKao()
    }

    private fun setSignInKaKao() = with(binding) {

        kakaoLoginButton.setOnClickListener {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity)) {
                UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity, callback = callback)
            } else {
                UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity, callback = callback)
            }
        }

    }

    private val callback : (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e("로그인 실패-","$error")
        } else if (token != null) {
            UserApiClient.instance.me { _, _ ->
                viewModel.kakaoLogin(token.accessToken)
                //viewModel?.addKakaoUser(token.accessToken, kakaoId)
            }
            Log.d("로그인성공 - 토큰", "${token.accessToken}")
        }
    }

    private fun setSignInNaver() = with(binding) {

        mOAuthLoginModule = OAuthLogin.getInstance()
        mOAuthLoginModule.init(
            this@LoginActivity
            ,getString(R.string.naver_client_id)
            ,getString(R.string.naver_client_secret)
            ,getString(R.string.naver_client_name)
        )

        naverLoginButton.setOnClickListener {

            @SuppressLint("HandlerLeak")
            val mOAuthLoginHandler: OAuthLoginHandler = object : OAuthLoginHandler() {
                override fun run(success: Boolean) {
                    if (success) {
                        val accessToken: String = mOAuthLoginModule.getAccessToken(baseContext)

                        lifecycleScope.launch {
                            viewModel.naverLogin(accessToken).join()
                        }

                    }
                    else {
                        val errorCode: String = mOAuthLoginModule.getLastErrorCode(this@LoginActivity).code
                        val errorDesc = mOAuthLoginModule.getLastErrorDesc(this@LoginActivity)
                    }
                }

            }
            mOAuthLoginModule.startOauthLoginActivity(this@LoginActivity, mOAuthLoginHandler)
        }

        naverLogout.setOnClickListener {
            mOAuthLoginModule.logout(this@LoginActivity)
        }
    }

    override fun observeData() = with(binding) {
        viewModel.loginStateLiveData.observe(this@LoginActivity) {
            when (it) {
                is LoginState.Loading -> handleLoading()
                is LoginState.Success -> handleSuccess(it)
                is LoginState.Error -> handleError()
                else -> Unit
            }
        }
    }

    private fun handleLoading() {}
    private fun handleSuccess(state: LoginState.Success) {
        startActivity(MainActivity.newIntent(this))
    }
    private fun handleError() {}
}