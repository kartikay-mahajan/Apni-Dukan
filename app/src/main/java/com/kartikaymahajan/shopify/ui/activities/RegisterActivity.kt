package com.kartikaymahajan.shopify.ui.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivityRegisterBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.User

class RegisterActivity : BaseActivity() {

    private lateinit var mBinding:ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        setupActionBar()
        mBinding.tvLogin.setOnClickListener{
            onBackPressed()
        }
        mBinding.btnRegister.setOnClickListener{
            registerUser()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(mBinding.toolbarRegisterActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }
        mBinding.toolbarRegisterActivity.setNavigationOnClickListener{ onBackPressed() }

    }

    /**
     * A function to validate the entries of a new user.
     */
    private fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(mBinding.etFirstName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(mBinding.etLastName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }

            TextUtils.isEmpty(mBinding.etEmail.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(mBinding.etPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(mBinding.etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_enter_confirm_password), true)
                false
            }

            mBinding.etPassword.text.toString().trim { it <= ' ' } != mBinding.etConfirmPassword.text.toString()
                .trim { it <= ' ' } -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_password_and_confirm_password_mismatch), true)
                false
            }
            !mBinding.cbTermsAndCondition.isChecked -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_agree_terms_and_condition), true)
                false
            }
            else -> {
                //showErrorSnackBar(resources.getString(R.string.registry_successful), false)
                true
            }
        }
    }

    private fun registerUser(){
        if(validateRegisterDetails()) {

            showProgressDialog(resources.getString(R.string.please_wait))

            val email : String = mBinding.etEmail.text.toString().trim(){ it <= ' ' }
            val password : String = mBinding.etPassword.text.toString().trim(){ it <= ' ' }

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(
                    OnCompleteListener<AuthResult> { task ->

                        if (task.isSuccessful){
                            val firebaseUser: FirebaseUser = task.result!!.user!!

                            val user = User(
                                firebaseUser.uid,
                                mBinding.etFirstName.text.toString().trim{ it <= ' '},
                                mBinding.etLastName.text.toString().trim{ it <= ' '},
                                mBinding.etEmail.text.toString().trim{ it <= ' '}
                            )

                            FirestoreClass().registerUser(this@RegisterActivity,user)

                            FirebaseAuth.getInstance().signOut()
                            finish()

                        }else{
                            hideProgressDialog()
                            showErrorSnackBar(task.exception!!.message.toString(),true)
                        }
                    })
        }
    }

    fun userRegistrationSuccess(){
        hideProgressDialog()

        Toast.makeText(this@RegisterActivity,resources.getString(R.string.register_success),Toast.LENGTH_SHORT).show()


        /**
         * Here the new user registered is automatically signed-in so we just sign-out the user from firebase
         * and send him to Intro Screen for Sign-In
         */
        FirebaseAuth.getInstance().signOut()
        // Finish the Register Screen
        finish()
    }

}