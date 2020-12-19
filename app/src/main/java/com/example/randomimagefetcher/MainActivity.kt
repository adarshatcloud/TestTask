package com.example.randomimagefetcher

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.randomimagefetcher.databinding.MainActivityBinding
import com.example.randomimagefetcher.interfaces.ImageUrlChooserCallBack
import com.example.randomimagefetcher.interfaces.LanguageChangeListener
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), ImageUrlChooserCallBack {

    lateinit var mainActivityBinding: MainActivityBinding
    private lateinit var mConnectivityManager: ConnectivityManager
    private var isNetworkConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivityBinding = MainActivityBinding.inflate(layoutInflater)
        setContentView(mainActivityBinding.root)

        mConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        initNetworkCallback()

        //If no url stored in preference
        if (LocalStorage(this@MainActivity).getStringValue(getString(R.string.pref_key_image_url)) == null) {
            //Ask user to choose an url to be used for loading the image
            showImageChooserDialog(this, this)
        }

        mainActivityBinding.fabReload.setOnClickListener {
            if (isNetworkConnected) {
                loadFreshImage()
            } else {
                Snackbar.make(
                    mainActivityBinding.mainLayout,
                    getString(R.string.str_message_not_connected),
                    Snackbar.LENGTH_LONG
                ).setAction(getString(R.string.label_close)) { }
                    .show()
            }
        }

        LocalStorage(this@MainActivity).getStringValue(getString(R.string.pref_key_image_url))
            ?.let { prefsImgUrl ->
                loadImage(prefsImgUrl, true)
            }


        mainActivityBinding.actionMenu.setOnClickListener {
            showPopupMenu(mainActivityBinding.actionMenu)
        }

    }

    private fun loadImage(url: String, isFirstTime: Boolean = false) {
        with(mainActivityBinding.imageView) image@{
            Glide.with(this.context).load(url).onlyRetrieveFromCache(isFirstTime)
                .skipMemoryCache(true)
                .error(R.drawable.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(this@image)
        }
    }

    private fun loadFreshImage(firstTime: Boolean = false) {
        thread {
            // Glide use image path as key to store cached image. Because We are using static image path
            // It is needed to clear cache before loading new image, otherwise glide will always load old image stored in cache
            Glide.get(this)
                .clearDiskCache()
            //This method needs to executed in main thread, otherwise the app will crash
            runOnUiThread {
                Glide.get(this)
                    .clearMemory()
                LocalStorage(this).getStringValue(getString(R.string.pref_key_image_url))
                    ?.let { prefsImgUrl ->
                        loadImage(prefsImgUrl, firstTime)
                    }
            }
        }
    }

    override fun onImageUrlChooserListener(imageUrl: String) {
        Log.e("ImageUrl", imageUrl)
        LocalStorage(this@MainActivity).setStringValue(
            getString(R.string.pref_key_image_url),
            imageUrl
        )
    }

    private fun showPopupMenu(view: View) {

        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menu ->

            when (menu.itemId) {

                R.id.action_url_change -> {
                    showImageChooserDialog(this, this)
                }

            }

            return@setOnMenuItemClickListener true
        }

        popupMenu.show()

    }

    private fun initNetworkCallback() {
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()
        mConnectivityManager.registerNetworkCallback(builder.build(),
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    isNetworkConnected = true
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    isNetworkConnected = false
                    Snackbar.make(
                        mainActivityBinding.mainLayout,
                        getString(R.string.str_message_not_connected),
                        Snackbar.LENGTH_LONG
                    ).setAction(getString(R.string.label_close)) { }
                        .show()
                }

            })

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mConnectivityManager.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}