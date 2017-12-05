package game.health;

import com.codahale.metrics.health.HealthCheck;
import game.db.RedisHelper;
import redis.clients.jedis.Jedis;

public class ReviewHealthCheck extends HealthCheck {

    public ReviewHealthCheck() {

    }

    @Override
    protected Result check() throws Exception {
        try (Jedis jedis = (Jedis) RedisHelper.jedis()) {
            Long lastId = Long.parseLong(jedis.get(RedisHelper.NEXT_FEEDBACK_ID_KEY));
            if (lastId instanceof Long && lastId > 0) {
                return Result.healthy();
            }
            return Result.unhealthy("Unable to check the last Feedback ID.");
        }
    }
}