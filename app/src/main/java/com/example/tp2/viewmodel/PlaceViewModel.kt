package com.example.tp2.viewmodel

import androidx.lifecycle.*
import com.example.tp2.db.Place
import com.example.tp2.db.PlaceDao


class PlaceViewModel(private val placeDao: PlaceDao) : ViewModel() {
    // =========== VARIABLES ===========
    private val _isAdmin = MutableLiveData<Boolean>()
    val isAdmin: LiveData<Boolean> = _isAdmin

    private val _currentEditPlace = MutableLiveData<Place>()
    val currentEditPlace: LiveData<Place> = _currentEditPlace

    // =========== MÉTHODES ANDROID ROOM ===========
    /**
     * Récupérer tous les endroits
     */
    fun getAllPlaces() = placeDao.getAllPlaces()

    /**
     * Récupérer tous les endroits visités
     */
    fun getAllVisitedPlaces() = placeDao.getAllVisitedPlaces()

    /**
     * Insérer un nouvel endroit
     */
    suspend fun insertPlace(place: Place) = placeDao.insertPlace(place)

    /**
     * Mettre à jour un endroit
     */
    suspend fun updatePlace(place: Place) = placeDao.updatePlace(place)

    /**
     * Supprimer un endroit
     */
    suspend fun deletePlace(place: Place) = placeDao.deletePlace(place)

    /**
     * Supprimer tous les endroits enregitrés
     */
    suspend fun deleteAllPlaces() = placeDao.deleteAllPlaces()

    // =========== INITIALISATION ===========
    init {
        setAdmin(false)
    }

    // =========== MÉTHODES ===========
    /**
     * Changer le status d'administrateur
     * @param isAdmin True si admin
     */
    fun setAdmin(isAdmin: Boolean) {
        _isAdmin.value = isAdmin
    }

    /**
     * Changer le lieu qui va pe
     */
    fun setCurrentEditPlace(place: Place) {
        _currentEditPlace.value = place
    }
}

/**
 * Factory
 */
class PlaceViewModelFactory(private val _placeDao: PlaceDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaceViewModel(_placeDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}