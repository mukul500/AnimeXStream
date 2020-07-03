package net.xblacky.animexstream.ui.main.search.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable


class SuggestionsAdapter(context: Context, resource: Int, suggestionsCallback: SuggestionsFilter.SuggestionAdapterCallbacks) : ArrayAdapter<String>(context, resource), Filterable{

    private var dataList: List<*> = emptyList<String>()

    private val listFilter = SuggestionsFilter(suggestionsCallback)


    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): String {
        return  (dataList[position] as? String ?: "")
    }

    override fun getFilter(): Filter {
        return listFilter
    }

    fun setResults(result: List<String>?) {
        result?.let {
            dataList = it
            notifyDataSetChanged()
        }
    }

    class SuggestionsFilter(private var suggestionsCallback: SuggestionAdapterCallbacks) : Filter() {

        private var lock: CharSequence = ""

         interface SuggestionAdapterCallbacks {
             fun findSuggestions(hint: String, newQuery: Boolean)
         }

         override fun performFiltering(prefix: CharSequence?): FilterResults {
            return FilterResults()
        }

         override fun publishResults(
            constraint: CharSequence?,
            results: FilterResults
        ) {
             constraint?.trim()?.let { hint ->
                 if(hint != ""){
                     suggestionsCallback.findSuggestions(hint.toString(),  lock != hint)
                     lock = hint
                }
             }
         }
    }
}