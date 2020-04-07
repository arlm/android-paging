/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.android.codelabs.paging.api.GithubPagingSource
import com.example.android.codelabs.paging.api.GithubService
import com.example.android.codelabs.paging.model.Repo

/**
 * ViewModel for the [SearchRepositoriesActivity] screen.
 * The ViewModel works with the [GithubRepository] to get the data.
 */
class SearchRepositoriesViewModel(
        private val context: Context
) : ViewModel() {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private lateinit var queryString: String
    private val queryLiveData = MutableLiveData<String>()

    /**
     * Search a repository based on a query string.
     */
    fun searchRepo(queryString: String) {
        livePagingData = LivePagingData(
                PagingConfig(GithubPagingSource.NETWORK_PAGE_SIZE)
        ) { GithubPagingSource(GithubService.create(), queryString, context) }
                .cachedIn(viewModelScope)
    }

    fun listScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {

            if (livePagingData?.value != null) {
                //repository.requestMore(immutableQuery)
            }
        }
    }

    var livePagingData : LiveData<PagingData<Repo>>? = null

    /**
     * Get the last query value.
     */
    fun lastQueryValue(): String? = queryLiveData.value
}
