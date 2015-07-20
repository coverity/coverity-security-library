package com.coverity.security.sql;

import com.coverity.security.sql.test.MockConnection;
import com.coverity.security.sql.test.MockPreparedStatement;
import org.testng.annotations.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ParameterizedStatementTest {
    @Test
    public void testBasicParameterizedStatement() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        ParameterizedStatement.prepare(connection, "SELECT :columnName,name,cost FROM myschema.:tableName WHERE cost > ? ORDER BY :orderCol ASC")
                .setIdentifier("columnName", "foo")
                .setIdentifier("tableName", "bar")
                .setIdentifier("orderCol", "fizz")
                .prepareStatement()
                .close();

        MockPreparedStatement mockStmt = connection.getMockStatements().get(0);
        assertEquals(mockStmt.getSql(), "SELECT `foo`,name,cost FROM myschema.`bar` WHERE cost > ? ORDER BY `fizz` ASC");
    }

    @Test
    public void testUnsetParameter() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        ParameterizedStatement stmt = ParameterizedStatement.prepare(connection, "SELECT :columnName,name,cost FROM myschema.:tableName WHERE cost > ? ORDER BY :orderCol ASC")
                .setIdentifier("columnName", "foo")
                .setIdentifier("tableName", "bar");

        boolean exception = false;
        try {
            stmt.prepareStatement().close();
        } catch (Exception e) {
            exception = true;
            assertEquals(e.getClass(), SQLException.class);
            assertEquals(e.getMessage(), "Unset parameter: orderCol");
        }
        assertTrue(exception);
    }

    @Test
    public void testSetIdentifiers() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        boolean exception = false;
        try {
            ParameterizedStatement.prepare(connection, "SELECT :columnNames FROM foo")
                    .setIdentifiers("columnNames", new String[0])
                    .prepareStatement()
                    .close();
        } catch (Exception e) {
            exception = true;
            assertEquals(e.getClass(), IllegalArgumentException.class);
        }
        assertTrue(exception);

        ParameterizedStatement.prepare(connection, "SELECT :columnNames FROM foo")
                .setIdentifiers("columnNames", new String[]{"a", "b", "c"})
                .prepareStatement()
                .close();

        MockPreparedStatement mockStmt = connection.getMockStatements().get(0);
        assertEquals(mockStmt.getSql(), "SELECT `a`, `b`, `c` FROM foo");

        ParameterizedStatement.prepare(connection, "SELECT :columnNames FROM foo")
                .setIdentifiers("columnNames", Arrays.asList("a", "b", "c"))
                .prepareStatement()
                .close();

        mockStmt = connection.getMockStatements().get(1);
        assertEquals(mockStmt.getSql(), "SELECT `a`, `b`, `c` FROM foo");
    }
}
