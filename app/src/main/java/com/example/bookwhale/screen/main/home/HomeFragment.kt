package com.example.bookwhale.screen.main.home

import android.util.Log
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.bookwhale.R
import com.example.bookwhale.databinding.FragmentHomeBinding
import com.example.bookwhale.model.main.home.ArticleModel
import com.example.bookwhale.screen.base.BaseFragment
import com.example.bookwhale.screen.main.MainViewModel
import com.example.bookwhale.util.provider.ResourcesProvider
import com.example.bookwhale.widget.adapter.ModelRecyclerAdapter
import com.example.bookwhale.widget.listener.main.home.ArticleListListener
import org.koin.android.ext.android.inject

class HomeFragment: BaseFragment<MainViewModel, FragmentHomeBinding>() {
    override val viewModel by activityViewModels<MainViewModel>()

    override fun getViewBinding(): FragmentHomeBinding = FragmentHomeBinding.inflate(layoutInflater)

    private val resourcesProvider by inject<ResourcesProvider>()

    private val adapter by lazy {
        ModelRecyclerAdapter<ArticleModel, MainViewModel>(
            listOf(),
            viewModel,
            resourcesProvider,
            adapterListener = object : ArticleListListener {
                override fun onClickItem(model: ArticleModel) {
                }
            }
        )
    }

    override fun initViews(): Unit = with(binding) {
        recyclerView.adapter = adapter

        viewModel.getArticles(null,PAGE,SIZE)
    }

    override fun observeData() {
        viewModel.homeArticleStateLiveData.observe(this) {
            when(it) {
                is HomeState.Loading -> handleLoading()
                is HomeState.Success -> handleSuccess(it)
                is HomeState.Error -> handleError(it)
                else -> Unit
            }
        }
    }

    private fun handleLoading() {
        binding.progressBar.isVisible = true
    }

    private fun handleSuccess(state: HomeState.Success) {
        binding.progressBar.isGone = true
        adapter.submitList(state.articles)
        if(state.articles.isEmpty()) binding.noArticleTextView.isVisible = true
    }

    private fun handleError(state: HomeState.Error) {
        binding.progressBar.isGone = true
        Toast.makeText(requireContext(), R.string.error_noArticles, Toast.LENGTH_SHORT).show()
    }

    companion object {

        fun newInstance() = HomeFragment()

        const val TAG = "HomeFragment"
        const val PAGE = 0
        const val SIZE = 10
    }
}