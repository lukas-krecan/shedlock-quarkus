package net.javacrumbs.quarkus;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.inject.Produces;

import static io.quarkus.runtime.configuration.DurationConverter.parseDuration;
 class SchedulerLockConfiguration {
    @ConfigProperty(name = "shedlock.defaults.lock-at-most-for")
    String defaultLockAtMostForString;

    @ConfigProperty(name = "shedlock.defaults.lock-at-least-for", defaultValue = "0")
    String defaultLockAtLeastForString;


    // Have to do it this way as I am not able to use PostConstruct in the inteceptor and it's not possible to constructor inject
    // the ConfigProperties
    @Produces
    QuarkusLockConfigurationExtractor lockConfigurationExtractor() {
        return new QuarkusLockConfigurationExtractor(
            parseDuration(defaultLockAtMostForString),
            parseDuration(defaultLockAtLeastForString)
        );
    }

}
