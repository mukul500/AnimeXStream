package net.xblacky.animexstream.ui.main.search

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import net.xblacky.animexstream.utils.connectivity.base.ConnectivityProvider
import kotlinx.android.synthetic.main.fragment_search.view.*
import kotlinx.android.synthetic.main.loading.view.*
import net.xblacky.animexstream.R
import net.xblacky.animexstream.ui.main.search.adapter.SuggestionsAdapter
import net.xblacky.animexstream.ui.main.search.epoxy.SearchController
import net.xblacky.animexstream.utils.CommonViewModel2
import net.xblacky.animexstream.utils.ItemOffsetDecoration
import net.xblacky.animexstream.utils.Utils
import net.xblacky.animexstream.utils.model.AnimeMetaModel


class SearchFragment : Fragment(), View.OnClickListener,
    SuggestionsAdapter.SuggestionsFilter.SuggestionAdapterCallbacks,
    SearchController.EpoxySearchAdapterCallbacks, ConnectivityProvider.ConnectivityStateListener {

    private val provider: ConnectivityProvider by lazy { ConnectivityProvider.createProvider(this.requireContext()) }

    private lateinit var rootView: View
    private lateinit var viewModel: SearchViewModel
    private lateinit var searchController: SearchController
    private lateinit var suggestionsAdapter: SuggestionsAdapter

    private var isNetworkAvailable = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_search, container, false)
        setOnClickListeners()
        setAdapters()
        setRecyclerViewScroll()
        configureEditText()
        setEditTextListener()
        return rootView
    }

    override fun onStart() {
        super.onStart()
        provider.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        provider.removeListener(this)
    }

    override fun onStateChange(state: ConnectivityProvider.NetworkState) {
        isNetworkAvailable = state.hasInternet()
    }

    private fun ConnectivityProvider.NetworkState.hasInternet(): Boolean {
        return (this as? ConnectivityProvider.NetworkState.ConnectedState)?.hasInternet == true
    }

    private fun configureEditText() {
        suggestionsAdapter = SuggestionsAdapter(rootView.context, android.R.layout.simple_list_item_1, this)
        rootView.searchEditText.setAdapter(suggestionsAdapter)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        setObserver()
    }

    private fun search(){
        hideKeyBoard()
        rootView.searchEditText.clearFocus()
        viewModel.fetchSearchList(rootView.searchEditText.text.toString().trim())
    }

    private fun setEditTextListener() {
        rootView.searchEditTextClear.setOnClickListener {
            rootView.searchEditText.editableText.clear()
        }
        rootView.searchEditText.setOnItemClickListener { parent, view, position, id ->
            search()
        }
        rootView.searchEditText.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || event.action == KeyEvent.ACTION_DOWN) {
                search()
                return@OnEditorActionListener true
            }
            false
        })
    }

    private fun setEditTextFocus() {
        rootView.searchEditText.requestFocus()
    }

    private fun setOnClickListeners() {
        rootView.backButton.setOnClickListener(this)
    }

    private fun setAdapters() {
        searchController = SearchController(this)
        searchController.spanCount = Utils.calculateNoOfColumns(context!!, 150f)
        rootView.searchRecyclerView.apply {
            layoutManager = GridLayoutManager(context, Utils.calculateNoOfColumns(context!!, 150f))
            adapter = searchController.adapter
            (layoutManager as GridLayoutManager).spanSizeLookup = searchController.spanSizeLookup
        }
        rootView.searchRecyclerView.addItemDecoration(
            ItemOffsetDecoration(
                context,
                R.dimen.episode_offset_left
            )
        )

    }

    private fun setObserver() {
        viewModel.suggestionsList.observe(viewLifecycleOwner, Observer {
            suggestionsAdapter.setResults(it)
        })

        viewModel.loadingModel.observe(viewLifecycleOwner, Observer {
            if(it == null){
                setEditTextFocus()
                showKeyBoard()
            }else{
                when(it.loading){
                    CommonViewModel2.Loading.COMPLETED -> {
                        rootView.loading.visibility = View.GONE
                        if(it.isListEmpty){
                            setEditTextFocus()
                            showKeyBoard()
                        }else{
                            hideKeyBoard()
                        }
                    }
                    CommonViewModel2.Loading.ERROR -> {
                        rootView.loading.visibility = View.GONE
                        Snackbar.make(
                            rootView,
                            getString(it.errorMsg),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                    CommonViewModel2.Loading.LOADING -> {
                        if(it.isListEmpty) rootView.loading.visibility = View.VISIBLE
                    }

                }
                searchController.setData(
                    viewModel.searchList.value,
                    it.loading == CommonViewModel2.Loading.LOADING
                )
            }
        })

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.backButton -> {
                hideKeyBoard()
                findNavController().popBackStack()

            }
        }
    }

    private fun setRecyclerViewScroll() {
        rootView.searchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManger = rootView.searchRecyclerView.layoutManager as GridLayoutManager
                val visibleItemCount = layoutManger.childCount
                val totalItemCount = layoutManger.itemCount
                val firstVisibleItemPosition = layoutManger.findFirstVisibleItemPosition()

                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0
                ) {
                    if (isNetworkAvailable) {
                        viewModel.fetchNextPage()
                    } else {
                        Snackbar.make(
                            view!!,
                            getString(R.string.no_internet),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun hideKeyBoard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity?.currentFocus?.windowToken, 0)
    }

    private fun showKeyBoard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(activity?.currentFocus, 0)
    }

    override fun animeTitleClick(model: AnimeMetaModel) {
        findNavController().navigate(
            SearchFragmentDirections.actionSearchFragmentToAnimeInfoFragment(
                categoryUrl = model.categoryUrl
            )
        )
    }

    override fun findSuggestions(hint: String, newQuery: Boolean) {
        if(newQuery) viewModel.fetchSuggestionsList(hint)
        else suggestionsAdapter.notifyDataSetChanged()
    }
}