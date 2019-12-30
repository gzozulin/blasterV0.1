package com.blaster.assets

import java.io.InputStream

class AssetStream {
    fun openAsset(filename: String) : InputStream {
        return Thread.currentThread().contextClassLoader.getResource(filename)!!.openStream()
    }
}