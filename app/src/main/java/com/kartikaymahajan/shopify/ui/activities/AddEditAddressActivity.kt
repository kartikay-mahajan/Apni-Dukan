package com.kartikaymahajan.shopify.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivityAddEditAddressBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.Address
import com.kartikaymahajan.shopify.utils.Constants
import kotlinx.android.synthetic.main.activity_add_edit_address.*

/**
 * Add edit address screen.
 */
class AddEditAddressActivity : BaseActivity() {

    private var mAddressDetails: Address? = null

    private lateinit var mBinding: ActivityAddEditAddressBinding

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        mBinding = ActivityAddEditAddressBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (intent.hasExtra(Constants.EXTRA_ADDRESS_DETAILS)) {
            mAddressDetails =
                intent.getParcelableExtra(Constants.EXTRA_ADDRESS_DETAILS)!!
        }

        setupActionBar()

        if (mAddressDetails != null) {
            if (mAddressDetails!!.id.isNotEmpty()) {

                mBinding.tvTitle.text = resources.getString(R.string.title_edit_address)
                mBinding.btnSubmitAddress.text = resources.getString(R.string.btn_lbl_update)

                mBinding.etFullName.setText(mAddressDetails?.name)
                mBinding.etPhoneNumber.setText(mAddressDetails?.mobileNumber)
                mBinding.etAddress.setText(mAddressDetails?.address)
                mBinding.etZipCode.setText(mAddressDetails?.zipCode)
                mBinding.etAdditionalNote.setText(mAddressDetails?.additionalNote)

                when (mAddressDetails?.type) {
                    Constants.HOME -> {
                        mBinding.rbHome.isChecked = true
                    }
                    Constants.OFFICE -> {
                        mBinding.rbOffice.isChecked = true
                    }
                    else -> {
                        mBinding.rbOther.isChecked = true
                        mBinding.tilOtherDetails.visibility = View.VISIBLE
                        mBinding.etOtherDetails.setText(mAddressDetails?.otherDetails)
                    }
                }
            }
        }

        mBinding.rgType.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rb_other) {
                mBinding.tilOtherDetails.visibility = View.VISIBLE
            } else {
                mBinding.tilOtherDetails.visibility = View.GONE
            }
        }

        mBinding.btnSubmitAddress.setOnClickListener {
            saveAddressToFirestore()
        }
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(mBinding.toolbarAddEditAddressActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        mBinding.toolbarAddEditAddressActivity.setNavigationOnClickListener { onBackPressed() }
    }


    /**
     * A function to validate the address input entries.
     */
    private fun validateData(): Boolean {
        return when {

            TextUtils.isEmpty(mBinding.etFullName.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_please_enter_full_name),
                    true
                )
                false
            }

            TextUtils.isEmpty(mBinding.etPhoneNumber.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(
                    resources.getString(R.string.err_msg_please_enter_phone_number),
                    true
                )
                false
            }

            TextUtils.isEmpty(mBinding.etAddress.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_address), true)
                false
            }

            TextUtils.isEmpty(mBinding.etZipCode.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }

            mBinding.rbOther.isChecked && TextUtils.isEmpty(
                mBinding.etZipCode.text.toString().trim { it <= ' ' }) -> {
                showErrorSnackBar(resources.getString(R.string.err_msg_please_enter_zip_code), true)
                false
            }
            else -> {
                true
            }
        }
    }

    /**
     * A function to save the address to the cloud firestore.
     */
    private fun saveAddressToFirestore() {

        // Here we get the text from editText and trim the space
        val fullName: String = mBinding.etFullName.text.toString().trim { it <= ' ' }
        val phoneNumber: String = mBinding.etPhoneNumber.text.toString().trim { it <= ' ' }
        val address: String = mBinding.etAddress.text.toString().trim { it <= ' ' }
        val zipCode: String = mBinding.etZipCode.text.toString().trim { it <= ' ' }
        val additionalNote: String = mBinding.etAdditionalNote.text.toString().trim { it <= ' ' }
        val otherDetails: String = mBinding.etOtherDetails.text.toString().trim { it <= ' ' }

        if (validateData()) {

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            val addressType: String = when {
                mBinding.rbHome.isChecked -> {
                    Constants.HOME
                }
                mBinding.rbOffice.isChecked -> {
                    Constants.OFFICE
                }
                else -> {
                    Constants.OTHER
                }
            }

            val addressModel = Address(
                FirestoreClass().getCurrentUserID(),
                fullName,
                phoneNumber,
                address,
                zipCode,
                additionalNote,
                addressType,
                otherDetails
            )

            if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
                FirestoreClass().updateAddress(
                    this@AddEditAddressActivity,
                    addressModel,
                    mAddressDetails!!.id
                )
            } else {
                FirestoreClass().addAddress(this@AddEditAddressActivity, addressModel)
            }
        }
    }

    /**
     * A function to notify the success result of address saved.
     */
    fun addUpdateAddressSuccess() {

        // Hide progress dialog
        hideProgressDialog()

        val notifySuccessMessage: String = if (mAddressDetails != null && mAddressDetails!!.id.isNotEmpty()) {
            resources.getString(R.string.msg_your_address_updated_successfully)
        } else {
            resources.getString(R.string.err_your_address_added_successfully)
        }

        Toast.makeText(this@AddEditAddressActivity, notifySuccessMessage, Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }
}