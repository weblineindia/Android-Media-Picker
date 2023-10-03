package com.wli.customcameralibrary

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.customcameralibrary.R
import java.io.File

class SelectedMediaAdapter : RecyclerView.Adapter<SelectedMediaAdapter.ImageViewHolder>() {

    private val imageList = ArrayList<Uri>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_image_view, parent, false)
        return ImageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUri = imageList[position]
        holder.bindImage(imageUri)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)

        fun bindImage(imageUri: Uri) {
            if (imageUri.path?.contains(".jpg") == true) {
                val bitmap = BitmapFactory.decodeFile(File(imageUri.path).path)
                imageView.setImageBitmap(bitmap)
            } else {
                Glide.with(itemView.context)
                    .load(imageUri)
                    .error(R.drawable.ic_launcher_background)
                    .into(imageView)
            }
        }
    }

    fun addAll(list: ArrayList<Uri>) {
        imageList.apply {
            clear()
            addAll(list)
            notifyDataSetChanged()
        }
    }
}
