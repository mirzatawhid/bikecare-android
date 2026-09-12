package studio.appvero.bikecare.core.localization

enum class AppLanguage(
    val tag: String
) {
    ENGLISH("en"),
    BANGLA("bn");

    companion object {

        fun fromTag(tag: String?): AppLanguage {

            return entries.firstOrNull {
                it.tag == tag
            } ?: ENGLISH
        }
    }
}