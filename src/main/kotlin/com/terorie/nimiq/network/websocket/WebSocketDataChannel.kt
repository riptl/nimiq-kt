package com.terorie.nimiq.network.websocket

import com.terorie.nimiq.network.DataChannel
import java.nio.ByteBuffer
import javax.websocket.Session

@ExperimentalUnsignedTypes
class WebSocketDataChannel(val session: Session) : DataChannel() {

    override fun sendChunk(buf: ByteArray) {
        session.basicRemote.sendBinary(ByteBuffer.wrap(buf))
    }

    override val readyState = when {
        session.isOpen -> ReadyState.OPEN
        else -> ReadyState.CLOSED
    }

    override fun close() = TODO()

}
