package net.rainbowcode.jpixelface;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimedConcurrentCache<K, V> extends ConcurrentHashMap<K, V>{
    private final ConcurrentHashMap<K, Long> times = new ConcurrentHashMap<>();
    private final Long TTL;

    public TimedConcurrentCache(Long ttl) {
        TTL = ttl;
        Thread thread = new Thread(){
            @Override
            public void run() {
                while (true) {
                    for (Map.Entry<K, Long> entry : times.entrySet()) {
                        Long value = entry.getValue();
                        if (System.currentTimeMillis() - value >= TTL) {
                            times.remove(entry.getKey());
                            remove(entry.getKey());
                            System.out.println(entry.getKey());
                        }
                    }

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public V put(K key, V value) {
        times.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }
}
