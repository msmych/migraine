package bot

import java.net.URL

object AppSupport {

    fun resource(path: String): URL {
        return requireNotNull(this::class.java.classLoader.getResource(path))
    }
}