package game.db;

import game.api.Feedback;
import game.resources.ReviewResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

public class FeedbackStoreTest {

    @Mock private Jedis jedis;
    @Mock private JedisPool pool;
    private Map<String,String> hash1 = new HashMap<>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        RedisHelper.setPool(pool);
        Mockito.when(pool.getResource()).thenReturn(new Jedis());
        Mockito.when(RedisHelper.jedis()).thenReturn(jedis);
        hash1.put("id", "101");
        hash1.put("rating", "5");
        hash1.put("comment", "I loved it!");
        hash1.put("userId", "user_409");
        hash1.put("sessionId", "session_2134");
    }

    @Test
    public void getRecentWithDefaultRange() {
        int limit = ReviewResource.DEFAULT_LIMIT;

        HashSet<String> resultKeys1 = new HashSet<>();
        String key1 = "Feedback:1";
        resultKeys1.add(key1);
        Mockito.doReturn(hash1).when(jedis).hgetAll(key1);
        Mockito.when(jedis.zrevrange(RedisHelper.FEEDBACK_ID_INDEX_KEY, 0, limit -1))
                .thenReturn(resultKeys1);

        Mockito.verify(jedis, Mockito.never()).zrevrangeByLex(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt());

        ArrayList<Feedback> list = FeedbackStore.getRecent(5, 1, 15);

        assertThat(list.size() == 1);
        Feedback feedback = list.get(0);
        assertThat(feedback.getId() == 101);
        assertThat(feedback.getRating() == 5);
        assertThat(feedback.getComment().equals("I loved it!"));
        assertThat(feedback.getUserId().equals("user_409"));
        assertThat(feedback.getSessionId().equals("session_2134"));
    }

    @Test
    public void getRecentWithSingleRating() {
        int limit = ReviewResource.DEFAULT_LIMIT;

        Set<String> resultMembers = new HashSet<>();
        String member1 = "5:0000000012";
        resultMembers.add(member1);
        String key1 = "Feedback:12";
        Mockito.doReturn(hash1).when(jedis).hgetAll(key1);
        Mockito.when(jedis.zrevrangeByLex(
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.anyInt(),
                Mockito.anyInt())).thenReturn(resultMembers);

        ArrayList<Feedback> list = FeedbackStore.getRecent(5, 5, limit);

        assertThat(list.size() == 1);
        Feedback feedback = list.get(0);
        assertThat(feedback.getId() == 101);
        assertThat(feedback.getRating() == 5);
        assertThat(feedback.getComment().equals("I loved it!"));
        assertThat(feedback.getUserId().equals("user_409"));
        assertThat(feedback.getSessionId().equals("session_2134"));
    }
}
