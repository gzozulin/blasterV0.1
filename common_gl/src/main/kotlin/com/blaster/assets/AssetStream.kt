package com.blaster.assets

import java.io.InputStream

class AssetStream {
    fun openAsset(filename: String) : InputStream {
        val url = Thread.currentThread().contextClassLoader.getResource(filename)
        checkNotNull(url) { "Asset not found $filename" }
        return url.openStream()
    }
}