package net.xblacky.animexstream.utils.rertofit

import io.reactivex.Observable
import net.xblacky.animexstream.utils.constants.C
import okhttp3.ResponseBody
import retrofit2.http.*

class NetworkInterface {


    interface FetchRecentSubOrDub {

        @GET("https://ajax.gogocdn.net/ajax/page-recent-release.html")
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("page") page: Int,
            @Query("type") type: Int
        ): Observable<ResponseBody>
    }

    interface FetchPopularFromAjax {

        @GET("https://ajax.gogocdn.net/ajax/page-recent-release-ongoing.html")
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchMovies {

        @GET("/anime-movies.html")
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchNewestSeason {

        @GET("/new-season.html")
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchEpisodeMediaUrl {

        @GET
        fun get(
            @HeaderMap header: Map<String, String>,
            @Url url: String
        ): Observable<ResponseBody>

    }

    interface FetchAnimeInfo {
        @GET
        fun get(
            @HeaderMap header: Map<String, String>,
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchM3u8Url {

        @GET
        fun get(
            @HeaderMap header: Map<String, String>,
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchEpisodeList {

        @GET(C.EPISODE_LOAD_URL)
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("ep_start") startEpisode: Int = 0,
            @Query("ep_end") endEpisode: String,
            @Query("id") id: String,
            @Query("default_ep") defaultEp: Int = 0,
            @Query("alias") alias: String
        ): Observable<ResponseBody>
    }

    interface FetchSearchData {

        @GET(C.SEARCH_URL)
        fun get(
            @HeaderMap header: Map<String, String>,
            @Query("keyword") keyword: String,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchM3u8Urlv2 {


        @GET
        fun get(

            @Url url: String,
            @Header("Referer") ref : String  =""
        ): Observable<ResponseBody>
    }

}