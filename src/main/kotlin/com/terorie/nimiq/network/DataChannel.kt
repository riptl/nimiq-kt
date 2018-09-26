package com.terorie.nimiq.network

import com.terorie.nimiq.network.message.Message
import java.lang.IllegalArgumentException

@ExperimentalUnsignedTypes
abstract class DataChannel {

    enum class ReadyState(val id: Int) {
        CONNECTING(0),
        OPEN(1),
        CLOSING(2),
        CLOSED(3);
    }

    companion object {
        const val CHUNK_SIZE_MAX = 1024 * 16 // 16 kiB
        const val MESSAGE_SIZE_MAX = 10 * 1024 * 1024 // 10 MiB
        const val CHUNK_TIMEOUT = 1000 * 5 // 5 seconds
        const val MESSAGE_TIMEOUT = 120
    }

    private val expectedMsgsByType = HashMap<Message.Type, ExpectedMessage>()

    private var sendingTag: UByte = 0U

    fun isExpectingMessage(type: Message.Type) =
        expectedMsgsByType.containsKey(type)

    fun confirmExpectedMessage(type: Message.Type, success: Boolean) {
        val expectedMsg = expectedMsgsByType[type]
            ?: return
    }

    fun expectMessage(
            types: Array<Message.Type>,
            onTimeout: () -> Unit,
            msgTimeout: Int = MESSAGE_TIMEOUT,
            chunkTimeout: Int = CHUNK_TIMEOUT) {
        val expectedMsg = ExpectedMessage(types, onTimeout, msgTimeout, chunkTimeout)
        for (type in types)
            expectedMsgsByType[type] = expectedMsg
    }

    fun send(buf: ByteArray) {
        if (buf.size > MESSAGE_SIZE_MAX)
            throw IllegalArgumentException("DataChannel::send() max message size exceeded")

        val tag = sendingTag
        sendingTag = ((sendingTag + 1) % 0xFFU).toUByte()
        sendChunked(buf, tag)
    }

    protected fun sendChunked(buf: ByteArray, tag: UByte) {
        // Send chunks.
        var offset = 0
        var remaining = buf.size
        while (remaining > 0) {
            val chunkSize =
                if (remaining + 1 >= CHUNK_SIZE_MAX)
                    CHUNK_SIZE_MAX - 1
                else
                    remaining
            val chunk = ByteArray(1 + chunkSize)
            chunk[0] = tag.toByte()
            System.arraycopy(buf, 0, chunk, 1, chunkSize)
            sendChunk(chunk)
            offset += chunkSize
            remaining -= chunkSize
        }
    }

    abstract fun close()
    protected abstract fun sendChunk(buf: ByteArray)
    abstract val readyState: ReadyState

}

@ExperimentalUnsignedTypes
class ExpectedMessage(
    val types: Array<Message.Type>,
    val onTimeout: () -> Unit,
    val msgTimeout: Int,
    val chunkTimeout: Int
)
