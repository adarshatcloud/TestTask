package com.example.randomimagefetcher

import android.content.Context
import com.example.randomimagefetcher.interfaces.ImageUrlChooserCallBack
import com.example.randomimagefetcher.interfaces.LanguageChangeListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun showImageChooserDialog(context: Context, callBack: ImageUrlChooserCallBack) {

    var selectedPosition = 0

    MaterialAlertDialogBuilder(context)
        .setTitle(context.getString(R.string.dialog_title_image_chooser))
        .setPositiveButton(context.getString(R.string.label_confirm)) { dialog, which ->
            callBack.onImageUrlChooserListener(context.resources.getStringArray(R.array.image_urls)[selectedPosition])
        }
        .setNegativeButton(context.getString(R.string.label_cancel)) { dialog, which ->
        }
        .setSingleChoiceItems(
            context.resources.getStringArray(R.array.image_urls),
            selectedPosition
        ) { dialog, which ->
            selectedPosition = which
        }
        .show()
}
