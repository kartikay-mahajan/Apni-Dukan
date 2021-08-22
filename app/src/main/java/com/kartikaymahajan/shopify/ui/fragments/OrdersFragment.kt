package com.kartikaymahajan.shopify.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.FragmentOrdersBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.Order
import com.kartikaymahajan.shopify.ui.adapters.MyOrdersListAdapter

class OrdersFragment : BaseFragment() {

    private var mBinding: FragmentOrdersBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentOrdersBinding.inflate(layoutInflater,container,false)
        return mBinding!!.root
    }

    override fun onResume() {
        super.onResume()
        getMyOrdersList()
    }

    private fun getMyOrdersList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getMyOrdersList(this@OrdersFragment)
    }

    fun populateOrdersListInUI(ordersList: ArrayList<Order>){
        hideProgressDialog()

        if(ordersList.size > 0){
            mBinding!!.rvMyOrderItems.visibility = View.VISIBLE
            mBinding!!.tvNoOrdersFound.visibility = View.GONE

            mBinding!!.rvMyOrderItems.layoutManager = LinearLayoutManager(activity)
            mBinding!!.rvMyOrderItems.setHasFixedSize(true)

            val myOrdersAdapter = MyOrdersListAdapter(requireActivity(),ordersList)
            mBinding!!.rvMyOrderItems.adapter = myOrdersAdapter

        }else{
            mBinding!!.rvMyOrderItems.visibility = View.GONE
            mBinding!!.tvNoOrdersFound.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}