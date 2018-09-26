package com.terorie.nimiq.network

enum class Protocol(val id: Int, val scheme: String) {
    DUMB(0, "dumb"),
    WSS(1, "wss"),
    RTC(2, "rtc"),
    WS(4, "ws"),
}
