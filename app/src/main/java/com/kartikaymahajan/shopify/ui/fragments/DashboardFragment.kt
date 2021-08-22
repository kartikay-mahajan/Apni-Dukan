package com.kartikaymahajan.shopify.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.databinding.FragmentDashboardBinding
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.Product
import com.kartikaymahajan.shopify.ui.activities.CartListActivity
import com.kartikaymahajan.shopify.ui.activities.ProductDetailsActivity
import com.kartikaymahajan.shopify.ui.activities.SettingsActivity
import com.kartikaymahajan.shopify.ui.adapters.DashboardItemsListAdapter
import com.kartikaymahajan.shopify.utils.Constants

class DashboardFragment : BaseFragment() {

    private var mBinding: FragmentDashboardBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onResume() {
        super.onResume()
        getDashboardItemsList()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        mBinding = FragmentDashboardBinding.inflate(inflater, container, false)

        return mBinding!!.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val id = item.itemId

        when(id){
            R.id.action_settings -> {
                startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            R.id.action_cart -> {
                startActivity(Intent(activity, CartListActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getDashboardItemsList(){
        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().getDashboardItemsList(this@DashboardFragment)
    }

    fun successDashboardItemsList(dashboardItemsList : ArrayList<Product>){
        hideProgressDialog()

        if(dashboardItemsList.size > 0){

            mBinding!!.rvDashboardItems.visibility = View.VISIBLE
            mBinding!!.tvNoDashboardItemsFound.visibility = View.GONE

            mBinding!!.rvDashboardItems.layoutManager = GridLayoutManager(activity, 2)
            mBinding!!.rvDashboardItems.setHasFixedSize(true)

            val adapter = DashboardItemsListAdapter(requireActivity(), dashboardItemsList)
            mBinding!!.rvDashboardItems.adapter = adapter
//
//            adapter.setOnClickListener(object: DashboardItemsListAdapter.OnClickListener{
//                override fun onClick(position: Int, product: Product) {
//                    val intent = Intent(context,ProductDetailsActivity::class.java)
//                    intent.putExtra(Constants.EXTRA_PRODUCT_ID,product.product_id)
//                    intent.pu
//                    startActivity(intent)
//                }
//            })

        } else {
            mBinding!!.rvDashboardItems.visibility = View.GONE
            mBinding!!.tvNoDashboardItemsFound.visibility = View.VISIBLE
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }
}