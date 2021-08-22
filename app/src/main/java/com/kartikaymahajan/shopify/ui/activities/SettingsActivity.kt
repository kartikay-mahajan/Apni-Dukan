package com.kartikaymahajan.shopify.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivitySettingsBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.User
import com.kartikaymahajan.shopify.utils.Constants
import com.kartikaymahajan.shopify.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : BaseActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivitySettingsBinding

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()

        mBinding.tvEdit.setOnClickListener(this)
        mBinding.btnLogout.setOnClickListener(this)
        mBinding.llAddress.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        if (v != null) {
            when (v) {
                mBinding.tvEdit -> {
                    val intent = Intent(this@SettingsActivity, UserProfileActivity::class.java)
                    intent.putExtra(Constants.EXTRA_USER_DETAILS, mUserDetails)
                    startActivity(intent)
                }
                mBinding.btnLogout -> {

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                mBinding.llAddress -> {
                    val intent = Intent(this@SettingsActivity, AddressListActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarSettingsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }
        mBinding.toolbarSettingsActivity.setNavigationOnClickListener{ onBackPressed() }

    }

    override fun onResume() {
        super.onResume()

        getUserDetails()
    }

    /**
     * A function to get the user details from firestore.
     */
    private fun getUserDetails() {

        // Show the progress dialog
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class to get the user details from firestore which is already created.
        FirestoreClass().getUserDetails(this@SettingsActivity)
    }

    /**
     * A function to receive the user details and populate it in the UI.
     */
    fun userDetailsSuccess(user: User) {

        mUserDetails = user
        // Hide the progress dialog
        hideProgressDialog()

        // Load the image using the Glide Loader class.
        GlideLoader(this@SettingsActivity)
            .loadUserPicture(user.image, mBinding.ivUserPhoto)

        mBinding.tvName.text = "${user.firstName} ${user.lastName}"
        mBinding.tvGender.text = user.gender
        mBinding.tvEmail.text = user.email
        mBinding.tvMobileNumber.text = "${user.mobile}"

    }
}