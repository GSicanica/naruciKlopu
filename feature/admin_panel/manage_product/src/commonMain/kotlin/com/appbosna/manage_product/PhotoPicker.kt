package com.appbosna.manage_product

import androidx.compose.runtime.Composable
import com.appbosna.data.remote.KmpFile

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class PhotoPicker {
    fun open()
    @Composable
    fun InitializePhotoPicker(onImageSelect: (KmpFile?) -> Unit)
}
