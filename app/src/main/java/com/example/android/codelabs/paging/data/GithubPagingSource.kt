package com.example.android.codelabs.paging.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.paging.LoadType
import androidx.paging.PagingSource
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.api.RepoSearchResponse
import com.example.android.codelabs.paging.model.Repo
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GithubPagingSource (
        private val myBackend: GithubService,
        private val searchTerm: String,
        private val context: Context
) : PagingSource<Int, Repo>() {
    // keep the list of all results received
    private val inMemoryCache = mutableListOf<Repo>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        return when (params.loadType) {
            LoadType.REFRESH -> loadInitial(params)
            LoadType.START -> loadBefore(params)
            LoadType.END -> loadAfter(params)
        }
    }

    private suspend fun loadInitial(params: LoadParams<Int>): LoadResult<Int, Repo> =
            try {
                val initPos = params.key ?: GITHUB_STARTING_PAGE_INDEX

                // suspending network load, executes automatically on worker thread
                val response = myBackend.searchRepos(
                        searchTerm + IN_QUALIFIER, initPos, params.pageSize)

                Log.d("GithubRepository", "Initial response $response")

                loadResults(response, params)
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
            }

    private suspend fun loadAfter(params: LoadParams<Int>): LoadResult<Int, Repo> =
            try {
                val initPos = params.key ?: GITHUB_STARTING_PAGE_INDEX

                // suspending network load, executes automatically on worker thread
                val response = myBackend.searchRepos(
                        searchTerm + IN_QUALIFIER, initPos, params.pageSize)

                Log.d("GithubRepository", "response after $response")

                loadResults(response, params)
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
            }

    private suspend fun loadBefore(params: LoadParams<Int>): LoadResult<Int, Repo> =
            try {
                val initPos = params.key ?: GITHUB_STARTING_PAGE_INDEX

                // suspending network load, executes automatically on worker thread
                val response = myBackend.searchRepos(
                        searchTerm + IN_QUALIFIER, initPos, params.pageSize)

                Log.d("GithubRepository", "response before $response")

                loadResults(response, params)
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
            }



    private fun loadResults(response: Response<RepoSearchResponse>, params: LoadParams<Int>): LoadResult.Page<Int, Repo> {
        return if (response.isSuccessful) {
            val items = response.body()?.items!!
            // keep the list of all results received
            inMemoryCache.clear()
            inMemoryCache.addAll(items)

            Log.d("GithubRepository", "loaded ${items.count()} items")

            val reposByName = reposByName(searchTerm)
            val nextPage = response.body()?.nextPage
            val previousPage = if (params.key ?: 0 >= 2) (params.key!! - 1) else null

            LoadResult.Page(
                    data = reposByName,
                    prevKey = previousPage,
                    nextKey = nextPage
            )
        } else {
            Log.d("GithubRepository", "fail to get data")

            Toast.makeText(
                    context,
                    "\uD83D\uDE28 Wooops ${response.message()}",
                    Toast.LENGTH_LONG
            ).show()

            LoadResult.Page(
                    data = emptyList(),
                    prevKey = 1,
                    nextKey = 1
            )
        }
    }

    private fun reposByName(query: String): List<Repo> {
        // from the in memory cache select only the repos whose name or description matches
        // the query. Then order the results.
        return inMemoryCache.filter {
            it.name.contains(query, true) ||
                    it.description != null && it.description.contains(query, true)
        }.sortedWith(compareByDescending<Repo> { it.stars }.thenBy { it.name })
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 10
        private const val IN_QUALIFIER = " in:name,description"

        // GitHub page API is 1 based: https://developer.github.com/v3/#pagination
        private const val GITHUB_STARTING_PAGE_INDEX = 1
    }
}