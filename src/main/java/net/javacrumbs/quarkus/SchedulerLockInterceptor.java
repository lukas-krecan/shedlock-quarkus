package net.javacrumbs.quarkus;

import net.javacrumbs.shedlock.core.DefaultLockingTaskExecutor;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.LockingTaskExecutor;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import java.util.Optional;

@SchedulerLock(name = "?")
@Priority(3001)
@Interceptor
public class SchedulerLockInterceptor {
    private final LockingTaskExecutor lockingTaskExecutor;
    private final QuarkusLockConfigurationExtractor lockConfigurationExtractor;

    public SchedulerLockInterceptor(LockProvider lockProvider, QuarkusLockConfigurationExtractor lockConfigurationExtractor) {
        this.lockingTaskExecutor = new DefaultLockingTaskExecutor(lockProvider);
        this.lockConfigurationExtractor = lockConfigurationExtractor; // use ConfigProvider.getConfig()
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
