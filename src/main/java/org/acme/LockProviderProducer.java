package org.acme;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbc.JdbcLockProvider;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.sql.DataSource;

public class LockProviderProducer {
    @Inject
    DataSource dataSource;

    @Produces
    public LockProvider lockProvider() {
        return new JdbcLockProvider(dataSource);
    }
}
