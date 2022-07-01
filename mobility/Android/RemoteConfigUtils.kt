package io.odinmanufacturing.utils

/**
 * Util class for firebase remote configuration
 */
object RemoteConfigUtils {
    /**
     * Remote key  for identify pinned state
     */
    const val IS_APP_PINNED = "is_app_pinned"
    /**
     * Remote key for web url
     */
    const val OM_URL = "om_url"
    val DEFAULTS: HashMap<String, Any> = hashMapOf(
        IS_APP_PINNED to false,
        OM_URL to "https://platform.odinmanufacturing.io/",

    )
}
