package com.example.lmfag.utility;

import androidx.annotation.NonNull;

import java.util.Map;

public class GetOrDefault {


    public static <K, V> V getOrDefault(@NonNull Map<K, V> map, K key, V defaultValue) {
        V v;
        return (((v = map.get(key)) != null) || map.containsKey(key))
                ? v
                : defaultValue;
    }
}
