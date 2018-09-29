package com.terorie.nimiq.network.connection

import com.terorie.nimiq.network.DataChannel
import com.terorie.nimiq.network.DataChannel.ReadyState
import com.terorie.nimiq.network.Protocol
import com.terorie.nimiq.network.address.PeerAddress
import com.terorie.nimiq.network.message.Message
import java.lang.Exception
import java.net.InetAddress

@ExperimentalUnsignedTypes
class NetworkConnection(
    private val channel: DataChannel,
    val protocol: Protocol,
    var inetAddress: InetAddress?,
    val peerAddress: PeerAddress?
) {

    var bytesSent = 0L
    var bytesReceived = 0L
    var closed = false
    val inbound = peerAddress == null // TODO not nice
    var lastError: Exception? = null

    private val readyState = channel.readyState

    // Callbacks
    var onClose: ((NetworkConnection, CloseType, String) -> Unit)? = null

    fun send(buf: ByteArray): Boolean {
        when (readyState) {
            // Fire close event (early) if channel is closing/closed.
            ReadyState.CLOSING,
            ReadyState.CLOSED -> {
                // TODO log
                onClose?.invoke(this, CloseType.CHANNEL_CLOSING, "channel closing")
                return false
            }

            // Don't attempt to send if channel is not (yet) open.
            ReadyState.CONNECTING -> {
                // TODO log
                return false
            }

            // Channel open
            else -> Unit
        }

        try {
            channel.send(buf)
            bytesSent += buf.size
            return true
        } catch (e: Exception) {
            // TODO log
            return false
        }
    }

    fun expectMessage(
        types: Array<Message.Type>,
        onTimeout: () -> Unit,
        msgTimeout: Int,
        chunkTimeout: Int
    ) {
        if (closed)
            return
        channel.expectMessage(types, onTimeout, msgTimeout, chunkTimeout)
    }

    fun isExpectingMessage(type: Message.Type): Boolean {
        if (closed)
            return false
        return channel.isExpectingMessage(type)
    }

    fun confirmExpectedMessage(type: Message.Type, success: Boolean) {
        if (closed)
            return
        channel.confirmExpectedMessage(type, success)
    }

    fun close(type: CloseType, reason: String) {
        if (!closed)
            {} // TODO log
        closeNow(type, reason)
    }

    private fun closeNow(type: CloseType, reason: String) {
        if (closed)
            return

        // Don't wait for the close event to fire.
        callbackClose(type, reason)

        // Close the channel.
        channel.close()

        // Clear callbacks
        onClose = null
    }

    private fun callbackClose(type: CloseType, reason: String) {
        var _type = type
        var _reason = reason

        // Don't callback close event again when already closed.
        if (closed)
            return

        // Mark this connection as closed
        closed = true

        // Propagate last network error
        if (_type == CloseType.CLOSED_BY_REMOTE && lastError != null) {
            _type = CloseType.NETWORK_ERROR
            _reason = lastError.toString()
        }

        // Tell listener that this connection has closed.
        onClose?.invoke(this, _type, _reason)
    }

}
