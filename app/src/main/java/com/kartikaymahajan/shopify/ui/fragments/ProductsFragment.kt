package com.kartikaymahajan.shopify.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.FragmentProductsBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.Product
import com.kartikaymahajan.shopify.ui.activities.AddProductActivity
import com.kartikaymahajan.shopify.ui.adapters.MyProductsListAdapter
import kotlinx.android.synthetic.main.fragment_products.*

class ProductsFragment : BaseFragment() {

    private var mBinding: FragmentProductsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentProductsBinding.inflate(inflater, container, false)

        return mBinding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_product_menu,menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onResume() {
        super.onResume()
        getProductListFromFirestore()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId
        when(id){
            R.id.action_add_product -> {
                startActivity(Intent(activity, AddProductActivity::class.java))
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun getProductListFromFirestore(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getProductsList(this@ProductsFragment)
    }

    /**
     * A function to get the successful product list from cloud firestore.
     *
     * @param productsList Will receive the product list from cloud firestore.
     */
    fun successProductsListFromFireStore(productsList: ArrayList<Product>) {

        // Hide Progress dialog.
        hideProgressDialog()

        if (productsList.size > 0) {
            mBinding!!.rvMyProductItems.visibility = View.VISIBLE
            mBinding!!.tvNoProductsFound.visibility = View.GONE

            mBinding!!.rvMyProductItems.layoutManager = LinearLayoutManager(activity)
            mBinding!!.rvMyProductItems.setHasFixedSize(true)

            val adapterProducts =
                MyProductsListAdapter(requireActivity(), productsList, this@ProductsFragment)

            mBinding!!.rvMyProductItems.adapter = adapterProducts
        } else {
            mBinding!!.rvMyProductItems.visibility = View.GONE
            mBinding!!.tvNoProductsFound.visibility = View.VISIBLE
        }
    }

    fun deleteProduct(productID: String){
        // Here we will call the delete function of the FirestoreClass. But, for now lets display the Toast message and call this function from adapter class.
        showAlertDialogToDeleteProduct(productID)
    }

    fun productDeleteSuccess(){
        hideProgressDialog()
        Toast.makeText(requireActivity(),resources.getString(R.string.product_delete_success_message),Toast.LENGTH_SHORT).show()

        getProductListFromFirestore()
    }

    /**
     * A function to show the alert dialog for the confirmation of delete product from cloud firestore.
     */
    private fun showAlertDialogToDeleteProduct(productID: String) {

        val builder = AlertDialog.Builder(requireActivity())
        //set title for alert dialog
        builder.setTitle(resources.getString(R.string.delete_dialog_title))
        //set message for alert dialog
        builder.setMessage(resources.getString(R.string.delete_dialog_message))
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->

            // Show the progress dialog.
            showProgressDialog(resources.getString(R.string.please_wait))

            // Call the function of Firestore class.
            FirestoreClass().deleteProduct(this@ProductsFragment, productID)

            dialogInterface.dismiss()
        }

        //performing negative action
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->

            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}