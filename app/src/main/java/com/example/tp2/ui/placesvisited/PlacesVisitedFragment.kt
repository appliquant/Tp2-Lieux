package com.example.tp2.ui.placesvisited

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2.R
import com.example.tp2.adapter.PlaceAdapter
import com.example.tp2.databinding.FragmentPlacesVisitedBinding
import com.example.tp2.db.Place
import com.example.tp2.db.PlaceApplication
import com.example.tp2.viewmodel.PlaceViewModel
import com.example.tp2.viewmodel.PlaceViewModelFactory
import kotlinx.coroutines.launch

class PlacesVisitedFragment : Fragment() {
    private var _binding: FragmentPlacesVisitedBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private val placeViewModel: PlaceViewModel by activityViewModels {
        PlaceViewModelFactory(
            (activity?.application as PlaceApplication).database.placeDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentPlacesVisitedBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser recyclerview
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PlaceAdapter(
            onItemClicked = {
                removeVisitedPlace(it)
            }, onItemLongClicked = { place, v ->
                {}
            }
        )
        recyclerView.adapter = adapter
        lifecycle.coroutineScope.launch {
            placeViewModel.getAllVisitedPlaces().collect {
                adapter.submitList(it)
            }
        }

        // Set up data binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            placesVisitedFragment = this@PlacesVisitedFragment
        }

        // Click listener sur la top bar
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                // Naviguer vers fragment Settings
                R.id.app_bar_item_settings -> {
                    navigateToSettingsFragment()
                    true
                }

                else -> false
            }
        }
    }

    /**
     * Retirer le lieu de la liste des lieux visités
     */
    private fun removeVisitedPlace(place: Place) {
        // Mettre à jour le status `is_visited`
        lifecycle.coroutineScope.launch {
            val updatedPlace =
                Place(
                    id = place.id,
                    name = place.name,
                    address = place.address,
                    description = place.description,
                    imagePath = place.imagePath,
                    isVisited = false // <---
                )
            placeViewModel.updatePlace(updatedPlace)
        }

        // Confirmation
        Toast.makeText(context, getString(R.string.removed_place_confirmation), Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Naviguer vers le fragment des paramètres
     */
    private fun navigateToSettingsFragment() {
        findNavController().navigate(R.id.settingsFragment)
    }
}