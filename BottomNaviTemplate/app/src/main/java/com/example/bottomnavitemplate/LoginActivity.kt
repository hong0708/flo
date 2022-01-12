package com.example.bottomnavitemplate

import android.content.Intent
import android.os.Bundle
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bottomnavitemplate.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity(), LoginView {
    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginSignupTv.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        binding.loginSigninTv.setOnClickListener {
            login()
        }
    }

    /*private fun login() {
        if (binding.loginIdEt.text.toString()
                .isEmpty() || binding.loginEmailEt.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.loginPasswordEt.text.toString().isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val email: String =
            binding.loginIdEt.text.toString() + "@" + binding.loginEmailEt.text.toString()
        val pwd: String = binding.loginPasswordEt.text.toString()

        val songDB = SongDatabase.getInstance(this)!!

        val user = songDB.userDao().getUser(email, pwd)

        user?.let {
            Log.d("GETUSER", "userId: ${user.id},$user")
            saveJWT(user.id)
        }
        *//*Toast.makeText(this, "회원정보가 존재하지 않습니다", Toast.LENGTH_SHORT).show()*//*
    }*/

    /*private fun saveJWT(jwt: Int) {
        val spf = getSharedPreferences("auth", MODE_PRIVATE)
        val editor = spf.edit()

        editor.putInt("jwt", jwt)
        editor.apply()
    }*/

    private fun login() {
        if (binding.loginIdEt.text.toString()
                .isEmpty() || binding.loginEmailEt.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (binding.loginPasswordEt.text.toString().isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            return
        }

        val email: String =
            binding.loginIdEt.text.toString() + "@" + binding.loginEmailEt.text.toString()
        val pwd: String = binding.loginPasswordEt.text.toString()
        val user = User(email, pwd, "")

        val authError = AuthService()
        authError.setLoginView(this)

        authError.login(user)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onLoginLoading() {
        binding.loginLoadingPb.visibility = View.VISIBLE
    }

    override fun onLoginSuccess(auth: Auth) {
        binding.loginLoadingPb.visibility = View.GONE

        saveJwt(this, auth.jwt)
        saveUserIdx(this, auth.userIdx)

        startMainActivity()
    }

    override fun onLoginFailure(code: Int, message: String) {
        binding.loginLoadingPb.visibility = View.GONE

        when(code){
            2015, 2019, 3014 -> {
                binding.loginErrorTv.visibility = View.VISIBLE
                binding.loginErrorTv.text = message
            }
        }
    }
}