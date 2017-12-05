package game;

import game.db.RedisHelper;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import game.resources.*;
import game.health.*;

public class game_apiApplication extends Application<game_apiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new game_apiApplication().run(args);
    }

    @Override
    public String getName() {
        return "game_api";
    }

    @Override
    public void initialize(final Bootstrap<game_apiConfiguration> bootstrap) {}

    @Override
    public void run(final game_apiConfiguration configuration,
                    final Environment environment) {

        RedisHelper.setRedisHost(configuration.getRedisHost());

        final ReviewResource reviewResource = new ReviewResource();
        environment.jersey().register(reviewResource);

        final ReviewHealthCheck healthCheck = new ReviewHealthCheck();
        environment.healthChecks().register("template", healthCheck);
    }

}
