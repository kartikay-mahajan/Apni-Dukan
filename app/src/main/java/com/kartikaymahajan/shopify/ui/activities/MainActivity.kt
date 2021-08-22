package com.kartikaymahajan.shopify.ui.activities

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.kartikaymahajan.shopify.databinding.ActivityMainBinding
import com.kartikaymahajan.shopify.utils.Constants

class MainActivity: BaseActivity() {

    private lateinit var mBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        val sharedPreferences =
            getSharedPreferences(Constants.SHOPIFY_PREFERENCES,Context.MODE_PRIVATE)

        val username = sharedPreferences.getString(Constants.LOGGED_IN_USERNAME,"")!!
        mBinding.tvMain.text = "Hello $username"

    }
}