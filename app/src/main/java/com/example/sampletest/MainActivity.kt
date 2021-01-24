package com.example.sampletest

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.sampletest.databinding.MainActivityBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    val mainActivityBinding: MainActivityBinding by lazy {MainActivityBinding.inflate(layoutInflater) }
    private var mConnectivityManager: ConnectivityManager?=null
    private var isNetworkConnected = false

    private val imageUrl = "https://picsum.photos/720/120"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mainActivityBinding.root)

        mConnectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        initNetworkCallback()

//        //If first time stored in pref
        if (LocalStorage(this@MainActivity).getBooleanValue(getString(R.string.pref_key_first_time))) {

            loadImage(imageUrl)
        }
        else{
            loadImage(imageUrl, true)
        }


        setupRefreshListener()

    }


    private fun setupRefreshListener() {
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

                        loadImage(imageUrl, firstTime)
                    }

            if(firstTime)
                LocalStorage(this@MainActivity).setBooleanValue(getString(R.string.pref_key_first_time),true)

            }
    }


    private fun initNetworkCallback() {
        val builder: NetworkRequest.Builder = NetworkRequest.Builder()
        mConnectivityManager?.registerNetworkCallback(builder.build(),
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

            mConnectivityManager?.unregisterNetworkCallback(ConnectivityManager.NetworkCallback())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}