package com.example.neuroed.network

import android.util.Log
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MyWebSocketListener : WebSocketListener(){

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "Connection opened")
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d("WebSocket", "Received text: $text")
    }

//    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
//        Log.d("WebSocket", "Received bytes: ${bytes.hex()}")
//    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.d("WebSocket", "Closing: $code / $reason")
//        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "Error: ${t.message}")
    }

}