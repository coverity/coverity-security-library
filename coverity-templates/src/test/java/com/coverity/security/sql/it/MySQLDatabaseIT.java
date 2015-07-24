package com.coverity.security.sql.it;

public class MySQLDatabaseIT extends AbstractDatabaseIT{

    @Override
    protected String getJdbcConnectionString() {
        return "jdbc:mysql://localhost:3306/test";
    }
    @Override
    protected String getConnectionUsername() {
        return "dev";
    }
    @Override
    protected String getConnectionPassword() {
        return "password";
    }
    @Override
    protected String getHibernateDialect() {
        return "org.hibernate.dialect.MySQLDialect";
    }

}
