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
    public void initialize(final Bootstrap<game_apiConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final game_apiConfiguration configuration,
                    final Environment environment) {

        RedisHelper.setRedisHosts(configuration.getRedisHosts());
        RedisHelper.setRedisHost(configuration.getRedisHost());

        final ReviewResource reviewResource = new ReviewResource();
        environment.jersey().register(reviewResource);

        final HelloWorldResource resource = new HelloWorldResource(
                configuration.getTemplate(),
                configuration.getDefaultName()
        );
        environment.jersey().register(resource);
        final TemplateHealthCheck healthCheck =
                new TemplateHealthCheck(configuration.getTemplate());
        environment.healthChecks().register("template", healthCheck);
        environment.jersey().register(resource);
    }

}
