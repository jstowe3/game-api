package game.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import game.db.FeedbackStore;
import org.hibernate.validator.constraints.Length;

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
        FeedbackStore.validatePostParams(userId, sessionId);
        this.id = FeedbackStore.getNextId();
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
