package net.javacrumbs.quarkus;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.time.Duration;
import java.util.Optional;

import static net.javacrumbs.quarkus.Utils.parseDuration;


@SchedulerLock(name = "?")
@Priority(3001)
@Interceptor
public class SchedulerLockInterceptor {
    private final LockingTaskExecutor lockingTaskExecutor;
    private final QuarkusLockConfigurationExtractor lockConfigurationExtractor;

    public SchedulerLockInterceptor(LockProvider lockProvider) {
        this.lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        String lockAtMostFor = getConfigValue("shedlock.defaults.lock-at-most-for");
        String lockAtLeastFor = getConfigValue("shedlock.defaults.lock-at-least-for");
        this.lockConfigurationExtractor = new QuarkusLockConfigurationExtractor(
            parseDuration(lockAtMostFor),
            lockAtLeastFor != null ? parseDuration(lockAtLeastFor) : Duration.ZERO
        );
    }

    private static String getConfigValue(String propertyName) {
        return ConfigProvider.getConfig().getConfigValue(propertyName).getValue();
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
