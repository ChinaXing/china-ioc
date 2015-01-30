package com.chinaxing.ioc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2015/1/29.
 */
public class CollectionUtil {
    public static <K, V> void addMapList(Map<K, List<V>> map, K key, V object) {
        if (map.containsKey(key)) {
            map.get(key).add(object);
        } else {
            List<V> l = new ArrayList<V>();
            map.put(key, l);
            l.add(object);
        }
    }
}
