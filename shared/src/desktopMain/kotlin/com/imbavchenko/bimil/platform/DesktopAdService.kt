package com.imbavchenko.bimil.platform

import com.imbavchenko.bimil.data.ad.AdService

class DesktopAdService : AdService {
    override fun initialize() {
        // No ads on desktop
    }

    override fun loadInterstitialAd() {
        // No ads on desktop
    }

    override fun showInterstitialAd(onComplete: () -> Unit) {
        // No ads on desktop - just complete immediately
        onComplete()
    }

    override fun canShowInterstitialToday(): Boolean {
        return false
    }

    override fun markInterstitialShown() {
        // No-op on desktop
    }
}
