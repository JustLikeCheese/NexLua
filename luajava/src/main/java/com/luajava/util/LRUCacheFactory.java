package com.luajava.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LRUCacheFactory {

    private LRUCacheFactory() {}
    public static final class LRUCache<K1, K2, V> {

        private final List<ConcurrentHashMap<K1, Map<K2, V>>> cacheShards;
        private final int innerSize;

        public LRUCache(int level1Size, int level2Size, int shards) {
            if (shards <= 0) {
                throw new IllegalArgumentException("Number of shards must be positive.");
            }
            this.innerSize = level2Size;
            ArrayList<ConcurrentHashMap<K1, Map<K2, V>>> shardList = new ArrayList<>(shards);
            for (int i = 0; i < shards; i++) {
                // level1Size 作为 ConcurrentHashMap 的初始容量
                shardList.add(new ConcurrentHashMap<K1, Map<K2, V>>(level1Size));
            }
            this.cacheShards = Collections.unmodifiableList(shardList);
        }

        public V get(K1 k1, K2 k2) {
            Map<K2, V> inner = getInnerCache(k1);
            // 内层缓存是同步的，所以 get 是线程安全的
            return inner.get(k2);
        }

        public void put(K1 k1, K2 k2, V v) {
            Map<K2, V> inner = getInnerCache(k1);
            // putIfAbsent 保证只在不存在时才放入
            inner.put(k2, v);
        }

        private Map<K2, V> getInnerCache(K1 k1) {
            // 使用 & 0x7FFFFFFF 确保 hashCode 为正数，防止数组负数索引
            int shardIndex = (k1.hashCode() & 0x7FFFFFFF) % cacheShards.size();
            ConcurrentHashMap<K1, Map<K2, V>> shard = cacheShards.get(shardIndex);

            // 这是 Java 7/8 中最高效的 "get-or-create" 模式
            Map<K2, V> inner = shard.get(k1);
            if (inner == null) {
                // 注意：这里 Collections.synchronizedMap() 是必须的，因为 LinkedHashMap 不是线程安全的
                Map<K2, V> newInner = Collections.synchronizedMap(new InnerCache<K2, V>(innerSize));
                inner = shard.putIfAbsent(k1, newInner);
                if (inner == null) {
                    inner = newInner;
                }
            }
            return inner;
        }
        private static final class InnerCache<K, V> extends java.util.LinkedHashMap<K, V> {
            private final int maxEntries;

            private InnerCache(int maxEntries) {
                super(16, 0.75f, true); // true 表示按访问顺序排序
                this.maxEntries = maxEntries;
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        }
    }
    public static final class LRUCacheSingle<K, V> {
        private final List<Map<K, V>> cacheShards;

        public LRUCacheSingle(int capacity, int shards) {
            if (shards <= 0) {
                throw new IllegalArgumentException("Number of shards must be positive.");
            }
            if (capacity <= 0) {
                throw new IllegalArgumentException("Capacity must be positive.");
            }

            ArrayList<Map<K, V>> shardList = new ArrayList<>(shards);
            final int perShardCapacity = (int) Math.ceil((double) capacity / shards);

            for (int i = 0; i < shards; i++) {
                shardList.add(Collections.synchronizedMap(new InnerCache<K, V>(perShardCapacity)));
            }
            this.cacheShards = Collections.unmodifiableList(shardList);
        }

        private Map<K, V> getShard(K key) {
            int shardIndex = (key.hashCode() & 0x7FFFFFFF) % cacheShards.size();
            return cacheShards.get(shardIndex);
        }

        public V get(K key) {
            return getShard(key).get(key);
        }

        public void put(K key, V value) {
            getShard(key).put(key, value);
        }

        // 内部缓存实现与两级缓存的相同
        private static final class InnerCache<K, V> extends java.util.LinkedHashMap<K, V> {
            private final int maxEntries;

            private InnerCache(int maxEntries) {
                super(16, 0.75f, true);
                this.maxEntries = maxEntries;
            }

            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > maxEntries;
            }
        }
    }
}