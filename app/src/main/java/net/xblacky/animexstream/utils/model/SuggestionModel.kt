package net.xblacky.animexstream.utils.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SuggestionModel {
    @SerializedName("content")
    @Expose
    var content: String? = null
}