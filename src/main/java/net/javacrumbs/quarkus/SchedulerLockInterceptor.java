package net.javacrumbs.quarkus;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Optional;

import static io.quarkus.runtime.configuration.DurationConverter.parseDuration;

@SchedulerLock(name = "?")
@Priority(3001)
@Interceptor
public class SchedulerLockInterceptor {
    private final DefaultLockingTaskExecutor lockingTaskExecutor;
    private final QuarkusLockConfigurationExtractor lockConfigurationExtractor;

    @ConfigProperty(name = "shedlock.defaults.lock-at-most-for")
    String defaultLockAtMostForString;

    @ConfigProperty(name = "shedlock.defaults.lock-at-least-for")
    String defaultLockAtLeastForString;

    @Inject
    public SchedulerLockInterceptor(LockProvider lockProvider) {
        lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        lockConfigurationExtractor = new QuarkusLockConfigurationExtractor(parseDuration(defaultLockAtMostForString), parseDuration(defaultLockAtLeastForString));
    }

    @AroundInvoke
    Object lock(InvocationContext context) throws Throwable {
        Class<?> returnType = context.getMethod().getReturnType();
        if (!void.class.equals(returnType) && !Void.class.equals(returnType)) {
            throw new LockingNotSupportedException();
        }

        Optional<LockConfiguration> lockConfiguration = lockConfigurationExtractor.getLockConfiguration(context.getMethod());
        if (lockConfiguration.isPresent()) {
            lockingTaskExecutor.executeWithLock((LockingTaskExecutor.Task) context::proceed, lockConfiguration.get());
            return null;
        } else {
            return context.proceed();
        }
    }
}
