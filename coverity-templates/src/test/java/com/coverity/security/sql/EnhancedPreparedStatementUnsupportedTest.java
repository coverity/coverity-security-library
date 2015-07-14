package com.coverity.security.sql;

import com.coverity.security.sql.test.MockConnection;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * Tests that all of the unsupported methods throw a SQLFeatureNotSupportedException
 */
public class EnhancedPreparedStatementUnsupportedTest {

    EnhancedPreparedStatement stmt;

    @BeforeClass
    public void setup() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");
        stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE id=?");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetParameterMetaData() throws SQLException {
        stmt.getParameterMetaData();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testAddBatch() throws SQLException {
        stmt.addBatch();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetMetaData() throws SQLException {
        stmt.getMetaData();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteQuerySql() throws SQLException {
        stmt.executeQuery("foo");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteUpdateSql() throws SQLException {
        stmt.executeUpdate("foo");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetMaxFieldSize() throws SQLException {
        stmt.getMaxFieldSize();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetMaxFieldSize() throws SQLException {
        stmt.setMaxFieldSize(10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetMaxRows() throws SQLException {
        stmt.getMaxRows();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetMaxRows() throws SQLException {
        stmt.setMaxRows(10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetEscapeProcessing() throws SQLException {
        stmt.setEscapeProcessing(true);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetQueryTimeout() throws SQLException {
        stmt.getQueryTimeout();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetQueryTimeout() throws SQLException {
        stmt.setQueryTimeout(10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testCancel() throws SQLException {
        stmt.cancel();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetWarnings() throws SQLException {
        stmt.getWarnings();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testClearWarnings() throws SQLException {
        stmt.clearWarnings();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetCursorName() throws SQLException {
        stmt.setCursorName("foo");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteSql() throws SQLException {
        stmt.execute("foo");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetResultSet() throws SQLException {
        stmt.getResultSet();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetUpdateCount() throws SQLException {
        stmt.getUpdateCount();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetMoreResults() throws SQLException {
        stmt.getMoreResults();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetFetchDirection() throws SQLException {
        stmt.setFetchDirection(1);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetFetchDirection() throws SQLException {
        stmt.getFetchDirection();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetFetchSize() throws SQLException {
        stmt.setFetchSize(10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetFetchSize() throws SQLException {
        stmt.getFetchSize();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetResultSetConcurrency() throws SQLException {
        stmt.getResultSetConcurrency();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetResultSetType() throws SQLException {
        stmt.getResultSetType();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testAddBatchSql() throws SQLException {
        stmt.addBatch("foo");
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testClearBatch() throws SQLException {
        stmt.clearBatch();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteBatch() throws SQLException {
        stmt.executeBatch();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetConnection() throws SQLException {
        stmt.getConnection();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetMoreResultsInt() throws SQLException {
        stmt.getMoreResults(10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetGeneratedKeys() throws SQLException {
        stmt.getGeneratedKeys();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteUpdateAutoGen() throws SQLException {
        stmt.executeUpdate("foo", 10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteUpdateColumnIndices() throws SQLException {
        stmt.executeUpdate("foo", new int[] { 10 });
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteUpdateColumnNames() throws SQLException {
        stmt.executeUpdate("foo", new String[] { "foo", "bar" });
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteAutoGen() throws SQLException {
        stmt.execute("foo", 10);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteColumnIndices() throws SQLException {
        stmt.execute("foo", new int[] { 10 });
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testExecuteColumnNames() throws SQLException {
        stmt.execute("foo", new String[] {"foo", "bar"});
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testGetResultSetHoldability() throws SQLException {
        stmt.getResultSetHoldability();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testIsClosed() throws SQLException {
        stmt.isClosed();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testSetPoolable() throws SQLException {
        stmt.setPoolable(false);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testIsPoolable() throws SQLException {
        stmt.isPoolable();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testCloseOnCompletion() throws SQLException {
        stmt.closeOnCompletion();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testIsCloseOnCompletion() throws SQLException {
        stmt.isCloseOnCompletion();
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testUnwrap() throws SQLException {
        stmt.unwrap(null);
    }

    @Test(expectedExceptions = SQLFeatureNotSupportedException.class)
    public void testIsWrapperFor() throws SQLException {
        stmt.isWrapperFor(null);
    }
}
