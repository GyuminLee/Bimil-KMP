package com.imbavchenko.bimil.di

import androidx.fragment.app.FragmentActivity
import java.lang.ref.WeakReference

class ActivityProvider {
    private var activityRef: WeakReference<FragmentActivity>? = null

    fun setActivity(activity: FragmentActivity) {
        activityRef = WeakReference(activity)
    }

    fun clearActivity() {
        activityRef?.clear()
        activityRef = null
    }

    fun getActivity(): FragmentActivity? = activityRef?.get()
}
