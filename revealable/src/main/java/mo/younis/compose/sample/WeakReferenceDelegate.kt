package mo.younis.compose.sample

import java.lang.ref.SoftReference
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> weakReference(tIn: T? = null): ReadWriteProperty<Any?, T?> {
    return object : ReadWriteProperty<Any?, T?> {
        var t = WeakReference<T?>(tIn)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            return t.get()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            t = WeakReference(value)
        }
    }
}

fun <T> softReference(tIn: T? = null): ReadWriteProperty<Any?, T?> {
    return object : ReadWriteProperty<Any?, T?> {
        var t = SoftReference<T?>(tIn)
        override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
            return t.get()
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
            t = SoftReference(value)
        }
    }
}
