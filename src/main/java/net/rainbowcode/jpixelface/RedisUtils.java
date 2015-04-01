package net.rainbowcode.jpixelface;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtils
{
    private static final JedisPool pool = new JedisPool(new JedisPoolConfig(),
            System.getenv("REDIS_PORT_6379_TCP_ADDR"));

    /**
     * Sets the key and expiry time
     *
     * @param key    The key
     * @param value  The value
     * @param expiry The expiry time in seconds
     */
    public static void setAndExpire(String key, String value, int expiry)
    {
        try (Jedis jedis = pool.getResource())
        {
            jedis.set(key, value);
            jedis.expire(key, expiry);
        }
    }

    /**
     * Sets the key and expiry time
     *
     * @param key    The key
     * @param value  The value
     * @param expiry The expiry time in seconds
     */
    public static void setAndExpire(byte[] key, byte[] value, int expiry)
    {
        try (Jedis jedis = pool.getResource())
        {
            jedis.set(key, value);
            jedis.expire(key, expiry);
        }
    }

    public static String getAsString(String key)
    {
        String retVal = null;
        try (Jedis jedis = pool.getResource())
        {
            retVal = jedis.get(key);
        }
        return retVal;
    }

    public static byte[] getAsBytes(byte[] key)
    {
        byte[] retVal = null;
        try (Jedis jedis = pool.getResource())
        {
            retVal = jedis.get(key);
        }
        return retVal;
    }

    public static JsonObject getAsJson(String key)
    {
        JsonObject retVal = null;
        try (Jedis jedis = pool.getResource())
        {
            retVal = jsonObjectFromString(jedis.get(key));
        }
        return retVal;
    }

    private static JsonObject jsonObjectFromString(String string)
    {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(string).getAsJsonObject();
        return object;
    }

    public static boolean exists(String key)
    {
        boolean retVal = false;
        try (Jedis jedis = pool.getResource())
        {
            retVal = jedis.exists(key);
        }
        return retVal;
    }

    public static boolean exists(byte[] key)
    {
        boolean retVal = false;
        try (Jedis jedis = pool.getResource())
        {
            retVal = jedis.exists(key);
        }
        return retVal;
    }
}
