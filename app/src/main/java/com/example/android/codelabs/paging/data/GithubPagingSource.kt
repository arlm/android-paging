package com.example.android.codelabs.paging.data

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.paging.PagingSource
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.model.Repo
import retrofit2.HttpException
import java.io.IOException

class GithubPagingSource (
        private val myBackend: GithubService,
        private val searchTerm: String,
        private val context: Context
) : PagingSource<Int, Repo>() {
    // keep the list of all results received
    private val inMemoryCache = mutableListOf<Repo>()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> =
            try {
                // suspending network load, executes automatically on worker thread
                val response = myBackend.searchRepos(searchTerm + IN_QUALIFIER, GITHUB_STARTING_PAGE_INDEX, NETWORK_PAGE_SIZE)
                Log.d("GithubRepository", "response $response")

                if (response.isSuccessful) {
                    // keep the list of all results received
                    inMemoryCache.clear()
                    inMemoryCache.addAll(response.body()?.items!!)

                    val reposByName = reposByName(searchTerm)
                    val nextPage = response.body()?.nextPage ?: GITHUB_STARTING_PAGE_INDEX
                    val lastPage = if (nextPage <= 2) GITHUB_STARTING_PAGE_INDEX else nextPage - 2

                    LoadResult.Page(
                            data = reposByName,
                            prevKey = null,
                            nextKey = null
                    )
                } else  {
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
            } catch (e: IOException) {
                LoadResult.Error(e)
            } catch (e: HttpException) {
                LoadResult.Error(e)
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
        const val NETWORK_PAGE_SIZE = 50
        private const val IN_QUALIFIER = " in:name,description"

        // GitHub page API is 1 based: https://developer.github.com/v3/#pagination
        private const val GITHUB_STARTING_PAGE_INDEX = 1
    }
}