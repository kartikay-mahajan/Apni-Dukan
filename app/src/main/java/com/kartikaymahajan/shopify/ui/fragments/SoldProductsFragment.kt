package com.kartikaymahajan.shopify.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.FragmentSoldProductsBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.SoldProduct
import com.kartikaymahajan.shopify.ui.adapters.SoldProductsListAdapter

/**
 * A simple [Fragment] subclass.
 * Use the [SoldProductsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SoldProductsFragment : BaseFragment() {

    private var mBinding: FragmentSoldProductsBinding? =null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentSoldProductsBinding.inflate(inflater,container,false)
        return mBinding!!.root
    }
    override fun onResume() {
        super.onResume()

        getSoldProductsList()
    }

    private fun getSoldProductsList() {
        // Show the progress dialog.
        showProgressDialog(resources.getString(R.string.please_wait))

        // Call the function of Firestore class.
        FirestoreClass().getSoldProductsList(this@SoldProductsFragment)
    }

    /**
     * A function to get the list of sold products.
     */
    fun successSoldProductsList(soldProductsList: ArrayList<SoldProduct>) {

        // Hide Progress dialog.
        hideProgressDialog()

        if (soldProductsList.size > 0) {
            mBinding!!.rvSoldProductItems.visibility = View.VISIBLE
            mBinding!!.tvNoSoldProductsFound.visibility = View.GONE

            mBinding!!.rvSoldProductItems.layoutManager = LinearLayoutManager(activity)
            mBinding!!.rvSoldProductItems.setHasFixedSize(true)

            val soldProductsListAdapter =
                SoldProductsListAdapter(requireActivity(), soldProductsList)
            mBinding!!.rvSoldProductItems.adapter = soldProductsListAdapter
        } else {
            mBinding!!.rvSoldProductItems.visibility = View.GONE
            mBinding!!.tvNoSoldProductsFound.visibility = View.VISIBLE
        }
    }

}