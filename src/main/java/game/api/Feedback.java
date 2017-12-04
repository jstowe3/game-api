package game.api;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import game.db.RedisHelper;
import org.hibernate.validator.constraints.Length;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCommands;

import java.util.HashMap;
import java.util.Optional;

/**
 * Created by joseph.stowe on 12/3/17.
 */
public class Feedback {
    private long id;
    public static final int MAX_RATING = 5;
    public static final int MIN_RATING = 1;

    @NotNull
    @Max(5)
    @Min(1)
    private int rating;

    @NotNull
    private String userId;

    @NotNull
    private String sessionId;

    @Length(max = 10)
    private String comment;

    public Feedback() {
        // Jackson deserialization
    }

    public Feedback(int rating, String comment, String userId, String sessionId) {
        validateParams(userId, sessionId);
        this.id = RedisHelper.getNextId(RedisHelper.NEXT_FEEDBACK_ID_KEY, RedisHelper.jedis());
        this.rating = rating;
        this.userId = userId;
        this.sessionId = sessionId;
        this.comment = comment;
    }

    public Feedback(long id, int rating, String comment, String userId, String sessionId) {
        this.id = id;
        this.rating = rating;
        this.userId = userId;
        this.sessionId = sessionId;
        this.comment = comment;
    }

    private void validateParams(String userId, String sessionId) {
        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            boolean isReviewd = jedis.sismember(RedisHelper.SESSION_FEEDBACK_USERS_SET_PREFIX+sessionId, userId);
            if(isReviewd){
                throw new WebApplicationException("UserId: "+userId+" already reviewed Session: "+sessionId,
                        Response.Status.CONFLICT);
            }
        }
    }

    public void save() {
        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            String key = "Feedback:"+id;
            HashMap<String, String> hash = new HashMap<>();
            hash.put("id", Long.toString(id));
            hash.put("rating", Integer.toString(rating));
            hash.put("userId", userId);
            hash.put("sessionId", sessionId);
            hash.put("comment", comment);
            jedis.hmset(key, hash);

            // Add this userId to the set of users who have left feedback for this sessionId
            jedis.sadd(RedisHelper.SESSION_FEEDBACK_USERS_SET_PREFIX+sessionId, userId);
            // Add this feedback to the rating-feedback index, order by rating followed by Feedback-id
            jedis.zadd(RedisHelper.RATING_FEEDBACK_INDEX_KEY, 0, rating+":"+id);
            // Add to index by rating
            jedis.zadd(RedisHelper.RATING_INDEX_KEY, rating, key);
            // Add to index by id
            jedis.zadd(RedisHelper.FEEDBACK_ID_INDEX_KEY, id, key);
        }
    }

    @JsonProperty
    public long getId() {
        return id;
    }

    @JsonProperty
    public int getRating() {
        return rating;
    }

    @JsonProperty
    public String getComment() {
        return comment;
    }

    @JsonProperty
    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty
    public String getUserId() {
        return userId;
    }

}
