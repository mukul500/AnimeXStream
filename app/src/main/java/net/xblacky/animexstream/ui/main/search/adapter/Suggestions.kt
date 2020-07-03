package net.xblacky.animexstream.ui.main.search.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import net.xblacky.animexstream.ui.main.search.SearchRepository
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.parser.HtmlParser
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class Suggestions(context: Context, resource: Int) : ArrayAdapter<String>(context, resource), Filterable{

    private var dataList: List<*> = emptyList<String>()

    private val listFilter = ListFilter(this)


    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): String {
        return  (dataList[position] as? String ?: "")
    }

    override fun getFilter(): Filter {
        return listFilter
    }

     class ListFilter(var suggestions: Suggestions) : Filter() {
         private val lock = Any()
         private val searchRepository = SearchRepository()

         override fun performFiltering(prefix: CharSequence?): FilterResults {
            val results = FilterResults()
            if (prefix == null || prefix.isEmpty()) {
                synchronized(lock) {
                    results.values = ArrayList<String>()
                    results.count = 0
                }
            } else {
                val searchStrLowerCase = prefix.toString().toLowerCase(Locale.ROOT)

                val url = URL("${C.SUGGESTION_URL}?keyword=$searchStrLowerCase")
                val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
                urlConnection.addRequestProperty("x-requested-with", "XMLHttpRequest")

                try {
                    val inp: InputStream = BufferedInputStream(urlConnection.inputStream)
                    val list = HtmlParser.parseSuggestions(String(inp.readBytes()))
                    results.values = list
                    results.count = list.size
                } finally {
                    urlConnection.disconnect()
                }
            }
            return results
        }

         override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {
            if (results.values != null) {
                this.suggestions.dataList = results.values as List<*>
            } else {
                this.suggestions.dataList = emptyList<String>()
            }
            if (results.count > 0) {
                this.suggestions.notifyDataSetChanged()
            } else {
                this.suggestions.notifyDataSetInvalidated()
            }
        }
    }
}