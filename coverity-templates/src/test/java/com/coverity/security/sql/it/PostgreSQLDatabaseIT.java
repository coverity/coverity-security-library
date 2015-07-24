package com.coverity.security.sql.it;

public class PostgreSQLDatabaseIT extends AbstractDatabaseIT{

    @Override
    protected String getJdbcConnectionString() {
        return "jdbc:postgresql://localhost:5432/test";
    }
    @Override
    protected String getConnectionUsername() {
        return "postgres";
    }
    @Override
    protected String getConnectionPassword() {
        return null;
    }
    @Override
    protected String getHibernateDialect() {
        return "org.hibernate.dialect.PostgreSQL81Dialect";
    }

}
