package com.blaster.assets

import java.io.InputStream

interface AssetStream {
    fun openAsset(filename: String) : InputStream
}