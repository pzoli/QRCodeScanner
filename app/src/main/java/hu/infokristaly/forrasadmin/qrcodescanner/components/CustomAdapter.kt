package hu.infokristaly.forrasadmin.qrcodescanner.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class MyListAdapter(private val context: Context, private val data: List<Pair<Int, String>>) : BaseAdapter() {
    override fun getCount(): Int = data.size

    override fun getItem(position: Int): Pair<Int, String> = data[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_single_choice, parent, false)
        val (key, value) = data[position]
        (view as TextView).text = "$value"
        return view
    }
}