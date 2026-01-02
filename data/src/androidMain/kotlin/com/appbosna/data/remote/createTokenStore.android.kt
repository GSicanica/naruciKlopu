import android.content.Context
import com.appbosna.data.remote.AndroidTokenStore

lateinit var appContext: Context

actual fun createTokenStore(): TokenStore {
    // Vrati instancu koja koristi Android kontekst
    return AndroidTokenStore(appContext)
}