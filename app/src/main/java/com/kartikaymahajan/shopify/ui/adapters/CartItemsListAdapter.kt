package com.kartikaymahajan.shopify.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kartikaymahajan.shopify.R
import com.kartikaymahajan.shopify.firestore.FirestoreClass
import com.kartikaymahajan.shopify.models.CartItem
import com.kartikaymahajan.shopify.ui.activities.CartListActivity
import com.kartikaymahajan.shopify.utils.Constants
import com.kartikaymahajan.shopify.utils.GlideLoader
import kotlinx.android.synthetic.main.item_cart_layout.view.*

open class CartItemsListAdapter(
    val context: Context,
    val list: ArrayList<CartItem>,
    private val updateCartItems: Boolean
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

         return MyViewHolder(
                LayoutInflater.from(context).inflate(
                    R.layout.item_cart_layout,parent,false
                )
            )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = list[position]
        if (holder is MyViewHolder){

            GlideLoader(context)
                .loadProductPicture(model.image,holder.itemView.iv_cart_item_image)

            holder.itemView.tv_cart_item_title.text = model.title
            holder.itemView.tv_cart_item_price.text = "$${model.price}"
            holder.itemView.tv_cart_quantity.text = model.cart_quantity

            if (model.cart_quantity == "0") {
                holder.itemView.ib_remove_cart_item.visibility = View.GONE
                holder.itemView.ib_add_cart_item.visibility = View.GONE

                if(updateCartItems){
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                }else{
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }

                holder.itemView.tv_cart_quantity.text =
                    context.resources.getString(R.string.lbl_out_of_stock)

                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context, R.color.colorSnackBarError
                    )
                )
            } else {
                if(updateCartItems) {
                    holder.itemView.ib_remove_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_add_cart_item.visibility = View.VISIBLE
                    holder.itemView.ib_delete_cart_item.visibility = View.VISIBLE
                }else{
                    holder.itemView.ib_remove_cart_item.visibility = View.GONE
                    holder.itemView.ib_add_cart_item.visibility = View.GONE
                    holder.itemView.ib_delete_cart_item.visibility = View.GONE
                }

                holder.itemView.tv_cart_quantity.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.item_value_text_color
                    )
                )
            }

            holder.itemView.ib_delete_cart_item.setOnClickListener{
                when(context){
                    is CartListActivity -> {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }
                }
                FirestoreClass().removeItemFromCart(context,model.id)
            }

            holder.itemView.ib_remove_cart_item.setOnClickListener{

                if (model.cart_quantity == "1"){
                    FirestoreClass().removeItemFromCart(context,model.id)
                }else{
                    val cartQuantity: Int = model.cart_quantity.toInt()
                    val itemHashMap = HashMap<String,Any>()
                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity -1).toString()

                    if (context is CartListActivity){
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FirestoreClass().updateMyCart(context,model.id,itemHashMap)
                }

            }
            holder.itemView.ib_add_cart_item.setOnClickListener {

                val cartQuantity: Int = model.cart_quantity.toInt()

                if (cartQuantity < model.stock_quantity.toInt()) {

                    val itemHashMap = HashMap<String, Any>()
                    itemHashMap[Constants.CART_QUANTITY] = (cartQuantity + 1).toString()

                    if (context is CartListActivity) {
                        context.showProgressDialog(context.resources.getString(R.string.please_wait))
                    }

                    FirestoreClass().updateMyCart(context, model.id, itemHashMap)
                }else{
                    if (context is CartListActivity){
                        context.showErrorSnackBar(
                            context.resources.getString(
                                R.string.msg_for_available_stock,model.stock_quantity
                            ),true
                        )
                    }
                }
            }


        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

}