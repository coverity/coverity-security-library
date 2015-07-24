package com.coverity.security.sql.it;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.sql.SQLException;

public class SQLiteDatabaseIT extends AbstractDatabaseIT{

    private File tmpFile;

    @Override
    @BeforeClass
    public void setup() throws Exception {
        tmpFile = File.createTempFile("csl", "sqlite");
        super.setup();
    }

    @Override
    @AfterClass
    public void teardown() throws SQLException {
        super.teardown();
        tmpFile.delete();
    }

    @Override
    protected String getJdbcConnectionString() {
        return "jdbc:sqlite:" + tmpFile.getAbsolutePath();
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
        return "com.coverity.security.sql.it.SQLiteDialect";
    }

}
