package game.db;

import redis.clients.jedis.*;

import java.util.*;

/**
 * Created by joseph.stowe on 12/3/17.
 */
public class RedisHelper {

    private static String[] _redis_hosts;
    private static String _redis_host;
    private static JedisCluster _redis_cluster = null;
    private static JedisPool _pool = null;
    public static final String NEXT_FEEDBACK_ID_KEY = "NEXT_FEEDBACK_ID";
    public static final String SESSION_FEEDBACK_USERS_SET_PREFIX = "sSessionFeedbackUsers:";
    public static final String RATING_FEEDBACK_INDEX_KEY = "zRatingFeedbackIndex";
    public static final String RATING_INDEX_KEY = "zRatingIndex";
    public static final String FEEDBACK_ID_INDEX_KEY = "zFeedbackIdIndex";

    public static void setRedisCluster(JedisCluster cluster) {
        _redis_cluster = cluster;
    }
    public static void setRedisHosts(String[] redisHosts) {_redis_hosts = redisHosts;}
    public static void setRedisHost(String redisHost) {_redis_host = redisHost;}

    // Alias of getJedisCluster()
    public static JedisCommands jedis() {
        return getJedis();
    }

    // Call this method if redis is running in a cluster (_redisServer is a comma separated list of hosts)
    public static JedisCluster getJedisCluster() {
        if(_redis_cluster == null) {
            Set<HostAndPort> hosts = new HashSet<>();
            for(String host_string : _redis_hosts) {
                String[] host_and_port = host_string.split(":");
                hosts.add(new HostAndPort(host_and_port[0], Integer.parseInt(host_and_port[1])));
            }
            _redis_cluster = new JedisCluster(hosts);
        }
        return _redis_cluster;
    }

    public static Jedis getJedis() {
        if (_pool == null) {
            _pool = new JedisPool(new JedisPoolConfig(), _redis_host);
        }
        return _pool.getResource();
    }

    public static long getNextId(String key, JedisCommands jedis) {
        return jedis.incr(key);
    }

    //TODO: Handle multi-key commands in case of Redis Cluster (using redis-hashing)
    public static Set<String> getCompositeRange(Jedis jedis,
                                                String setKey1,
                                                String setKey2,
                                                int set1Min,
                                                int set1Max,
                                                int limit) {
        String tempKey1 = "zTemp:"+UUID.randomUUID().toString();
        jedis.zunionstore(tempKey1, setKey1); //make a copy of setKey1
        // Remove members outside of target range
        jedis.zremrangeByScore(tempKey1, "-inf", "("+set1Min);
        jedis.zremrangeByScore(tempKey1, "("+set1Max, "+inf");
        String tempResult1 = "zTempResult"+UUID.randomUUID().toString();
        ZParams params = new ZParams();
        // Order by the score of setKey2 in the result set
        params.weightsByDouble(1,0);
        jedis.zinterstore(tempResult1, params, setKey2, tempKey1);
        Set<String> resultSet = jedis.zrevrange(tempResult1, 0, limit - 1);
        jedis.del(tempResult1);
        jedis.del(tempKey1);
        return resultSet;
    }

}
