package com.example.tp2.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.example.tp2.R
import com.example.tp2.databinding.FragmentHomeBinding
import com.example.tp2.databinding.FragmentSettingsBinding
import com.example.tp2.db.Place
import com.example.tp2.db.PlaceApplication
import com.example.tp2.viewmodel.PlaceViewModel
import com.example.tp2.viewmodel.PlaceViewModelFactory
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val placeViewModel: PlaceViewModel by activityViewModels {
        PlaceViewModelFactory(
            (activity?.application as PlaceApplication).database.placeDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val fragmentBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up data binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            settingsFragment = this@SettingsFragment
            viewModel = placeViewModel
        }

        // Click listener switch
        binding.isadminSwitch?.setOnCheckedChangeListener { buttonView, isChecked ->
            placeViewModel.setAdmin(isChecked)
        }
    }

    /**
     * Supprimer tous les elements dans la bd
     */
    fun deleteAllPlaces() {
        lifecycle.coroutineScope.launch {
            placeViewModel.deleteAllPlaces()
        }

        // Message de confirmation
        Toast.makeText(
            context,
            getString(R.string.settings_delete_all_places_confirmation),
            Toast.LENGTH_SHORT
        ).show()
    }

    /**
     * Retourner à la page d'accueil
     */
    fun navigateToHomeFragment() {
//        findNavController().navigate(R.id.action_settingsFragment_to_navigation_home)
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}