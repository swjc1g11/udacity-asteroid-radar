package com.udacity.asteroidradar.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.udacity.asteroidradar.databinding.AsteroidImageOfTheDayHeaderBinding
import com.udacity.asteroidradar.databinding.AsteroidListItemBinding
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.domain.ImageOfTheDay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val IMAGE_OF_THE_DAY = 0
const val ASTEROID_ITEM = 1

interface AsteroidListAsteroidClickListener {
    fun onClick(asteroid: Asteroid)
}

class AsteroidListAdapter(val asteroidClickListener: AsteroidListAsteroidClickListener) : ListAdapter<AsteroidListDataItem, RecyclerView.ViewHolder>(AsteroidDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(imageOfTheDay: ImageOfTheDay?, list: List<Asteroid>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(AsteroidListDataItem.ImageOfTheDayHeaderItem(imageOfTheDay))
                else -> listOf(AsteroidListDataItem.ImageOfTheDayHeaderItem(imageOfTheDay)) + list.map {
                    AsteroidListDataItem.AsteroidItem(it)
                }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    fun updateHeader(imageOfTheDay: ImageOfTheDay) {
        if (!currentList.isEmpty()) {
            val item = getItem(0)
            if (item is AsteroidListDataItem.ImageOfTheDayHeaderItem) {
                item.imageOfTheDay = imageOfTheDay
                notifyDataSetChanged()
            }
        }
    }

    class AsteroidDiffCallback : DiffUtil.ItemCallback<AsteroidListDataItem>() {
        override fun areItemsTheSame(oldItem: AsteroidListDataItem, newItem: AsteroidListDataItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AsteroidListDataItem, newItem: AsteroidListDataItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            IMAGE_OF_THE_DAY -> ImageOfTheDayViewHolder.from(parent)
            ASTEROID_ITEM -> AsteroidViewHolder.from(parent, asteroidClickListener)
            else -> throw ClassCastException("Unknown viewType ${viewType}")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AsteroidListDataItem.ImageOfTheDayHeaderItem -> IMAGE_OF_THE_DAY
            is AsteroidListDataItem.AsteroidItem -> ASTEROID_ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ImageOfTheDayViewHolder -> holder.bind((getItem(position) as AsteroidListDataItem.ImageOfTheDayHeaderItem).imageOfTheDay)
            is AsteroidViewHolder -> holder.bind((getItem(position) as AsteroidListDataItem.AsteroidItem).asteroid)
        }
    }

    class ImageOfTheDayViewHolder private constructor(val binding: AsteroidImageOfTheDayHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ImageOfTheDay?) {
            binding.imageOfTheDay = item
        }

        companion object {
            fun from(parent: ViewGroup) : ImageOfTheDayViewHolder {
                val binding = AsteroidImageOfTheDayHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ImageOfTheDayViewHolder(binding)
            }
        }
    }

    class AsteroidViewHolder private constructor(val binding: AsteroidListItemBinding, val clickListener: AsteroidListAsteroidClickListener) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Asteroid) {
            binding.asteroid = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, asteroidClickListener: AsteroidListAsteroidClickListener) : AsteroidViewHolder {
                val binding = AsteroidListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return AsteroidViewHolder(binding, asteroidClickListener)
            }
        }

    }
}

sealed class AsteroidListDataItem {
    data class AsteroidItem(val asteroid: Asteroid) : AsteroidListDataItem() {
        override val id = asteroid.id
    }
    data class ImageOfTheDayHeaderItem(var imageOfTheDay: ImageOfTheDay?) : AsteroidListDataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}