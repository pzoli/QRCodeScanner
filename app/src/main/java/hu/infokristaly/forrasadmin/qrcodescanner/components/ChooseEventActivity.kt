package hu.infokristaly.forrasadmin.qrcodescanner.components

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import hu.infokristaly.forrasadmin.qrcodescanner.R
import hu.infokristaly.forrasadmin.qrcodescanner.databinding.ActivityChooseEventBinding

class ChooseEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChooseEventBinding
    private val events = mutableMapOf<Int, String>()
    private var eventId = -1

    val KEY_EVENT_ID = "eventId"

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChooseEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState != null) {
            eventId = savedInstanceState.getInt(KEY_EVENT_ID)
        }

        try {
            val eventsMap = intent.getSerializableExtra("events")
            if (eventsMap != null) {
                events.clear()
                events.putAll((eventsMap as SerializableEventMap).contentMap)
                val eventsList = events.toList()
                val adapter = MyListAdapter(this, eventsList)
                binding.lvEvents.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                binding.lvEvents.adapter = adapter
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message.toString())
        }

        binding.lvEvents.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                eventId = events.keys.toList().get(position)
            }

        binding.btnSelect.setOnClickListener { it ->
            if (eventId != -1) {
                val i = Intent()
                i.putExtra(KEY_EVENT_ID, eventId)
                setResult(RESULT_OK, i)
                finish()
            }
        }

        binding.btnCancel.setOnClickListener {
            val i = Intent()
            setResult(RESULT_CANCELED, i)
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_EVENT_ID,eventId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        eventId = savedInstanceState.getInt(KEY_EVENT_ID)
    }
}