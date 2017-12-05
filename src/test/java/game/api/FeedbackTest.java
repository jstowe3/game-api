package game.api;

import static io.dropwizard.testing.FixtureHelpers.*;
import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.jackson.Jackson;
import org.junit.Test;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FeedbackTest {

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @Test
    public void serializesToJSON() throws Exception {
        final Feedback feedback = new Feedback(19, 3, "love it!", "test_user_id_14", "test_session_id_2");
        final String expected = MAPPER.writeValueAsString(
                MAPPER.readValue(fixture("fixtures/feedback.json"), Feedback.class));

        assertThat(MAPPER.writeValueAsString(feedback)).isEqualTo(expected);
    }
}
