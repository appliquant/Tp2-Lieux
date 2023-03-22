package com.example.tp2.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2.databinding.PlaceItemRecylerviewBinding
import com.example.tp2.db.Place


class PlaceAdapter(
    private val onItemClicked: (Place) -> Unit,
    private val onItemLongClicked: (Place, View) -> Unit,
) :
    ListAdapter<Place, PlaceAdapter.ViewHolder>(DiffCallback) {
    /**
     * Référence vers un élément unique du recycler view
     */
    class ViewHolder(private var binding: PlaceItemRecylerviewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // PlaceItemRecylerviewBinding -> binding de `res/layout/place_item_recylerview`

        /**
         * Fonction utilisé pour passer les éléments de onBindViewHolder
         * @see onBindViewHolder
         */
        fun bind(place: Place) {
            val imageUri = Uri.parse(place.imagePath)
            binding.placeItemRecyclerviewImage.setImageURI(imageUri)
            binding.placeItemRecyclerviewName.text = place.name
            binding.placeItemRecyclerviewAddress.text = place.address
            binding.placeItemRecyclerviewDescription.text = place.description
        }
    }

    /**
     * Créer des nouvelles vues
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = ViewHolder(
            PlaceItemRecylerviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

        // Click listener sur une carte
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            val item = getItem(position)
            onItemClicked(item)
        }

        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.adapterPosition
            val item = getItem(position)
            onItemLongClicked(item, viewHolder.itemView)
            true
        }

        return viewHolder
    }

    /**
     * Remplacer le contenu d'une vue d'élément de liste
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }


    /**
     * Objet qui aide ListAdapter à identifier les éléments qui ont été
     * modifiés entre l'ancienne liste et la nouvelle après une mise à jour
     */
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Place>() {
            override fun areItemsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Place, newItem: Place): Boolean {
                return oldItem == newItem
            }
        }
    }
}