package net.xblacky.animexstream.utils.connectivity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.EXTRA_NETWORK_INFO
import android.net.NetworkInfo
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState.NotConnectedState
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProviderBaseImpl

@Suppress("DEPRECATION")
class ConnectivityProviderLegacyImpl(
    private val context: Context,
    private val cm: ConnectivityManager
) : ConnectivityProviderBaseImpl() {

    private val receiver = ConnectivityReceiver()

    override fun subscribe() {
        context.registerReceiver(receiver, IntentFilter(CONNECTIVITY_ACTION))
    }

    override fun unsubscribe() {
        context.unregisterReceiver(receiver)
    }

    override fun getNetworkState(): NetworkState {
        val activeNetworkInfo = cm.activeNetworkInfo
        return if (activeNetworkInfo != null) {
            ConnectedLegacy(activeNetworkInfo)
        } else {
            NotConnectedState
        }
    }

    private inner class ConnectivityReceiver : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            // on some devices ConnectivityManager.getActiveNetworkInfo() does not provide the correct network state
            // https://issuetracker.google.com/issues/37137911
            val networkInfo = cm.activeNetworkInfo
            val fallbackNetworkInfo: NetworkInfo? = intent.getParcelableExtra(EXTRA_NETWORK_INFO)
            // a set of dirty workarounds
            val state: NetworkState =
                if (networkInfo?.isConnectedOrConnecting == true) {
                    ConnectedLegacy(networkInfo)
                } else if (networkInfo != null && fallbackNetworkInfo != null &&
                    networkInfo.isConnectedOrConnecting != fallbackNetworkInfo.isConnectedOrConnecting
                ) {
                    ConnectedLegacy(fallbackNetworkInfo)
                } else {
                    val state = networkInfo ?: fallbackNetworkInfo
                    if (state != null) ConnectedLegacy(state) else NotConnectedState
                }
            dispatchChange(state)
        }
    }
}