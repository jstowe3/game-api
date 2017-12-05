package game;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;

public class game_apiConfiguration extends Configuration {

    @NotEmpty
    private String redisHost;

    @JsonProperty
    public String getRedisHost() {
        return redisHost;
    }

    @JsonProperty
    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

}
