package hu.infokristaly.forrasadmin.qrcodescanner.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.infokristaly.forrasadmin.qrcodescanner.R
import hu.infokristaly.forrasadmin.qrcodescanner.databinding.ActivityLoginBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL


class LoginActivity : AppCompatActivity() {
    private var registerClientToEventResult: String = ""
    private lateinit var binding: ActivityLoginBinding
    private var qrcode: String? = null
    private val storedLoginItems = StoredLoginItems()
    private var serverAddress = ""
    private var toolbar: Toolbar? = null

    val activitySettingsLauncher =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            serverAddress = getServerAddress()
        }
                val activityLoginWithQRCodeLauncher =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (Activity.RESULT_OK != result?.resultCode) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                qrcode = result.data?.getStringExtra("qrcode")
                if (qrcode != null) {
                    val parts = qrcode?.split("\t")
                    if (!parts.isNullOrEmpty() && parts.size == 2) {
                        val pinContent = parts[0].split(":")
                        if (pinContent.size == 2 && pinContent[0] == "PIN") {
                            storedLoginItems.pin = pinContent[1]
                            val nameContent = parts[1].split(":");
                            if (nameContent.size == 2 && nameContent[0] == "NAME") {
                                storedLoginItems.systemUserName = nameContent[1]
                                updateView()
                            }
                            downloadEvents()
                        }
                    }
                }

                Toast.makeText(this, "Scanned: $qrcode", Toast.LENGTH_LONG)
                    .show()

            }
        }

    val activityChooseEventLauncher =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (Activity.RESULT_OK != result?.resultCode) {
                Toast.makeText(this, "Event cancelled", Toast.LENGTH_LONG).show()
            } else {
                storedLoginItems.eventId = result.data?.extras?.getInt("eventId")?.toInt() ?: -1
                Log.i("DEBUG", storedLoginItems.eventId.toString())
                storedLoginItems.eventName =
                    storedLoginItems.events.get(storedLoginItems.eventId).toString()
                updateView()
            }
        }

    val activityAddClientWithQRCodeLauncher =
        registerForActivityResult<Intent, ActivityResult>(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (Activity.RESULT_OK != result?.resultCode) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_LONG).show()
            } else {
                qrcode = result.data?.getStringExtra("qrcode")
                if (qrcode != null) {
                    val parts = qrcode?.split("\t")
                    if (!parts.isNullOrEmpty() && parts.size == 2) {
                        val enyszContent = parts[0].split(":")
                        if (enyszContent.size == 2 && enyszContent[0] == "ENYSZ") {
                            storedLoginItems.clientEMYSZ = enyszContent[1]
                            val nameContent = parts[1].split(":");
                            if (nameContent.size == 2 && nameContent[0] == "NAME") {
                                registerClientToEvent()
                            }
                        }
                    }
                }

                Toast.makeText(
                    this,
                    "Scanned: $qrcode \nEredmény: $registerClientToEventResult",
                    Toast.LENGTH_LONG
                )
                    .show()
            }
        }

    private fun registerClientToEvent() {
        runBlocking {
            var result: Deferred<String> = async() {
                withContext(Dispatchers.IO) {
                    val result =
                        addClientToEventApiRequest(URL("http://$serverAddress/forras-admin/rest/addClientToEvent?pin=${storedLoginItems.pin}&eventid=${storedLoginItems.eventId}&clientid=${storedLoginItems.clientEMYSZ}"))
                    return@withContext result
                }
            }
            registerClientToEventResult = result.await()
        }
    }

    private fun addClientToEventApiRequest(url: URL): String {
        var result = ""
        try {
            val conn = url.openConnection() as HttpURLConnection
            with(conn) {
                requestMethod = "GET"
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            result += line
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
        return result;
    }

    private fun downloadEvents() {
        runBlocking {
            var result: Deferred<MutableMap<Int, String>> = async() {
                withContext(Dispatchers.IO) {
                    val result =
                        getEventsApiRequest(URL("http://$serverAddress/forras-admin/rest/getTodayEventsForUser?pin=${storedLoginItems.pin}"))
                    return@withContext result
                }
            }
            storedLoginItems.events = result.await()
        }
        val intent = Intent(this, ChooseEventActivity::class.java)
        intent.putExtra("events", SerializableEventMap(storedLoginItems.events))
        activityChooseEventLauncher.launch(intent)
    }

    fun getEventsApiRequest(url: URL): MutableMap<Int, String> {
        val result = mutableMapOf<Int, String>()

        try {
            val conn = url.openConnection() as HttpURLConnection
            with(conn) {
                requestMethod = "GET"
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream.bufferedReader().use {
                        it.lines().forEach { line ->
                            val eventContent = line.split("\t")
                            try {
                                result.put(eventContent[0].toInt(), eventContent[1]);
                            } catch (e:Exception) {
                                Log.e("ERROR", e.message.toString())
                            }
                        }
                    }
                } else {
                    Log.e("ERROR", responseCode.toString() +":"+ responseMessage)
                }
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
        return result
    }

    private fun updateView() {
        binding.tvChoosedEvent.text = storedLoginItems.eventName
        binding.tvLoggedInUser.text = storedLoginItems.systemUserName
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        storedLoginItems.saveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        storedLoginItems.restoreStateFromBundle(savedInstanceState)
        serverAddress = getServerAddress()
        updateView()
    }

    private fun getServerAddress(): String {
        val prefFile = "${packageName}_preferences"
        val sharedPreferences = getSharedPreferences(prefFile , Context.MODE_PRIVATE)
        val result = sharedPreferences.getString("serveraddress", "")?:""
        return result
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState != null) {
            storedLoginItems.restoreStateFromBundle(savedInstanceState)
        } else {
            val sharedPrefs = getSharedPreferences("my_activity_prefs", Context.MODE_PRIVATE)
            storedLoginItems.restoreFromSharedPrefs(sharedPrefs)
        }
        updateView()

        toolbar = findViewById(R.id.mytoolbar)
        setSupportActionBar(toolbar)
        serverAddress = getServerAddress()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.appbar,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.m_login -> {
                val intent = Intent(this, QRCodeScannerActivity::class.java)
                activityLoginWithQRCodeLauncher.launch(intent)
            }
            R.id.m_choose_session -> {
                val intent = Intent(this, ChooseEventActivity::class.java)
                intent.putExtra("events", SerializableEventMap(storedLoginItems.events))
                activityChooseEventLauncher.launch(intent)
            }
            R.id.m_add_client_to_session -> {
                val intent = Intent(this, QRCodeScannerActivity::class.java)
                activityAddClientWithQRCodeLauncher.launch(intent)
            }
            R.id.m_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                activitySettingsLauncher.launch(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        val sharedPrefs = getSharedPreferences("my_activity_prefs", Context.MODE_PRIVATE)
        storedLoginItems.saveState(sharedPrefs)
    }

    override fun onStop() {
        super.onStop()
        val sharedPrefs = getSharedPreferences("my_activity_prefs", Context.MODE_PRIVATE)
        storedLoginItems.saveState(sharedPrefs)
    }

}