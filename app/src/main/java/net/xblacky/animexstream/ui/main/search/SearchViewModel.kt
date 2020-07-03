package net.xblacky.animexstream.ui.main.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import net.xblacky.animexstream.utils.CommonViewModel2
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.AnimeMetaModel
import net.xblacky.animexstream.utils.model.SuggestionModel
import net.xblacky.animexstream.utils.parser.HtmlParser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response

class SearchViewModel : CommonViewModel2(), retrofit2.Callback<SuggestionModel> {

    private val searchRepository = SearchRepository()
    private var _searchList: MutableLiveData<ArrayList<AnimeMetaModel>> = MutableLiveData()
    private var _suggestionsList: MutableLiveData<ArrayList<String>> = MutableLiveData()
    private var pageNumber: Int = 1
    private lateinit var keyword: String
    private var _canNextPageLoaded = true
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var searchList: LiveData<ArrayList<AnimeMetaModel>> = _searchList
    var suggestionsList: LiveData<ArrayList<String>> = _suggestionsList

    fun fetchSuggestionsList(keyword: String) {
        val list = _suggestionsList.value
        list?.clear()
        _suggestionsList.value = list
        searchRepository.fetchSearchSuggestions(keyword).enqueue(this)
    }

    fun fetchSearchList(keyword: String) {
        pageNumber = 1
        this.keyword = keyword
        val list = _searchList.value
        list?.clear()
        _searchList.value = list
        if (!super.isLoading()) {
            compositeDisposable.add(
                searchRepository.fetchSearchList(
                    keyword,
                    pageNumber
                ).subscribeWith(getSearchObserver(C.TYPE_SEARCH_NEW))
            )
            updateLoadingState(loading = Loading.LOADING, e = null, isListEmpty = isListEmpty())
        }
    }

    fun fetchNextPage() {
        if (_canNextPageLoaded && !super.isLoading()) {
            compositeDisposable.add(
                searchRepository.fetchSearchList(
                    keyword,
                    pageNumber
                ).subscribeWith(getSearchObserver(C.TYPE_SEARCH_UPDATE))
            )
            updateLoadingState(loading = Loading.LOADING, e = null, isListEmpty = isListEmpty())
        }
    }

    private fun getSearchObserver(searchType: Int): DisposableObserver<ResponseBody> {
        return object : DisposableObserver<ResponseBody>() {
            override fun onComplete() {
                updateLoadingState(loading = Loading.COMPLETED, e = null, isListEmpty = isListEmpty())
            }

            override fun onNext(response: ResponseBody) {
                val list =
                    HtmlParser.parseMovie(response = response.string(), typeValue = C.TYPE_DEFAULT)
                if (list.isNullOrEmpty() || list.size < 20) {
                    _canNextPageLoaded = false
                }
                if (searchType == C.TYPE_SEARCH_NEW) {
                    _searchList.value = list
                } else if (searchType == C.TYPE_SEARCH_UPDATE) {
                    val updatedList = _searchList.value
                    updatedList?.addAll(list)
                    _searchList.value = updatedList
                }
                pageNumber++
            }

            override fun onError(e: Throwable) {
                updateLoadingState(loading = Loading.ERROR, e = e, isListEmpty = isListEmpty())
            }

        }
    }


    override fun onCleared() {
        if(!compositeDisposable.isDisposed){
            compositeDisposable.dispose()
        }
        super.onCleared()
    }

    private fun isListEmpty(): Boolean{
        return _searchList.value.isNullOrEmpty()
    }

    override fun onFailure(call: Call<SuggestionModel>, t: Throwable) {}

    override fun onResponse(call: Call<SuggestionModel>, response: Response<SuggestionModel>) {
        response.body()?.content?.let {
            val list = HtmlParser.parseSuggestions(it)
            _suggestionsList.value = list
        }
    }

}