package com.coverity.security.sql.it;

public class H2DatabaseIT extends AbstractDatabaseIT{

    @Override
    protected String getJdbcConnectionString() {
        return "jdbc:h2:mem:test";
    }
    @Override
    protected String getConnectionUsername() {
        return "sa";
    }
    @Override
    protected String getConnectionPassword() {
        return "password";
    }
    @Override
    protected String getHibernateDialect() {
        return "org.hibernate.dialect.H2Dialect";
    }

}
