package game.db;

import redis.clients.jedis.Jedis;
import game.api.Feedback;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by joseph.stowe on 12/5/17.
 */
public class FeedbackStore {

    private Feedback feedback;
    public static final String KEY_PREFIX = "Feedback:";

    public FeedbackStore(Feedback feedback) {
        this.feedback = feedback;
    }

    public static long getNextId() {
        return RedisHelper.getNextId(RedisHelper.NEXT_FEEDBACK_ID_KEY, RedisHelper.jedis());
    }

    public static void validatePostParams(String userId, String sessionId) {
        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            boolean isReviewd = jedis.sismember(RedisHelper.SESSION_FEEDBACK_USERS_SET_PREFIX+sessionId, userId);
            if(isReviewd){
                throw new WebApplicationException("UserId: "+userId+" already reviewed Session: "+sessionId,
                        Response.Status.CONFLICT);
            }
        }
    }

    public static String getKeyFromRatingFeedbackIndex(String member) {
        String paddedId = member.split(":")[1];
        String id = paddedId.replaceFirst("^0+(?!$)", "");
        return KEY_PREFIX + id;
    }

    public void save() {
        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            String key = KEY_PREFIX+feedback.getId();
            HashMap<String, String> hash = new HashMap<>();
            hash.put("id", Long.toString(feedback.getId()));
            hash.put("rating", Integer.toString(feedback.getRating()));
            hash.put("userId", feedback.getUserId());
            hash.put("sessionId", feedback.getSessionId());
            hash.put("comment", feedback.getComment());
            jedis.hmset(key, hash);

            // Add this userId to the set of users who have left feedback for this sessionId
            jedis.sadd(RedisHelper.SESSION_FEEDBACK_USERS_SET_PREFIX+feedback.getSessionId(), feedback.getUserId());
            // Add this feedback to the rating-feedback index, order by rating followed by Feedback-id
            jedis.zadd(RedisHelper.RATING_FEEDBACK_INDEX_KEY, 0, feedback.getRating()+":"+String.format("%010d", feedback.getId()));
            // Add to index by rating
            jedis.zadd(RedisHelper.RATING_INDEX_KEY, feedback.getRating(), key);
            // Add to index by id
            jedis.zadd(RedisHelper.FEEDBACK_ID_INDEX_KEY, feedback.getId(), key);
        }
    }

    public static ArrayList<Feedback> getRecent(int maxRating, int minRating, int limit) {
        Set<String> resultKeys;

        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            if(maxRating == Feedback.MAX_RATING && minRating == Feedback.MIN_RATING) {
                resultKeys = jedis.zrevrange(RedisHelper.FEEDBACK_ID_INDEX_KEY, 0, limit -1);
            } else if(maxRating == minRating) {
                Set<String> resultMembers = jedis.zrevrangeByLex(RedisHelper.RATING_FEEDBACK_INDEX_KEY,
                        "("+Integer.toString(minRating + 1),
                        "["+Integer.toString(maxRating),
                        0, limit);
                resultKeys = new LinkedHashSet<>();
                for(String member : resultMembers) {
                    String key = FeedbackStore.getKeyFromRatingFeedbackIndex(member);
                    resultKeys.add(key);
                }
            } else {
                resultKeys = RedisHelper.getCompositeRange(jedis,
                        RedisHelper.RATING_INDEX_KEY,
                        RedisHelper.FEEDBACK_ID_INDEX_KEY,
                        minRating,
                        maxRating,
                        limit);
            }

            ArrayList<Feedback> results = new ArrayList<>();
            for(String key : resultKeys) {
                Map<String,String> hash = jedis.hgetAll(key);
                results.add(new Feedback(
                        Long.parseLong(hash.get("id")),
                        Integer.parseInt(hash.get("rating")),
                        hash.get("comment"),
                        hash.get("userId"),
                        hash.get("sessionId")));
            }
            return results;
        }
    }

}
