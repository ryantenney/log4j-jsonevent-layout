package net.logstash.log4j;

import java.util.HashMap;

class NonNullHashMap<K, V> extends HashMap<K, V> {

    private static final long serialVersionUID = 1384186911441979506L;

    @Override
    public V put(K key, V value) {
        if (value != null) {
            return super.put(key, value);
        }
        return null;
    }

}