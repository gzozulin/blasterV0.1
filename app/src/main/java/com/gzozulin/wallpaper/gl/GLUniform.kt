package com.gzozulin.wallpaper.gl

enum class GLUniform(val label: String) {
    UNIFORM_MODEL_M(            "uModelM"),
    UNIFORM_PROJ_M(             "uProjectionM"),
    UNIFORM_VIEW_M(             "uViewM"),
    UNIFORM_VIEW_POS(           "uViewPosition"),
    UNIFORM_TEXTURE_DIFFUSE(    "uTextureDiffuse"),
    UNIFORM_TEXTURE_SPECULAR(   "uTextureSpecular"),
    UNIFORM_TEXTURE_POSITION(   "uTexturePosition"),
    UNIFORM_TEXTURE_NORMAL(     "uTextureNormal"),
    UNIFORM_TEXTURE_ALBEDO_SPEC("uTextureAlbedoSpec"),
    UNIFORM_LIGHT_POS(          "uLightPos"),
    UNIFORM_LIGHT_COLOR(        "uLightColor")
}