package com.blaster.gl

enum class GlUniform(val label: String) {
    UNIFORM_MODEL_M(            "uModelM"),
    UNIFORM_PROJ_M(             "uProjectionM"),
    UNIFORM_VIEW_M(             "uViewM"),
    UNIFORM_VIEW_POS(           "uViewPosition"),

    UNIFORM_TEXTURE_POSITION(   "uTexPosition"),
    UNIFORM_TEXTURE_NORMAL(     "uTexNormal"),
    UNIFORM_TEXTURE_DIFFUSE(    "uTexDiffuse"),

    UNIFORM_LIGHT_0_POS(        "uLights[0].position"),
    UNIFORM_LIGHT_0_COLOR(      "uLights[0].color"),
    UNIFORM_LIGHT_1_POS(        "uLights[1].position"),
    UNIFORM_LIGHT_1_COLOR(      "uLights[1].color"),
    UNIFORM_LIGHT_2_POS(        "uLights[2].position"),
    UNIFORM_LIGHT_2_COLOR(      "uLights[2].color"),
    UNIFORM_LIGHT_3_POS(        "uLights[3].position"),
    UNIFORM_LIGHT_3_COLOR(      "uLights[3].color"),
    UNIFORM_LIGHT_4_POS(        "uLights[4].position"),
    UNIFORM_LIGHT_4_COLOR(      "uLights[4].color"),
    UNIFORM_LIGHT_5_POS(        "uLights[5].position"),
    UNIFORM_LIGHT_5_COLOR(      "uLights[5].color"),
    UNIFORM_LIGHT_6_POS(        "uLights[6].position"),
    UNIFORM_LIGHT_6_COLOR(      "uLights[6].color"),
    UNIFORM_LIGHT_7_POS(        "uLights[7].position"),
    UNIFORM_LIGHT_7_COLOR(      "uLights[7].color"),
    UNIFORM_LIGHT_8_POS(        "uLights[8].position"),
    UNIFORM_LIGHT_8_COLOR(      "uLights[8].color"),
    UNIFORM_LIGHT_9_POS(        "uLights[9].position"),
    UNIFORM_LIGHT_9_COLOR(      "uLights[9].color"),
    UNIFORM_LIGHT_10_POS(       "uLights[10].position"),
    UNIFORM_LIGHT_10_COLOR(     "uLights[10].color"),
    UNIFORM_LIGHT_11_POS(       "uLights[11].position"),
    UNIFORM_LIGHT_11_COLOR(     "uLights[11].color"),
    UNIFORM_LIGHT_12_POS(       "uLights[12].position"),
    UNIFORM_LIGHT_12_COLOR(     "uLights[12].color"),
    UNIFORM_LIGHT_13_POS(       "uLights[13].position"),
    UNIFORM_LIGHT_13_COLOR(     "uLights[13].color"),
    UNIFORM_LIGHT_14_POS(       "uLights[14].position"),
    UNIFORM_LIGHT_14_COLOR(     "uLights[14].color"),
    UNIFORM_LIGHT_15_POS(       "uLights[15].position"),
    UNIFORM_LIGHT_15_COLOR(     "uLights[15].color"),

    UNIFORM_COLOR(              "uColor"),

    UNIFORM_CHAR_INDEX(         "uCharIndex"),
    UNIFORM_CHAR_START(         "uCharStart"),
    UNIFORM_CHAR_SCALE(         "uCharScale");

    companion object {
        fun uniformLightPosition(number: Int) = valueOf("UNIFORM_LIGHT_${number}_POS")
        fun uniformLightColor(number: Int) = valueOf("UNIFORM_LIGHT_${number}_COLOR")
    }
}