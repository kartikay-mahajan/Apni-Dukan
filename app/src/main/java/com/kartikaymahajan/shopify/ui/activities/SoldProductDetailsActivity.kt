package com.kartikaymahajan.shopify.ui.activities

import android.os.Bundle
import android.view.View
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivitySoldProductDetailsBinding
import com.kartikaymahajan.shopify.models.SoldProduct
import com.kartikaymahajan.shopify.utils.Constants
import com.kartikaymahajan.shopify.utils.GlideLoader
import java.text.SimpleDateFormat
import java.util.*

/**
 * A detail screen for the sold product item.
 */
class SoldProductDetailsActivity : BaseActivity() {

    private lateinit var mBinding: ActivitySoldProductDetailsBinding
    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        mBinding = ActivitySoldProductDetailsBinding.inflate(layoutInflater)
        // This is used to align the xml view to this class
        setContentView(mBinding.root)

        var productDetails: SoldProduct = SoldProduct()

        if (intent.hasExtra(Constants.EXTRA_SOLD_PRODUCT_DETAILS)) {
            productDetails =
                intent.getParcelableExtra<SoldProduct>(Constants.EXTRA_SOLD_PRODUCT_DETAILS)!!
        }
        setupActionBar()

        setupUI(productDetails)
    }
    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(mBinding.toolbarSoldProductDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        mBinding.toolbarSoldProductDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }
    /**
     * A function to setup UI.
     *
     * @param productDetails Order details received through intent.
     */
    private fun setupUI(productDetails: SoldProduct) {

        mBinding.tvSoldProductDetailsId.text = productDetails.order_id

        // Date Format in which the date will be displayed in the UI.
        val dateFormat = "dd MMM yyyy HH:mm"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = productDetails.order_date
        mBinding.tvSoldProductDetailsDate.text = formatter.format(calendar.time)

        GlideLoader(this@SoldProductDetailsActivity).loadProductPicture(
            productDetails.image,
            mBinding.ivProductItemImage
        )
        mBinding.tvProductItemName.text = productDetails.title
        mBinding.tvProductItemPrice.text ="$${productDetails.price}"
        mBinding.tvSoldProductQuantity.text = productDetails.sold_quantity

        mBinding.tvSoldDetailsAddressType.text = productDetails.address.type
        mBinding.tvSoldDetailsFullName.text = productDetails.address.name
        mBinding.tvSoldDetailsAddress.text =
            "${productDetails.address.address}, ${productDetails.address.zipCode}"
        mBinding.tvSoldDetailsAdditionalNote.text = productDetails.address.additionalNote

        if (productDetails.address.otherDetails.isNotEmpty()) {
            mBinding.tvSoldDetailsOtherDetails.visibility = View.VISIBLE
            mBinding.tvSoldDetailsOtherDetails.text = productDetails.address.otherDetails
        } else {
            mBinding.tvSoldDetailsOtherDetails.visibility = View.GONE
        }
        mBinding.tvSoldDetailsMobileNumber.text = productDetails.address.mobileNumber

        mBinding.tvSoldProductSubTotal.text = productDetails.sub_total_amount
        mBinding.tvSoldProductShippingCharge.text = productDetails.shipping_charge
        mBinding.tvSoldProductTotalAmount.text = productDetails.total_amount
    }
}
