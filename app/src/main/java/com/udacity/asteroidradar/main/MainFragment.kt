package com.udacity.asteroidradar.main

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.domain.ImageOfTheDay

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after the activity or fragment has been created."
        }
        ViewModelProvider(this, MainViewModel.Factory(activity.application)).get(MainViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setHasOptionsMenu(true)

        binding.asteroidRecycler.layoutManager = LinearLayoutManager(requireActivity())
        val adapter = AsteroidListAdapter(object : AsteroidListAsteroidClickListener {
            override fun onClick(asteroid: Asteroid) {
                viewModel.setNavigateToDetailScreenAsteroid(asteroid)
            }
        })
        binding.asteroidRecycler.adapter = adapter

        viewModel.navigateToDetailScreenAsteroid.observe(viewLifecycleOwner, Observer { asteroid: Asteroid? ->
            asteroid?.let {
                findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
                viewModel.onNavigatedToDetailScreen()
            }
        })

        viewModel.imageOfTheDay.observe(viewLifecycleOwner, Observer { imageOfTheDay: ImageOfTheDay ->
            imageOfTheDay?.let {
                adapter.updateHeader(imageOfTheDay)
            }
        })

        viewModel.asteroids.observe(viewLifecycleOwner, Observer { asteroids : List<Asteroid> ->
            asteroids?.let {
                adapter.addHeaderAndSubmitList(viewModel.imageOfTheDay.value, asteroids)
                adapter.notifyDataSetChanged()
                if (asteroids.isNotEmpty()) {
                    viewModel.setAsteroidsHaveBeenLoaded(true)
                }
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_all_menu -> viewModel.loadAsteroidsForNextSevenDays()
            R.id.show_today_menu -> viewModel.loadAsteroidsForToday()
        }
        return true
    }
}
