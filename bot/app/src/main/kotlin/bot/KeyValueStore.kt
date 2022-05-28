package bot

interface KeyValueStore<K, V> {

    fun get(key: K): V?

    fun put(key: K, value: V): V
}

class HashMapKeyValueStore<K, V> : KeyValueStore<K, V> {

    private val map = HashMap<K, V>()

    override fun get(key: K): V? {
        return map[key]
    }

    override fun put(key: K, value: V): V {
        map[key] = value
        return value
    }
}