package com.example.android.codelabs.paging.ui

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.android.codelabs.paging.model.Repo

class ReposPagingAdapter : PagingDataAdapter<Repo, RepoViewHolder>(USER_COMPARATOR) {
    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): RepoViewHolder {
        return RepoViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repoItem = getItem(position)
        // Note that item may be null if placeholders aren't disabled,
        // so our ViewHolder supports binding to null
        holder.bind(repoItem)
    }

    companion object {
        val USER_COMPARATOR = object : DiffUtil.ItemCallback<Repo>() {
            override fun areItemsTheSame(oldItem: Repo, newItem: Repo) =
                    // User ID serves as unique ID
                    oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Repo, newItem: Repo) =
            // Compare full contents
                    // (note: Java users should call .equals()!)
                    oldItem == newItem
        }
    }
}
