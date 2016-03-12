package com.coverity.security.sql;

import com.coverity.security.sql.ParameterizedStatement;
import com.coverity.security.sql.test.MockConnection;
import com.coverity.security.sql.test.MockPreparedStatement;
import org.testng.annotations.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class QueryBuilderTest {
    @Test
    public void testBuildPreparedStatement() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        QueryBuilder qb = new QueryBuilder(connection);
        qb.append("SELECT * FROM foo ");
        qb.append("WHERE name=?");
        PreparedStatement stmt = qb.buildPreparedStatement();
        stmt.setString(1, "bar");
        stmt.executeQuery();
        stmt.close();

        MockPreparedStatement mockStmt = connection.getMockStatements().get(0);
        assertEquals(mockStmt.getSql(), "SELECT * FROM foo WHERE name=?");
    }

    @Test
    public void testBuildEnhancedPreparedStatement() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        QueryBuilder qb = new QueryBuilder(connection, "SELECT ?,name,cost FROM myschema.? ");
        qb.append("WHERE cost > ? ").append("ORDER BY ? ASC");
        EnhancedPreparedStatement stmt = qb.buildEnhancedPreparedStatement();
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
    public void testBuildParameterizedStatement() throws SQLException {
        MockConnection connection = new MockConnection("`", "#@");

        QueryBuilder qb = new QueryBuilder(connection, "SELECT :columnName,name,cost ")
            .append("FROM myschema.:tableName ")
            .append("WHERE cost > ? ORDER BY :orderCol ASC");
        qb.buildParameterizedStatement()
                .setIdentifier("columnName", "foo")
                .setIdentifier("tableName", "bar")
                .setIdentifier("orderCol", "fizz")
                .prepareStatement()
                .close();

        MockPreparedStatement mockStmt = connection.getMockStatements().get(0);
        assertEquals(mockStmt.getSql(), "SELECT `foo`,name,cost FROM myschema.`bar` WHERE cost > ? ORDER BY `fizz` ASC");
    }

}
