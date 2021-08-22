package com.kartikaymahajan.shopify.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivityUserProfileBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.User
import com.kartikaymahajan.shopify.utils.Constants
import com.kartikaymahajan.shopify.utils.GlideLoader
import kotlinx.android.synthetic.main.activity_user_profile.*
import java.io.IOException

class UserProfileActivity : BaseActivity(), View.OnClickListener{

    private lateinit var mBinding: ActivityUserProfileBinding

    private var mSelectedImageFileUri: Uri? = null

    private var mUserProfileImageURL: String = ""

    private lateinit var mUserDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if(intent.hasExtra(Constants.EXTRA_USER_DETAILS)){
            // Get the user details from the intent as ParcelableExtra
            mUserDetails = intent.getParcelableExtra(Constants.EXTRA_USER_DETAILS)!!
        }

        if(mUserDetails.profileCompleted == 0){
            mBinding.tvTitle.text = resources.getString(R.string.title_complete_profile)

            mBinding.etFirstName.isEnabled = false   // so that user cant change values now
            mBinding.etFirstName.setText(mUserDetails.firstName)   //put same value as user had put while logging in

            mBinding.etLastName.isEnabled = false   // so that user cant change values now
            mBinding.etLastName.setText(mUserDetails.lastName)   //put same value as user had put while logging in

            mBinding.etEmail.isEnabled = false   // so that user cant change values now
            mBinding.etEmail.setText(mUserDetails.email)   //put same value as user had put while logging in

        }else{
            setupActionBar()

            mBinding.tvTitle.text = resources.getString(R.string.title_edit_profile)
            GlideLoader(this@UserProfileActivity)
                .loadUserPicture(mUserDetails.image, mBinding.ivUserPhoto)

            mBinding.etFirstName.setText(mUserDetails.firstName)   //put same value as user had put while logging in
            mBinding.etLastName.setText(mUserDetails.lastName)   //put same value as user had put while logging in

            mBinding.etEmail.isEnabled = false   // so that user cant change values now
            mBinding.etEmail.setText(mUserDetails.email)   //put same value as user had put while logging in

            if (mUserDetails.mobile != 0L) {
                mBinding.etMobileNumber.setText(mUserDetails.mobile.toString())
            }
            if (mUserDetails.gender == Constants.MALE) {
                mBinding.rbMale.isChecked = true
            } else {
                mBinding.rbFemale.isChecked = true
            }
        }

        mBinding.ivUserPhoto.setOnClickListener(this@UserProfileActivity)
        mBinding.btnSubmit.setOnClickListener(this@UserProfileActivity)
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(mBinding.toolbarUserProfileActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        mBinding.toolbarUserProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onClick(v: View?) {
        if(v != null){
            when(v){
                mBinding.ivUserPhoto -> {

                    // Here we will check if the permission is already allowed or we need to request for it.
                    // First of all we will check the READ_EXTERNAL_STORAGE permission and if it is not allowed we will request for the same.
                    if (ContextCompat.checkSelfPermission(
                            this, Manifest.permission.READ_EXTERNAL_STORAGE
                        ) == PackageManager.PERMISSION_GRANTED) {

                        Constants.showImageChooser(this@UserProfileActivity)
                    //showErrorSnackBar("You already have the storage permission.", false)
                    } else {

                        /*Requests permissions to be granted to this application. These permissions
                         must be requested in your manifest, they should not be granted to your app,
                         and they should have protection level*/

                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            Constants.READ_STORAGE_PERMISSION_CODE
                        )
                    }
                }
                mBinding.btnSubmit -> {
                    if(validateUserProfileDetails()){
                        showProgressDialog(resources.getString(R.string.please_wait))

                        if(mSelectedImageFileUri != null) {
                            FirestoreClass().uploadImageToCloudStorage(
                                this, mSelectedImageFileUri,
                                    Constants.USER_PROFILE_IMAGE)
                        }else{
                            updateUserProfileDetails()
                        }
                    }
                }
            }
        }
    }

    /**
     * A function to notify the success result and proceed further accordingly after updating the user details.
     */
    fun userProfileUpdateSuccess(){
        hideProgressDialog()
        Toast.makeText(this@UserProfileActivity,resources.getString(R.string.msg_profile_update_success),Toast.LENGTH_SHORT).show()

        startActivity(Intent(this@UserProfileActivity, DashboardActivity::class.java))
        finish()
    }

    /**
     * A function to validate the input entries for profile details.
     */
    private fun validateUserProfileDetails(): Boolean{
        return when{
            TextUtils.isEmpty(mBinding.etMobileNumber.text.toString().trim() { it <= ' '}) ->{
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_mobile_number),true)
                false
            }
            else ->{
                true
            }
        }
    }

    /**
     * A function to notify the success result of image upload to the Cloud Storage.
     *
     * @param imageURL After successful upload the Firebase Cloud returns the URL.
     */
    fun imageUploadSuccess(imageURL: String){
        mUserProfileImageURL = imageURL
        updateUserProfileDetails()
    }

    /**
     * A function to update user profile details to the firestore.
     */
    private fun updateUserProfileDetails(){
        val userHashMap = HashMap<String, Any>()

        // Get the FirstName from editText and trim the space
        val firstName = mBinding.etFirstName.text.toString().trim { it <= ' ' }
        if (firstName != mUserDetails.firstName) {
            userHashMap[Constants.FIRST_NAME] = firstName
        }

        // Get the LastName from editText and trim the space
        val lastName = mBinding.etLastName.text.toString().trim { it <= ' ' }
        if (lastName != mUserDetails.lastName) {
            userHashMap[Constants.LAST_NAME] = lastName
        }

        // Here the field which are not editable needs no update. So, we will update user Mobile Number and Gender for now.
        // Here we get the text from editText and trim the space
        val mobileNumber = mBinding.etMobileNumber.text.toString().trim { it <= ' ' }
        if (mobileNumber.isNotEmpty() && mobileNumber != mUserDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = mobileNumber.toLong()
        }

        val gender = if (rb_male.isChecked) {
            Constants.MALE
        } else {
            Constants.FEMALE
        }
        if(gender.isNotEmpty() && gender != mUserDetails.gender){
            userHashMap[Constants.GENDER] = gender
        }

        if (mUserProfileImageURL.isNotEmpty()) {
            userHashMap[Constants.IMAGE] = mUserProfileImageURL
        }

        userHashMap[Constants.GENDER] = gender
        userHashMap[Constants.COMPLETE_PROFILE]=1

        // Here if user is about to complete the profile then update the field or else no need.
        // 0: User profile is incomplete.
        // 1: User profile is completed.
        if (mUserDetails.profileCompleted == 0) {
            userHashMap[Constants.COMPLETE_PROFILE] = 1
        }

        // call the registerUser function of FireStore class to make an entry in the database.
        FirestoreClass().updateUserProfileData(this@UserProfileActivity, userHashMap)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == Constants.READ_STORAGE_PERMISSION_CODE){
            //If permission is granted
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //showErrorSnackBar("The storage permission is granted", false)
                Constants.showImageChooser(this@UserProfileActivity)
            }else{
                // Displaying another toast if permission is not granted
                Toast.makeText(this, resources.getString(R.string.read_storage_permission_denied),Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            if(requestCode == Constants.PICK_IMAGE_REQUEST_CODE){
                if(data!=null){
                    try {
                        // the uri of selected image from phone storage
                        mSelectedImageFileUri = data.data!!
                        //mBinding.ivUserPhoto.setImageURI(selectedImageFileUri)
                        // OR
                        GlideLoader(this@UserProfileActivity)
                            .loadUserPicture(mSelectedImageFileUri!!,mBinding.ivUserPhoto)

                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@UserProfileActivity,resources.getString(R.string.image_selection_failed),Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }else if(resultCode == Activity.RESULT_CANCELED){
            Log.e("Request Cancelled","Image selection cancelled")
        }
    }

}
