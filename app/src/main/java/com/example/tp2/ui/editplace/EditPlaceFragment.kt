package com.example.tp2.ui.editplace

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import com.example.tp2.R
import com.example.tp2.databinding.FragmentEditPlaceBinding
import com.example.tp2.db.Place
import com.example.tp2.db.PlaceApplication
import com.example.tp2.viewmodel.PlaceViewModel
import com.example.tp2.viewmodel.PlaceViewModelFactory
import kotlinx.coroutines.launch

class EditPlaceFragment : Fragment() {
    private var _binding: FragmentEditPlaceBinding? = null
    private val binding get() = _binding!!
    private val placeViewModel: PlaceViewModel by activityViewModels {
        PlaceViewModelFactory(
            (activity?.application as PlaceApplication).database.placeDao()
        )
    }
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var tempPhotoUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        val fragmentBinding = FragmentEditPlaceBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up data binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            editPlaceFragment = this@EditPlaceFragment
        }


        // Initialiser variables
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            savePhotoTemporarly(uri)
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

        loadFields()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Remplir les champs
     */
    fun loadFields() {
        val place = placeViewModel.currentEditPlace.value ?: return navigateBack()

        binding.txtInpName.setText(place.name)
        binding.txtInpAddress.setText(place.address)
        binding.txtInpDescription.setText(place.description)
    }

    /**
     * Éditer le lieu
     */
    fun editPlace() {
        val name = binding.txtInpName.text.toString()
        val address = binding.txtInpAddress.text.toString()
        val description = binding.txtInpDescription.text.toString()

        val isValid = validateFields(name, address, description)
        if (!isValid) {
            Toast.makeText(
                context,
                getString(R.string.bottom_sheet_validate_fields),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Déterminer si il y a une nouvelle photo
        val isNewPhoto =
            tempPhotoUri == null // True = pas de nouvelle photo, False = nouvelle photo
        val oldPlace = placeViewModel.currentEditPlace

        // Éditer le nouveau lieu
        if (isNewPhoto) {
            // Pas de nouvelle photo
            lifecycle.coroutineScope.launch {
                val editedPlace = Place(
                    oldPlace.value!!.id,
                    name,
                    address,
                    description,
                    oldPlace.value!!.imagePath,
                    oldPlace.value!!.isVisited
                )

                placeViewModel.updatePlace(editedPlace)
            }

        } else {
            // Nouvelle photo
            lifecycle.coroutineScope.launch {
                val editedPlace = Place(
                    oldPlace.value!!.id,
                    name,
                    address,
                    description,
                    tempPhotoUri.toString(),
                    oldPlace.value!!.isVisited
                )

                placeViewModel.updatePlace(editedPlace)
            }
        }

        // Confirmation
        Toast.makeText(
            context,
            getString(R.string.edited_place_confirmation),
            Toast.LENGTH_SHORT
        ).show()

        navigateBack()
    }

    /**
     * Valider les champs
     * @return True si valide
     * Ne valide pas si une nouvelle photo est utilisé
     */
    fun validateFields(
        name: String,
        address: String,
        description: String,
    ): Boolean {
        if (name.isEmpty() || address.isEmpty() || description.isEmpty()) {
            return false
        }

        return true
    }

    /**
     * Choisir une photo de la galerie de photos
     * https://developer.android.com/training/data-storage/shared/photopicker
     */
    fun choosePhoto() {
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    /**
     * Sauvegarder la photo séléctionnée temporairement
     */
    private fun savePhotoTemporarly(uri: Uri?) {
        if (uri != null) {
            val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context?.contentResolver?.takePersistableUriPermission(uri, flag)

            // Sauvegarder uri de la photo temporairement
            tempPhotoUri = uri
        } else {
            Toast.makeText(context, getString(R.string.photopicker_unsupported), Toast.LENGTH_SHORT)
                .show()
        }
    }


    /**
     * Naviguer vers le fragment des paramètres
     */
    private fun navigateToSettingsFragment() {
        findNavController().navigate(R.id.settingsFragment)
    }

    /**
     * Retourner au menu principal
     */
    fun navigateBack() {
        findNavController().navigateUp()
    }
}