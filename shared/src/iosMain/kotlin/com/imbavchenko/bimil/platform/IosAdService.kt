package com.imbavchenko.bimil.platform

import com.imbavchenko.bimil.data.ad.AdService

class IosAdService : AdService {
    override fun initialize() {
        // TODO: Implement iOS AdMob when needed
    }

    override fun loadInterstitialAd() {
        // TODO: Implement iOS AdMob when needed
    }

    override fun showInterstitialAd(onComplete: () -> Unit) {
        // No ads on iOS yet - just complete immediately
        onComplete()
    }

    override fun canShowInterstitialToday(): Boolean {
        return false
    }

    override fun markInterstitialShown() {
        // No-op on iOS yet
    }
}
