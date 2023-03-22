package com.example.tp2.ui.home

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2.R
import com.example.tp2.adapter.PlaceAdapter
import com.example.tp2.databinding.FragmentHomeBinding
import com.example.tp2.db.Place
import com.example.tp2.db.PlaceApplication
import com.example.tp2.viewmodel.PlaceViewModel
import com.example.tp2.viewmodel.PlaceViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private val placeViewModel: PlaceViewModel by activityViewModels {
        PlaceViewModelFactory(
            (activity?.application as PlaceApplication).database.placeDao()
        )
    }
    private lateinit var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var tempPhotoUri: Uri? = null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var dbAlreadyPrepopulated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        _binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialiser recycler view
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = PlaceAdapter(
            onItemClicked = { addToVisitedPlaces(it) },
            onItemLongClicked = { place: Place, v: View ->
                // Vérifier si le monde administrateur est activé
                if (placeViewModel.isAdmin.value == true) {
                    openPopupMenu(place, v)
                }
            }
        )
        recyclerView.adapter = adapter
        lifecycle.coroutineScope.launch {
//            prePopulateDb()

            placeViewModel.getAllPlaces().collect {
                adapter.submitList(it)
            }
        }

        // Initialiser autres variables
        val bottomSheet = view.findViewById<View>(R.id.bottom_sheet_add_places)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            savePhotoTemporarly(uri)
        }

        // Set up data binding
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            homeFragment = this@HomeFragment
        }

        // Click listener sur la top bar
        binding.topAppBar.setOnMenuItemClickListener {
            when (it.itemId) {
                // Naviguer vers fragment Settings
                R.id.app_bar_item_settings -> {
                    navigateToSettingsFragment()
                    true
                }

                // Afficher la Bottom Sheet ajout d'un lieu
                R.id.app_bar_item_add_place -> {
                    openBottomSheet()
                    true
                }
                else -> false
            }
        }

        // Click listener bouton `Choisir image` de bottom sheet
        view.findViewById<Button>(R.id.btnChooseImage).setOnClickListener {
            choosePhoto()
        }

        // Click listener bouton `Ajouter` de bottom Sheet
        view.findViewById<View>(R.id.btnConfirmAddPlace).setOnClickListener {
            addNewPlace(view)
        }
    }

    private fun prePopulateDb() {
        if (dbAlreadyPrepopulated == false) {
            lifecycle.coroutineScope.launch {
                for (i in 1..3) {
                    val uri = Uri.Builder()
                        .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                        .authority(resources.getResourcePackageName(R.drawable.car1))
                        .appendPath(resources.getResourceTypeName(R.drawable.car1))
                        .appendPath(resources.getResourceEntryName(R.drawable.car1))
                        .build()
                        .toString()

                    val newPlace = Place(
                        i,
                        "Lieu ${i}",
                        "Adresse ${i}",
                        "Description ${i}",
                        uri,
                        false
                    )
                    placeViewModel.insertPlace(newPlace)
                }
            }

            dbAlreadyPrepopulated = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Choisir une photo de la galerie de photos
     * https://developer.android.com/training/data-storage/shared/photopicker
     */
    private fun choosePhoto() {
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
            closeBottomSheet()
            Toast.makeText(
                context,
                getString(R.string.photopicker_unsupported),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    /**
     * Ajouter nouveau lieu dans la bd
     */
    private fun addNewPlace(_view: View) {
        val name = _view.findViewById<TextInputEditText>(R.id.txtInpName).text.toString()
        val address = _view.findViewById<TextInputEditText>(R.id.txtInpAddress).text.toString()
        val description =
            _view.findViewById<TextInputEditText>(R.id.txtInpDescription).text.toString()

        // Valider champs
        val isvalid = validateBottomSheetFields(name, address, description)

        if (!isvalid) {
            Toast.makeText(
                context,
                getString(R.string.bottom_sheet_validate_fields),
                Toast.LENGTH_SHORT
            ).show()
            return
        }


        // Insérer le nouveau lieu dans la bd
        lifecycle.coroutineScope.launch {
            // id est autogénéré <---
            val newPlace =
                Place(
                    0,
                    name = name,
                    address = address,
                    description = description,
                    imagePath = tempPhotoUri.toString(),
                    isVisited = false
                )
            placeViewModel.insertPlace(newPlace)
        }

        // Fermer bottom sheet
        closeBottomSheet(view)
    }

    /**
     * Valider que tous les champs dans bottom sheet sont remplis
     * @return True si valide
     */
    private fun validateBottomSheetFields(
        name: String,
        address: String,
        description: String,
    ): Boolean {
        if (name.isEmpty() || address.isEmpty() || description.isEmpty() || tempPhotoUri == null) {
            return false
        }

        return true
    }


    /**
     * Ouvrir bottom sheet
     */
    private fun openBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Fermer bottom sheet
     */
    private fun closeBottomSheet(_view: View? = null) {
        // Effacer champs si view est défini
        if (view != null) {
            _view?.findViewById<TextInputEditText>(R.id.txtInpName)?.setText("")
            _view?.findViewById<TextInputEditText>(R.id.txtInpAddress)?.setText("")
            _view?.findViewById<TextInputEditText>(R.id.txtInpDescription)?.setText("")
            tempPhotoUri = null
        }
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    /**
     * Ajouter un lieu dans la liste des lieux visités
     * @param place Lieu visité
     */
    private fun addToVisitedPlaces(place: Place) {
        // Mettre à jour le status `is_visited`
        lifecycle.coroutineScope.launch {
            val updatedPlace =
                Place(
                    id = place.id,
                    name = place.name,
                    address = place.address,
                    description = place.description,
                    imagePath = place.imagePath,
                    isVisited = true // <---
                )
            placeViewModel.updatePlace(updatedPlace)
        }

        // Confirmation
        Toast.makeText(context, getString(R.string.saved_place_confirmation), Toast.LENGTH_SHORT)
            .show()
    }

    /**
     * Ouvir le pop up menu quand on clic longtemps sur un lieu
     */
    private fun openPopupMenu(place: Place, view: View) {
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.pop_up_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.pop_up_menu_edit -> {
                    // Mettre en stock le lieu qui sera édité
                    placeViewModel.setCurrentEditPlace(place)
                    navigateToEditPlaceFragment()
                    true
                }

                R.id.pop_up_menu_delete -> {
                    deletePlace(place)
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    /**
     * Supprimer un lieu (du popup menu `openPopupMenu()`)
     */
    private fun deletePlace(place: Place) {
        MaterialAlertDialogBuilder(requireContext())
            .setIcon(R.drawable.baseline_delete_forever_24)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_text))
            .setNegativeButton(resources.getString(R.string.delete_dialog_decline)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.delete_dialog_confirm)) { _, _ ->
                // Supprimer lieu
                lifecycle.coroutineScope.launch {
                    placeViewModel.deletePlace(place)
                }

                // Confirmation
                Toast.makeText(
                    context,
                    getString(R.string.deleted_place_confirmation),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
            .show()

    }

    /**
     * Naviguer vers le fragment des paramètres
     */
    private fun navigateToSettingsFragment() {
        findNavController().navigate(R.id.action_navigation_home_to_settingsFragment)
    }

    /**
     * Naviguer vers le fragment modification d'un lieu
     */
    private fun navigateToEditPlaceFragment() {
        findNavController().navigate(R.id.editPlaceFragment)
    }
}