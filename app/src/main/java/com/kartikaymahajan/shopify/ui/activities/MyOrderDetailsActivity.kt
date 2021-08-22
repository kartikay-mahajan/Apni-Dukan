package com.kartikaymahajan.shopify.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivityMyOrderDetailsBinding
import com.kartikaymahajan.shopify.models.Order
import com.kartikaymahajan.shopify.ui.adapters.CartItemsListAdapter
import com.kartikaymahajan.shopify.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * My Order Details Screen.
 */
class MyOrderDetailsActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMyOrderDetailsBinding

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        mBinding = ActivityMyOrderDetailsBinding.inflate(layoutInflater)
        // This is used to align the xml view to this class
        setContentView(mBinding.root)

        setupActionBar()

        var myOrderDetails: Order = Order()

        if (intent.hasExtra(Constants.EXTRA_MY_ORDER_DETAILS)) {
            myOrderDetails =
                intent.getParcelableExtra<Order>(Constants.EXTRA_MY_ORDER_DETAILS)!!
        }

        setupUI(myOrderDetails)
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(mBinding.toolbarMyOrderDetailsActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        mBinding.toolbarMyOrderDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to setup UI.
     *
     * @param orderDetails Order details received through intent.
     */
    private fun setupUI(orderDetails: Order) {

        mBinding.tvOrderDetailsId.text = orderDetails.title

        // Date Format in which the date will be displayed in the UI.
        val dateFormat = "dd MMM yyyy HH:mm"
        // Create a DateFormatter object for displaying date in specified format.
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = orderDetails.order_datetime

        val orderDateTime = formatter.format(calendar.time)
        mBinding.tvOrderDetailsDate.text = orderDateTime

        // Get the difference between the order date time and current date time in hours.
        // If the difference in hours is 1 or less then the order status will be PENDING.
        // If the difference in hours is 2 or greater then 1 then the order status will be PROCESSING.
        // And, if the difference in hours is 3 or greater then the order status will be DELIVERED.

        val diffInMilliSeconds: Long = System.currentTimeMillis() - orderDetails.order_datetime
        val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMilliSeconds)
        Log.e("Difference in Hours", "$diffInHours")

        when {
            diffInHours < 1 -> {
                mBinding.tvOrderStatus.text = resources.getString(R.string.order_status_pending)
                mBinding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorAccent
                    )
                )
            }
            diffInHours < 2 -> {
                mBinding.tvOrderStatus.text = resources.getString(R.string.order_status_in_process)
                mBinding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorOrderStatusInProcess
                    )
                )
            }
            else -> {
                mBinding.tvOrderStatus.text = resources.getString(R.string.order_status_delivered)
                mBinding.tvOrderStatus.setTextColor(
                    ContextCompat.getColor(
                        this@MyOrderDetailsActivity,
                        R.color.colorOrderStatusDelivered
                    )
                )
            }
        }

        mBinding.rvMyOrderItemsList.layoutManager = LinearLayoutManager(this@MyOrderDetailsActivity)
        mBinding.rvMyOrderItemsList.setHasFixedSize(true)

        val cartListAdapter =
            CartItemsListAdapter(this@MyOrderDetailsActivity, orderDetails.items, false)
        mBinding.rvMyOrderItemsList.adapter = cartListAdapter

        mBinding.tvMyOrderDetailsAddressType.text = orderDetails.address.type
        mBinding.tvMyOrderDetailsFullName.text = orderDetails.address.name
        mBinding.tvMyOrderDetailsAddress.text =
            "${orderDetails.address.address}, ${orderDetails.address.zipCode}"
        mBinding.tvMyOrderDetailsAdditionalNote.text = orderDetails.address.additionalNote

        if (orderDetails.address.otherDetails.isNotEmpty()) {
            mBinding.tvMyOrderDetailsOtherDetails.visibility = View.VISIBLE
            mBinding.tvMyOrderDetailsOtherDetails.text = orderDetails.address.otherDetails
        } else {
            mBinding.tvMyOrderDetailsOtherDetails.visibility = View.GONE
        }
        mBinding.tvMyOrderDetailsMobileNumber.text = orderDetails.address.mobileNumber

        mBinding.tvOrderDetailsSubTotal.text = orderDetails.sub_total_amount
        mBinding.tvOrderDetailsShippingCharge.text = orderDetails.shipping_charge
        mBinding.tvOrderDetailsTotalAmount.text = orderDetails.total_amount

        mBinding.paymentMode.text = orderDetails.payment_method
        mBinding.paymentMode.setTextColor(resources.getColor(R.color.colorDarkGrey))
    }
}