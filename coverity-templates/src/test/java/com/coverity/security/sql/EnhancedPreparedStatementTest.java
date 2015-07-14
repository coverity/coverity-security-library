package com.coverity.security.sql;

import com.coverity.security.sql.test.ExceptionThrowingConnection;
import com.coverity.security.sql.test.MockConnection;
import com.coverity.security.sql.test.MockPreparedStatement;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EnhancedPreparedStatementTest {
    @Test
    public void testBasicEnhancedPreparedStatement() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT ?,name,cost FROM myschema.? WHERE cost > ? ORDER BY ? ASC");
        stmt.setIdentifier(1, "foo");
        stmt.setIdentifier(2, "bar");
        stmt.setInt(3, 100);
        stmt.setIdentifier(4, "fizz");
        stmt.executeQuery();

        MockPreparedStatement mockStmt = connection.getMockStatements().get(0);
        assertEquals(mockStmt.getSql(), "SELECT `foo`,name,cost FROM myschema.`bar` WHERE cost > ? ORDER BY `fizz` ASC");
        assertEquals(mockStmt.getParameter(1), new Object[] { 100 });
    }

    @Test
    public void testMultipleExecuteStmts() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE cost > ?");
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);
        stmt.execute();

        stmt.setIdentifier(1, "bar");
        stmt.execute();
        stmt.close();

        assertEquals(connection.getMockStatements().size(), 2);
        assertEquals(connection.getMockStatements().get(0).getSql(), "SELECT * FROM `foo` WHERE cost > ?");
        assertEquals(connection.getMockStatements().get(0).getParameter(1), new Object[] { 100 });
        assertTrue(connection.getMockStatements().get(0).isClosed());
        assertEquals(connection.getMockStatements().get(1).getSql(), "SELECT * FROM `bar` WHERE cost > ?");
        assertEquals(connection.getMockStatements().get(1).getParameter(1), new Object[] { 100 });
        assertTrue(connection.getMockStatements().get(1).isClosed());
    }

    @Test
    public void testMultipleQueryStmts() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE cost > ?");
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);
        stmt.executeQuery();

        stmt.setIdentifier(1, "bar");
        stmt.executeQuery();
        stmt.close();

        assertEquals(connection.getMockStatements().size(), 2);
        assertTrue(connection.getMockStatements().get(0).isClosed());
        assertTrue(connection.getMockStatements().get(1).isClosed());
    }

    @Test
    public void testMultipleUpdateStmts() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "UPDATE table SET ?=? WHERE id=?");
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);
        stmt.setInt(3, 101);
        stmt.executeUpdate();

        stmt.setIdentifier(1, "bar");
        stmt.executeUpdate();
        stmt.close();

        assertEquals(connection.getMockStatements().size(), 2);
        assertTrue(connection.getMockStatements().get(0).isClosed());
        assertTrue(connection.getMockStatements().get(1).isClosed());
    }

    @Test
    public void testUnsetParameters() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE id=?");
        stmt.setInt(2, 100);

        boolean exception = false;
        try {
            stmt.execute();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        assertTrue(connection.getMockStatements().get(0).isClosed());

        stmt.clearParameters();
        stmt.setIdentifier(1, "foo");
        exception = false;
        try {
            stmt.execute();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        assertTrue(connection.getMockStatements().get(1).isClosed());

        stmt.clearParameters();
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);
        stmt.execute();
        assertFalse(connection.getMockStatements().get(2).isClosed());
        stmt.close();
        assertTrue(connection.getMockStatements().get(2).isClosed());
    }

    @Test
    public void testCloseOnJdbcException() throws SQLException {
        ExceptionThrowingConnection connection = new ExceptionThrowingConnection("`", "#@");
        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE id=?");
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);

        boolean exception = false;
        try {
            stmt.execute();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);
        assertTrue(connection.getMockStatements().get(0).isClosed());
    }

    @Test
    public void testClearParameters() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, "SELECT * FROM ? WHERE id=?");
        stmt.setIdentifier(1, "foo");
        stmt.setInt(2, 100);
        stmt.execute();

        stmt.clearParameters();

        boolean exception = false;
        try {
            stmt.execute();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        stmt.setIdentifier(1, "bar");
        exception = false;
        try {
            stmt.execute();
        } catch (Exception e) {
            exception = true;
        }
        assertTrue(exception);

        stmt.setInt(2, 101);
        stmt.execute();

    }

}
