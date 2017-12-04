package game.resources;

import com.codahale.metrics.annotation.Timed;
import game.api.Feedback;
import game.db.RedisHelper;
import org.hibernate.validator.constraints.Length;
import redis.clients.jedis.Jedis;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.core.MediaType;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Created by joseph.stowe on 12/3/17.
 */
@Path("/review")
@Produces(MediaType.APPLICATION_JSON)
public class ReviewResource {

    private int recordLimit = 15;

    public ReviewResource() {}

    //TODO: What media-type do we need to accept from the client?
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    @Timed
    @Path("/{sessionId}")
    public Feedback object(
               @NotNull
               @PathParam("sessionId") String sessionId,
               @NotNull
               @HeaderParam("X-UserId") String userId,
               @FormParam("rating")
               @NotNull
               @Max(5)
               @Min(1) int rating,
               @Length(max = 100)
               @FormParam("comment") Optional<String> comment) {
        String commentValue = comment.orElse("");
        Feedback feedback = new Feedback(rating, commentValue, userId, sessionId);
        feedback.save();
        return feedback;
    }

    @GET
    @Timed
    @Path("/recent")
    public List<Feedback> recent(@Max(5) @Min(1) @QueryParam("maxRating") Optional<Integer> maxRating,
                             @Max(5) @Min(1) @QueryParam("minRating") Optional<Integer> minRating) {
        int maxRatingValue = maxRating.orElse(Feedback.MAX_RATING);
        int minRatingValue = minRating.orElse(Feedback.MIN_RATING);
        int limit = this.recordLimit;
        Set<String> resultKeys;
        try (Jedis jedis = (Jedis)RedisHelper.jedis()) {
            if(maxRatingValue == Feedback.MAX_RATING && minRatingValue == Feedback.MIN_RATING) {
                resultKeys = jedis.zrevrange(RedisHelper.FEEDBACK_ID_INDEX_KEY, 0, limit -1);
            } else {
                resultKeys = RedisHelper.getCompositeRange(jedis,
                        RedisHelper.RATING_INDEX_KEY,
                        RedisHelper.FEEDBACK_ID_INDEX_KEY,
                        minRatingValue,
                        maxRatingValue,
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
