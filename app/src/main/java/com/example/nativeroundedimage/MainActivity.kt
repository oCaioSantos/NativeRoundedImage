package com.example.nativeroundedimage

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.widget.Button
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable

class MainActivity : AppCompatActivity() {

    private val galleryActivityForResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageURI ->
            imageURI?.let {
                setImage(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnChooseImage).setOnClickListener {
            chooseFromGallery()
        }
        drawCircularImage()
    }

    private fun drawCircularImage(imageURI: Uri? = null) {
        var bitmapDrawable: Bitmap? = null
        var picture: Drawable? = null
        if (imageURI == null) {
            picture = ContextCompat.getDrawable(baseContext, R.drawable.greek_pic_large)
            bitmapDrawable = (picture as BitmapDrawable).bitmap
        } else {
            val contentResolver: ContentResolver = applicationContext.contentResolver
            try {
                picture = BitmapFactory
                    .decodeStream(contentResolver.openInputStream(imageURI))
                    .toDrawable(resources)
                bitmapDrawable = picture.bitmap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (picture is BitmapDrawable && bitmapDrawable is Bitmap) {
            val pictureIsPortrait = picture.isPortrait()
            // Cálculo das dimensões da imagem de saída com base na orientação
            val pictureWidth: Int
            val pictureHeight: Int
            with(baseContext.resources.displayMetrics) {
                val pictureAspectRatio = picture.aspectRatio()
                if (pictureIsPortrait) {
                    // Se a imagem original for retrato
                    pictureWidth = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 200f, this
                    ).toInt()
                    pictureHeight = (pictureWidth / pictureAspectRatio).toInt()
                } else {
                    // Se a imagem original for paisagem
                    pictureHeight = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 200f, this
                    ).toInt()
                    pictureWidth = (pictureHeight * pictureAspectRatio).toInt()
                }
            }

            // Criação de um Bitmap de saída com as dimensões calculadas
            val dimension = if (pictureIsPortrait) pictureWidth else pictureHeight
            val outputBitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888)

            // Desenho da imagem original em um círculo no Bitmap de saída
            Canvas(outputBitmap).apply {
                val bitmapDrawableScaled = Bitmap.createScaledBitmap(
                    bitmapDrawable, pictureWidth, pictureHeight, false
                )

                // Cálculo das coordenadas para centralizar a imagem
                val x: Int
                val y: Int
                if (pictureIsPortrait) {
                    x = 0
                    y = -((bitmapDrawableScaled.height - outputBitmap.height) / 2)
                } else {
                    x = -((bitmapDrawableScaled.width - outputBitmap.width) / 2)
                    y = 0
                }

                // Configuração do Paint para desenhar um círculo
                val paint = Paint()
                    .apply { isAntiAlias = true }
                    .also {
                        val circleRadius = outputBitmap.width / 2f
                        drawCircle(circleRadius, circleRadius, circleRadius, it)
                    }

                // Desenho da imagem original dentro do círculo usando o modo SRC_IN
                Rect(
                    x, y, bitmapDrawableScaled.width + x, bitmapDrawableScaled.height + y
                ).also { destination ->
                    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                    drawBitmap(bitmapDrawableScaled, null, destination, paint)
                }
            }

            // Definição do Bitmap de saída na ImageView
            BitmapDrawable(resources, outputBitmap).also {
                findViewById<ImageView>(R.id.imageView).setImageBitmap(it.bitmap)
            }
        }
    }

    fun BitmapDrawable.isPortrait(): Boolean {
        return this.aspectRatio() < 1
    }

    fun BitmapDrawable.aspectRatio(): Double {
        return this.bitmap.width.toDouble() / this.bitmap.height.toDouble()
    }

    private fun setImage(imageURI: Uri) {
        drawCircularImage(imageURI)
    }

    private fun chooseFromGallery() {
        galleryActivityForResult.launch("image/*")
    }
}