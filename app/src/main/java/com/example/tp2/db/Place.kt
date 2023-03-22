package com.example.tp2.db

import android.net.Uri
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Représente un endroit
 */
@Entity(tableName = "places")
data class Place(
    /**
     * Id de l'endroit
     * Id est autogénéré
     */
    @PrimaryKey(autoGenerate = true) val id: Int,

    /**
     * Nom de l'endroit
     */
    @NonNull @ColumnInfo(name = "name") val name: String,

    /**
     * Adresse de l'endroit
     */
    @NonNull @ColumnInfo(name = "address") val address: String,

    /**
     * Description de l'endroit
     */
    @NonNull @ColumnInfo(name = "description") val description: String,

    /**
     * Sauvegarde un objet `Uri` en tant que string `Uri.toString()...`.
     * Faire `Uri.parse(...)` pour transformer en objet `Uri`
     */
    @NonNull @ColumnInfo(name = "image_path") val imagePath: String,

    /**
     * Valeur pour savoir si un lieu à été visité
     * True = a été visité
     */
    @NonNull @ColumnInfo(name = "is_visited", defaultValue = "false") val isVisited: Boolean,
)




