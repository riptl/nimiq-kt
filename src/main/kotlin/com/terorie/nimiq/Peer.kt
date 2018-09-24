package com.terorie.nimiq

class Peer(
    val channel: PeerChannel,
    val version: Int,
    val headHash: HashLight,
    val timeOffset: Int
) {

    init {
        setNetAddress()
    }

    fun setNetAddress() {
        // If the connector was able the determine the peer's netAddress, update the peer's advertised netAddress.
        if (channel.netAddress != null) {
            /*
             * TODO What to do if it doesn't match the currently advertised one?
             * This might happen if multiple IPs are assigned to a host.
             */
            // TODO Log some stuff

            // Only set the advertised netAddress if we have the public IP of the peer.
            // WebRTC connectors might return local IP addresses for peers on the same LAN.
            if (!channel.netAddress.isPrivate) {
                peerAddress.netAddress = channel.netAddress
            }
            // Otherwise, use the netAddress advertised for this peer if available.
            else if (this.channel.peerAddress.netAddress) {
                this.channel.netAddress = this.channel.peerAddress.netAddress
            }
            // Otherwise, we don't know the netAddress of this peer. Use a pseudo netAddress.
            else {
                this.channel.netAddress = NetAddress.UNKNOWN
            }
        }
    }

}
