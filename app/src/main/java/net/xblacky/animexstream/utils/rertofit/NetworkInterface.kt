package net.xblacky.animexstream.utils.rertofit

import io.reactivex.Observable
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.SuggestionModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Url

class NetworkInterface {

    //TODO To add Header for undectability

    interface FetchRecentSubOrDub {
        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET("https://ajax.gogocdn.net/ajax/page-recent-release.html")
        fun get(
            @Query("page") page: Int,
            @Query("type") type: Int
        ): Observable<ResponseBody>
    }

    interface FetchPopularFromAjax {

        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET("https://ajax.gogocdn.net/ajax/page-recent-release-ongoing.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchMovies {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET("/anime-movies.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchNewestSeason {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )

        @GET("/new-season.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchEpisodeMediaUrl {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>

    }

    interface FetchAnimeInfo {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchM3u8Url {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchEpisodeList{
        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET(C.EPISODE_LOAD_URL)
        fun get(
            @Query("ep_start") startEpisode: Int = 0,
            @Query("ep_end") endEpisode: String,
            @Query("id") id: String,
            @Query("default_ep") defaultEp: Int = 0,
            @Query("alias") alias: String
        ): Observable<ResponseBody>
    }

    interface FetchSearchData{
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET(C.SEARCH_URL)
        fun get(
            @Query("keyword") keyword: String,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchSearchSuggestionData{
        @Headers(
            C.XML_HTTP_REQUEST
        )
        @GET(C.SUGGESTION_URL)
        fun get(
            @Query("keyword") keyword: String
        ): Call<SuggestionModel>
    }
}