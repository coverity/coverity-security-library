package com.coverity.security.sql;

import com.google.errorprone.annotations.CompileTimeConstant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>Used to construct statements (PreparedStatement, EnhancedPreparedStatement, or ParameterizedStatement) in a way that
 * allows them to be dynamically built but enforces the safe use of @CompileTimeConstant pieces. For example:</p>
 *
 * <p><code>QueryBuilder qb = new QueryBuilder("SELECT * FROM foo ");<br>
 *     if (name != null) {<br>
 *     &nbsp;&nbsp;&nbsp;&nbsp;qb.append("WHERE name=? ");<br>
 *     }<br>
 *     if (orderBy != null) {<br>
 *     &nbsp;&nbsp;&nbsp;&nbsp;qb.append("ORDER BY ? ");<br>
 *     }<br>
 *     EnhancedPreparedStatement stmt = qb.buildEnhancedPreparedStatement();<br>
 *     int index = 1;<br>
 *     if (name != null) {<br>
 *     &nbsp;&nbsp;&nbsp;&nbsp;stmt.setString(index++, name);<br>
 *     }<br>
 *     if (orderBy != null) {<br>
 *     &nbsp;&nbsp;&nbsp;&nbsp;stmt.setIdentifier(index++, orderBy);<br>
 *     }<br>
 *     ResultSet rs = stmt.executeQuery();<br>
 *     // ...</code></p>
 *
 */
public class QueryBuilder {
    private final Connection connection;
    private final StringBuilder query;

    /**
     * Creates a QueryBuilder with an initially empty query.
     *
     * @param connection Connection against which the statement will be created.
     */
    public QueryBuilder(Connection connection) {
        this.connection = connection;
        this.query = new StringBuilder();
    }

    /**
     * Creates a QueryBuilder with the provided initial query string.
     *
     * @param connection Connection against which the statement will be created.
     * @param queryPart The initial query string.
     */
    public QueryBuilder(Connection connection, @CompileTimeConstant final String queryPart) {
        this.connection = connection;
        this.query = new StringBuilder(queryPart);
    }

    /**
     * Appends the given string to the query string. This method returns this same QueryBuilder instance so that
     * calls to it may be chained.
     *
     * @param queryPart The query string to append.
     * @return This QueryBuilder instance.
     */
    public QueryBuilder append(@CompileTimeConstant final String queryPart) {
        this.query.append(queryPart);
        return this;
    }

    /**
     * Returns a PreparedStatement based on the current query string.
     *
     * @return The PreparedStatement, which should be closed by the caller.
     * @throws SQLException if the underlying connection throws an exception while trying to build the query.
     */
    public PreparedStatement buildPreparedStatement() throws SQLException {
        return connection.prepareStatement(query.toString());
    }

    /**
     * Returns an EnhancedPreparedStatement based on the current query string.
     *
     * @return The EnhancedPreparedStatement, which should be closed by the caller.
     * @throws SQLException if the underlying connection throws an exception while trying to build the query string.
     */
    public EnhancedPreparedStatement buildEnhancedPreparedStatement() throws SQLException {
        @SuppressWarnings("CompileTimeConstant")
        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(connection, query.toString());
        return stmt;
    }

    /**
     * Returns an ParameterizedStatement based on the current query string.
     *
     * @return The ParameterizedStatement, which should be closed by the caller.
     * @throws SQLException if the underlying connection throws an exception while trying to build the query string.
     */
    public ParameterizedStatement buildParameterizedStatement() throws SQLException {
        @SuppressWarnings("CompileTimeConstant")
        ParameterizedStatement stmt = ParameterizedStatement.prepare(connection, query.toString());
        return stmt;
    }

}
