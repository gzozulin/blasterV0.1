package com.blaster.gl

enum class GlUniform(val label: String) {
    UNIFORM_MODEL_M             ("uModelM"),
    UNIFORM_PROJ_M              ("uProjectionM"),
    UNIFORM_VIEW_M              ("uViewM"),
    UNIFORM_EYE                 ("uEye"),

    UNIFORM_TEXTURE_POSITION    ("uTexPosition"),
    UNIFORM_TEXTURE_NORMAL      ("uTexNormal"),
    UNIFORM_TEXTURE_DIFFUSE     ("uTexDiffuse"),

    UNIFORM_LIGHTS_POINT_CNT    ("uLightsPointCnt"),
    UNIFORM_LIGHTS_DIR_CNT      ("uLightsDirCnt"),
    UNIFORM_LIGHT_0_VECTOR      ("uLights[0].vector"),
    UNIFORM_LIGHT_0_INTENS      ("uLights[0].intensity"),
    UNIFORM_LIGHT_1_VECTOR      ("uLights[1].vector"),
    UNIFORM_LIGHT_1_INTENS      ("uLights[1].intensity"),
    UNIFORM_LIGHT_2_VECTOR      ("uLights[2].vector"),
    UNIFORM_LIGHT_2_INTENS      ("uLights[2].intensity"),
    UNIFORM_LIGHT_3_VECTOR      ("uLights[3].vector"),
    UNIFORM_LIGHT_3_INTENS      ("uLights[3].intensity"),
    UNIFORM_LIGHT_4_VECTOR      ("uLights[4].vector"),
    UNIFORM_LIGHT_4_INTENS      ("uLights[4].intensity"),
    UNIFORM_LIGHT_5_VECTOR      ("uLights[5].vector"),
    UNIFORM_LIGHT_5_INTENS      ("uLights[5].intensity"),
    UNIFORM_LIGHT_6_VECTOR      ("uLights[6].vector"),
    UNIFORM_LIGHT_6_INTENS      ("uLights[6].intensity"),
    UNIFORM_LIGHT_7_VECTOR      ("uLights[7].vector"),
    UNIFORM_LIGHT_7_INTENS      ("uLights[7].intensity"),
    UNIFORM_LIGHT_8_VECTOR      ("uLights[8].vector"),
    UNIFORM_LIGHT_8_INTENS      ("uLights[8].intensity"),
    UNIFORM_LIGHT_9_VECTOR      ("uLights[9].vector"),
    UNIFORM_LIGHT_9_INTENS      ("uLights[9].intensity"),
    UNIFORM_LIGHT_10_VECTOR     ("uLights[10].vector"),
    UNIFORM_LIGHT_10_INTENS     ("uLights[10].intensity"),
    UNIFORM_LIGHT_11_VECTOR     ("uLights[11].vector"),
    UNIFORM_LIGHT_11_INTENS     ("uLights[11].intensity"),
    UNIFORM_LIGHT_12_VECTOR     ("uLights[12].vector"),
    UNIFORM_LIGHT_12_INTENS     ("uLights[12].intensity"),
    UNIFORM_LIGHT_13_VECTOR     ("uLights[13].vector"),
    UNIFORM_LIGHT_13_INTENS     ("uLights[13].intensity"),
    UNIFORM_LIGHT_14_VECTOR     ("uLights[14].vector"),
    UNIFORM_LIGHT_14_INTENS     ("uLights[14].intensity"),
    UNIFORM_LIGHT_15_VECTOR     ("uLights[15].vector"),
    UNIFORM_LIGHT_15_INTENS     ("uLights[15].intensity"),

    UNIFORM_COLOR               ("uColor"),

    UNIFORM_CHAR_INDEX          ("uCharIndex"),
    UNIFORM_CHAR_START          ("uCharStart"),
    UNIFORM_CHAR_SCALE          ("uCharScale"),

    UNIFORM_WIDTH               ("uWidth"),
    UNIFORM_HEIGHT              ("uHeight"),

    UNIFORM_SCALE_FLAG          ("uScaleFlag"),
    UNIFORM_TRANSPARENCY_FLAG   ("uTransparencyFlag");

    companion object {
        fun uniformLightVector(number: Int) = valueOf("UNIFORM_LIGHT_${number}_VECTOR")
        fun uniformLightIntensity(number: Int) = valueOf("UNIFORM_LIGHT_${number}_INTENS")
    }
}