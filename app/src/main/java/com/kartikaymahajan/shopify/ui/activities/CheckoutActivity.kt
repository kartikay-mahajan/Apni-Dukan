package com.kartikaymahajan.shopify.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.ActivityCheckoutBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.Address
import com.kartikaymahajan.shopify.models.CartItem
import com.kartikaymahajan.shopify.models.Order
import com.kartikaymahajan.shopify.models.Product
import com.kartikaymahajan.shopify.ui.adapters.CartItemsListAdapter
import com.kartikaymahajan.shopify.utils.Constants
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

/**
 * A CheckOut activity screen.
 */
class CheckoutActivity : BaseActivity(), PaymentResultListener, AdapterView.OnItemSelectedListener {

    private lateinit var mBinding: ActivityCheckoutBinding
    // A global variable for the selected address details.
    private var mAddressDetails: Address? = null

    // A global variable for the product list.
    private lateinit var mProductsList: ArrayList<Product>

    // A global variable for the cart list.
    private lateinit var mCartItemsList: ArrayList<CartItem>

    // A global variable for the SubTotal Amount.
    private var mSubTotal: Double = 0.0

    // A global variable for the Total Amount.
    private var mTotalAmount: Double = 0.0

    // A global variable for Order details.
    private lateinit var mOrderDetails: Order

    private var paymentMethod:String = " Cash On Delivery"

    var payment_methods = arrayOf<String>("Cash on Delivery", "Online Payment")

    /**
     * This function is auto created by Android when the Activity Class is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        mBinding = ActivityCheckoutBinding.inflate(layoutInflater)
        // This is used to align the xml view to this class
        setContentView(mBinding.root)

        setupActionBar()

        if (intent.hasExtra(Constants.EXTRA_SELECTED_ADDRESS)) {
            mAddressDetails =
                intent.getParcelableExtra<Address>(Constants.EXTRA_SELECTED_ADDRESS)!!
        }

        if (mAddressDetails != null) {
            mBinding.tvCheckoutAddressType.text = mAddressDetails?.type
            mBinding.tvCheckoutFullName.text = mAddressDetails?.name
            mBinding.tvCheckoutAddress.text = "${mAddressDetails!!.address}, ${mAddressDetails!!.zipCode}"
            mBinding.tvCheckoutAdditionalNote.text = mAddressDetails?.additionalNote

            if (mAddressDetails?.otherDetails!!.isNotEmpty()) {
                mBinding.tvCheckoutOtherDetails.text = mAddressDetails?.otherDetails
            }
            mBinding.tvCheckoutMobileNumber.text = mAddressDetails?.mobileNumber
        }

        mBinding.btnPlaceOrder.setOnClickListener {
            placeAnOrder()
        }

        val spin = mBinding.paymentMode
        spin.onItemSelectedListener = this

        // Create the instance of ArrayAdapter
        // having the list of courses
        val ad: ArrayAdapter<*> = ArrayAdapter<Any?>(
            this,
            android.R.layout.simple_spinner_item,
            payment_methods
        )

        // set simple layout resource file
        // for each item of spinner
        ad.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)

        // Set the ArrayAdapter (ad) data on the
        // Spinner which binds data to spinner
        spin.adapter = ad

        getProductList()
    }

    /**
     * A function for actionBar Setup.
     */
    private fun setupActionBar() {

        setSupportActionBar(mBinding.toolbarCheckoutActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
        }

        mBinding.toolbarCheckoutActivity.setNavigationOnClickListener { onBackPressed() }
    }

    /**
     * A function to get product list to compare the current stock with the cart items.
     */
    private fun getProductList() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        FirestoreClass().getAllProductsList(this@CheckoutActivity)
    }

    /**
     * A function to get the success result of product list.
     *
     * @param productsList
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        mProductsList = productsList

        getCartItemsList()
    }

    /**
     * A function to get the list of cart items in the activity.
     */
    private fun getCartItemsList() {

        FirestoreClass().getCartList(this@CheckoutActivity)
    }

    /**
     * A function to notify the success result of the cart items list from cloud firestore.
     *
     * @param cartList
     */
    fun successCartItemsList(cartList: ArrayList<CartItem>) {

        // Hide progress dialog.
        hideProgressDialog()

        for (product in mProductsList) {
            for (cartItems in cartList) {
                if (product.product_id == cartItems.product_id) {
                    cartItems.stock_quantity = product.stock_quantity
                }
            }
        }

        mCartItemsList = cartList

        mBinding.rvCartListItems.layoutManager = LinearLayoutManager(this@CheckoutActivity)
        mBinding.rvCartListItems.setHasFixedSize(true)

        val cartListAdapter = CartItemsListAdapter(this@CheckoutActivity, mCartItemsList, false)
        mBinding.rvCartListItems.adapter = cartListAdapter

        for (item in mCartItemsList) {

            val availableQuantity = item.stock_quantity.toInt()

            if (availableQuantity > 0) {
                val price = item.price.toDouble()
                val quantity = item.cart_quantity.toInt()

                mSubTotal += (price * quantity)
            }
        }

        mBinding.tvCheckoutSubTotal.text = "$$mSubTotal"
        // Here we have kept Shipping Charge is fixed as $10 but in your case it may cary. Also, it depends on the location and total amount.
        mBinding.tvCheckoutShippingCharge.text = "$10.0"

        if (mSubTotal > 0) {
            mBinding.llCheckoutPlaceOrder.visibility = View.VISIBLE

            mTotalAmount = mSubTotal + 10.0
            mBinding.tvCheckoutTotalAmount.text = "$$mTotalAmount"
        } else {
            mBinding.llCheckoutPlaceOrder.visibility = View.GONE
        }
    }

    /**
     * A function to prepare the Order details to place an order.
     */
    private fun placeAnOrder() {

        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        mOrderDetails = Order(
            FirestoreClass().getCurrentUserID(),
            mCartItemsList,
            mAddressDetails!!,
            "My order ${System.currentTimeMillis()}",
            mCartItemsList[0].image,
            mSubTotal.toString(),
            "10.0", // The Shipping Charge is fixed as $10 for now in our case.
            mTotalAmount.toString(),
            System.currentTimeMillis(),
            paymentMethod
        )

        FirestoreClass().placeOrder(this@CheckoutActivity, mOrderDetails)
    }

    /**
     * A function to notify the success result of the order placed.
     */
    fun orderPlacedSuccess() {

        FirestoreClass().updateAllDetails(this@CheckoutActivity, mCartItemsList, mOrderDetails)
    }

    /**
     * A function to notify the success result after updating all the required details.
     */
    fun allDetailsUpdatedSuccessfully() {

        // Hide the progress dialog.
        hideProgressDialog()

        Toast.makeText(this@CheckoutActivity, "Your order placed successfully.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this@CheckoutActivity, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onPaymentSuccess(s: String?) {
        val alertDialog=AlertDialog.Builder(this)
        alertDialog.setTitle("Payment Id")
        alertDialog.setMessage(s)
        alertDialog.show()

        mBinding.tvPaymentStatus.text = "Payment Done"
        mBinding.tvPaymentStatus.setTextColor(Color.parseColor("#7CFC00"))
        mBinding.paymentMode.isEnabled = false
        mBinding.paymentMode.isClickable = false

        mBinding.btnPlaceOrder.performClick()
    }

    override fun onPaymentError(p0: Int, s: String?) {
        Toast.makeText(this,"Some Error occurred",Toast.LENGTH_SHORT).show()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(position){
            0->{
                paymentMethod = payment_methods[0]
                mBinding.tvPaymentStatus.text = "Payment Pending"
                mBinding.tvPaymentStatus.setTextColor(Color.parseColor("#DC143C"))

            }
            1->{
                paymentMethod = payment_methods[1]
                val activity: Activity = this
                val checkOut: Checkout = Checkout()
                checkOut.setKeyID("rzp_test_6HfjnvzDnzNwGJ")

                try {
                    val options = JSONObject()
                    options.put("name","Shopify")
                    options.put("description","Online Payment")
                    //You can omit the image option to fetch the image from dashboard
                    options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                    options.put("theme.color", "#3399cc");
                    options.put("currency","INR");
                    options.put("amount",(mTotalAmount*100).toString())//pass amount in currency subunits

                    checkOut.open(activity,options)
                }catch (e: Exception){
                    Toast.makeText(activity,"Error in payment: ",Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}