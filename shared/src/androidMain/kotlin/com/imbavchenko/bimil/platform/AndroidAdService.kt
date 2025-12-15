package com.imbavchenko.bimil.platform

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.imbavchenko.bimil.data.ad.AdService
import com.imbavchenko.bimil.di.ActivityProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AndroidAdService(
    private val context: Context,
    private val activityProvider: ActivityProvider
) : AdService {

    private var interstitialAd: InterstitialAd? = null
    private val prefs = context.getSharedPreferences("ad_prefs", Context.MODE_PRIVATE)

    companion object {
        // Test ad IDs - replace with production IDs for release
        const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    }

    override fun initialize() {
        MobileAds.initialize(context)
        loadInterstitialAd()
    }

    override fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    override fun showInterstitialAd(onComplete: () -> Unit) {
        val activity = activityProvider.getActivity()

        if (interstitialAd != null && activity != null && canShowInterstitialToday()) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd()
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd()
                    onComplete()
                }
            }
            markInterstitialShown()
            interstitialAd?.show(activity)
        } else {
            onComplete()
        }
    }

    override fun canShowInterstitialToday(): Boolean {
        val lastShownDate = prefs.getString("last_interstitial_date", null)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return lastShownDate != today
    }

    override fun markInterstitialShown() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        prefs.edit().putString("last_interstitial_date", today).apply()
    }
}
