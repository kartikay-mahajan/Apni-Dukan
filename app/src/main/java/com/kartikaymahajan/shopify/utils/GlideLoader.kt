package com.kartikaymahajan.shopify.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.kartikaymahajan.shopify.R
import java.io.IOException

class GlideLoader(val context: Context) {

    fun loadUserPicture(image: Any, imageView: ImageView){
        try{
            //Load the user image in the ImageView
            Glide.with(context)
                .load(image)  //URI of image
                .centerCrop()  // scale type of image
                .placeholder(R.drawable.ic_user_placeholder) // default imageview if image failed to load
                .into(imageView) // the view in which the image will be loaded
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    fun loadProductPicture(image: Any,imageView: ImageView){
        try {
            //Load the user image in the ImageView
            Glide.with(context)
                .load(image)  //URI of image
                .centerCrop()  // scale type of image
                .into(imageView) // the view in which the image will be loaded
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

}