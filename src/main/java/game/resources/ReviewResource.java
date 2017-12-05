package game.resources;

import com.codahale.metrics.annotation.Timed;
import game.api.Feedback;
import game.db.FeedbackStore;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Created by joseph.stowe on 12/3/17.
 */
@Path("/review")
@Produces(MediaType.APPLICATION_JSON)
public class ReviewResource {

    public static final int DEFAULT_LIMIT = 15;

    public ReviewResource() {}

    //TODO: What media-type do we need to accept from the client?
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @POST
    @Timed
    @Path("/{sessionId}")
    public Feedback create(
               @NotNull @PathParam("sessionId") String sessionId,
               @NotNull @HeaderParam("X-UserId") String userId,
               @FormParam("rating") @NotNull @Max(5) @Min(1) int rating,
               @FormParam("comment") @Length(max = 100) Optional<String> comment) {
        String commentValue = comment.orElse("");
        Feedback feedback = new Feedback(rating, commentValue, userId, sessionId);
        new FeedbackStore(feedback).save();
        return feedback;
    }

    @GET
    @Timed
    @Path("/recent")
    public List<Feedback> recent(
            @Max(5) @Min(1) @QueryParam("maxRating") Optional<Integer> maxRating,
            @Max(5) @Min(1) @QueryParam("minRating") Optional<Integer> minRating) {
        int maxRatingValue = maxRating.orElse(Feedback.MAX_RATING);
        int minRatingValue = minRating.orElse(Feedback.MIN_RATING);
        if(maxRatingValue < minRatingValue) {
            throw new WebApplicationException("maxRating: "+maxRatingValue+" must be greater or equal to minRating: "+minRatingValue,
                    Response.Status.BAD_REQUEST);
        }
        return FeedbackStore.getRecent(maxRatingValue, minRatingValue, this.DEFAULT_LIMIT);
    }
}
