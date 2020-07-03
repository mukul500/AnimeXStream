package net.xblacky.animexstream.utils.connectivity

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState.ConnectedState.Connected
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider.NetworkState.NotConnectedState
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProviderBaseImpl

@RequiresApi(Build.VERSION_CODES.N)
class ConnectivityProviderImpl(private val cm: ConnectivityManager) :
    ConnectivityProviderBaseImpl() {

    private val networkCallback = ConnectivityCallback()

    override fun subscribe() {
        cm.registerDefaultNetworkCallback(networkCallback)
    }

    override fun unsubscribe() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    override fun getNetworkState(): NetworkState {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (capabilities != null) {
            Connected(capabilities)
        } else {
            NotConnectedState
        }
    }

    private inner class ConnectivityCallback : NetworkCallback() {

        override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
            dispatchChange(Connected(capabilities))
        }

        override fun onLost(network: Network) {
            dispatchChange(NotConnectedState)
        }
    }
}