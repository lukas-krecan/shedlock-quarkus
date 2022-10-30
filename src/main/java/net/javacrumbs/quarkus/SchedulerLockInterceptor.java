package net.javacrumbs.quarkus;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.time.Duration;
import java.util.Optional;

@SchedulerLock
@Priority(3001)
@Interceptor
public class SchedulerLockInterceptor {
    private final DefaultLockingTaskExecutor lockingTaskExecutor;
    private final QuarkusLockConfigurationExtractor lockConfigurationExtractor;

    @Inject
    public SchedulerLockInterceptor(LockProvider lockProvider) {
        lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        lockConfigurationExtractor = new QuarkusLockConfigurationExtractor(Duration.ofSeconds(10), Duration.ofSeconds(10), null);
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
