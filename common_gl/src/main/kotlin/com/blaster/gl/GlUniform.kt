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
    UNIFORM_LIGHT_VECTOR        ("uLights[%d].vector"),
    UNIFORM_LIGHT_INTENSITY     ("uLights[%d].intensity"),

    UNIFORM_COLOR               ("uColor"),

    UNIFORM_CHAR_INDEX          ("uCharIndex"),
    UNIFORM_CHAR_START          ("uCharStart"),
    UNIFORM_CHAR_SCALE          ("uCharScale"),

    UNIFORM_WIDTH               ("uWidth"),
    UNIFORM_HEIGHT              ("uHeight"),

    UNIFORM_SCALE_FLAG          ("uScaleFlag"),
    UNIFORM_TRANSPARENCY_FLAG   ("uTransparencyFlag");
}