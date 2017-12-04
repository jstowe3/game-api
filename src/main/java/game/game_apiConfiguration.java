package game;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.*;
import javax.validation.constraints.*;

public class game_apiConfiguration extends Configuration {

    @NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @NotEmpty
    private String[] redisHosts;

    @NotEmpty
    private String redisHost;

    @JsonProperty
    public String[] getRedisHosts() {
        return redisHosts;
    }

    @JsonProperty
    public void setRedisHosts(String[] redisHosts) {
        this.redisHosts = redisHosts;
    }

    @JsonProperty
    public String getRedisHost() {
        return redisHost;
    }

    @JsonProperty
    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }


}
