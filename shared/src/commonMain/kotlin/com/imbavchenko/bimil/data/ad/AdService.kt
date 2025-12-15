package com.imbavchenko.bimil.data.ad

interface AdService {
    fun initialize()
    fun loadInterstitialAd()
    fun showInterstitialAd(onComplete: () -> Unit)
    fun canShowInterstitialToday(): Boolean
    fun markInterstitialShown()
}
