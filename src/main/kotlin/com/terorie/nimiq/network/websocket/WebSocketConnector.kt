package com.terorie.nimiq.network.websocket

import com.terorie.nimiq.network.DataChannel.ReadyState
import com.terorie.nimiq.network.NetworkConfig
import com.terorie.nimiq.network.Protocol
import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.network.address.WsBasePeerAddress
import com.terorie.nimiq.network.connection.NetworkConnection
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.InetAddress
import java.net.URI
import javax.websocket.*

@ExperimentalUnsignedTypes
class WebSocketConnector(
    val protocol: Protocol,
    val networkConfig: NetworkConfig
) {

    companion object {
        private val wsContainer = ContainerProvider.getWebSocketContainer()
    }

    // TODO Server

    private val sockets = HashMap<PeerAddress, Conn>()

    var onConnection: ((NetworkConnection) -> Unit)? = null
    var onError: ((PeerAddress, Exception?) -> Unit)? = null

    fun connect(peerAddress: WsBasePeerAddress): Boolean {
        if (peerAddress.protocol != protocol)
            throw IllegalArgumentException("protocol mismatch")

        val conf = ClientEndpointConfig.Builder.create().build()
        val conn = Conn(peerAddress)
        var session: Session? = null
        conn.onOpen = {
            sockets.remove(peerAddress)

            // Don't fire error events after the connection has been established.
            conn.onError = null

            val networkConn = NetworkConnection(
                WebSocketDataChannel(session!!),
                protocol,
                // TODO DNS round robin issues?
                InetAddress.getByName(peerAddress.host),
                peerAddress
            )
        }
        conn.onError = { _, exception ->
            sockets.remove(peerAddress)
            onError?.invoke(peerAddress, exception)
        }

        session = wsContainer.connectToServer(conn.handle, conf,
            URI("${protocol.scheme}://${peerAddress.host}:${peerAddress.port}"))

        // TODO Timeout

        return true
    }

    fun abort(peerAddress: PeerAddress) {
        val socket = sockets[peerAddress]
            ?: return

        sockets.remove(peerAddress)
        socket.onError = null
    }

    private inner class Conn(val peerAddress: PeerAddress) {

        var readyState = ReadyState.CONNECTING
        var onOpen: (() -> Unit)? = null
        var onClose: (() -> Unit)? = null
        var onError: ((PeerAddress, Exception?) -> Unit)? = null

        val handle = object : Endpoint() {
            override fun onOpen(session: Session?, config: EndpointConfig?) {
                sockets.remove(peerAddress)

                readyState = ReadyState.OPEN
                this@Conn.onOpen?.invoke()
            }

            override fun onClose(session: Session?, closeReason: CloseReason?) {
                this@Conn.onClose?.invoke()
            }

            override fun onError(session: Session?, thr: Throwable?) {
                // Don't report error events after the connection has been established.
                if (readyState == ReadyState.OPEN)
                    return

                sockets.remove(peerAddress)
                this@Conn.onError?.invoke(peerAddress, thr as? Exception)
            }
        }

    }

}
