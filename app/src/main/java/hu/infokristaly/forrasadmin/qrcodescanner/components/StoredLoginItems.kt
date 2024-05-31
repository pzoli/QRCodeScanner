package hu.infokristaly.forrasadmin.qrcodescanner.components

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import com.google.gson.Gson

class StoredLoginItems {


    var pin: String? = null
    var clientEMYSZ: String? = null
    var systemUserName: String = ""
    var events = mutableMapOf<Int, String>()
    var eventId = -1
    var eventName: String? = ""

    val KEY_SYSTE_MUSER = "systemUserName"
    var KEY_CLIENT_ENYSZ = "clientEMYSZ"
    val KEY_PIN = "pin"
    val KEY_EVENTS = "events"
    val KEY_EVENT_ID = "eventId"
    val KEY_EVENT_NAME = "eventName"

    fun saveInstanceState(outState: Bundle) {
        outState.putString(KEY_PIN, pin)
        outState.putString(KEY_CLIENT_ENYSZ, clientEMYSZ)
        outState.putString(KEY_SYSTE_MUSER, systemUserName)
        outState.putString(KEY_EVENT_NAME, eventName)
        val extras = Bundle();
        extras.putSerializable(KEY_EVENTS, SerializableEventMap(events));
        outState.putBundle(KEY_EVENTS, extras)
        outState.putInt(KEY_EVENT_ID, eventId)
    }

    fun restoreStateFromBundle(savedInstanceState: Bundle) {
        savedInstanceState.let {
            pin = savedInstanceState.getString(KEY_PIN)
            clientEMYSZ = savedInstanceState.getString(KEY_CLIENT_ENYSZ)
            systemUserName = savedInstanceState.getString(KEY_SYSTE_MUSER) ?: ""
            val extras = savedInstanceState.getBundle(KEY_EVENTS)
            events = (extras?.getSerializable(KEY_EVENTS) as SerializableEventMap).contentMap
            eventId = savedInstanceState.getInt(KEY_EVENT_ID) ?: -1
            eventName = savedInstanceState.getString(KEY_EVENT_NAME)
        }
    }

    fun restoreFromSharedPrefs(sharedPrefs: SharedPreferences) {
        pin = sharedPrefs.getString(KEY_PIN, "")
        clientEMYSZ = sharedPrefs.getString(KEY_CLIENT_ENYSZ, "")
        eventId = sharedPrefs.getInt(KEY_EVENT_ID, -1)
        eventName = sharedPrefs.getString(KEY_EVENT_NAME, "")
        systemUserName = sharedPrefs.getString(KEY_SYSTE_MUSER, "") ?: ""
        val eventsJSON = sharedPrefs.getString(KEY_EVENTS, "") ?: ""
        val gson = Gson()
        try {
            val eventObj = gson.fromJson(eventsJSON, SerializableEventMap::class.java)
            events = eventObj.contentMap
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
        }
    }

    fun saveState(sharedPrefs: SharedPreferences) {
        val serializableEvents = SerializableEventMap(events)
        val gson = Gson()
        val jsonEvents = gson.toJson(serializableEvents)

        sharedPrefs.edit()
            .putString(KEY_PIN, pin)
            .putString(KEY_CLIENT_ENYSZ, clientEMYSZ)
            .putInt(KEY_EVENT_ID, eventId)
            .putString(KEY_EVENT_NAME, eventName)
            .putString(KEY_SYSTE_MUSER, systemUserName)
            .putString(KEY_EVENTS, jsonEvents)
            .apply()
    }



}