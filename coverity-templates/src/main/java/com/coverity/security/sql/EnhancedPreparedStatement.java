package com.coverity.security.sql;

import java.sql.*;

/**
 * A drop-in replacement for PreparedStatements which allows for SQL identifiers to be parameterized. For example, the
 * following original code:
 *
 *   PreparedStatement stmt = conn.prepareStatement("SELECT MAX(" + colName + ") FROM mytable WHERE name=?");
 *   stmt.setString(1, "foo");
 *   ResultSet rs = stmt.executeQuery();
 *   ...
 *   rs.close();
 *   stmt.close();
 *
 * could be replaced with the following, which would safely parameterize the column name
 *
 *   EnhancedPrepradeStatement stmt = EnhancedPreparedStatement.prepareStatement(conn, "SELECT MAX(?) FROM mytable WHERE name=?");
 *   stmt.setIdentifier(1, colName);
 *   stmt.setString(2, "foo");
 *   ResultSet rs = stmt.executeQuery();
 *   ...
 *   rs.close();
 *   stmt.close();
 *
 * The EnhancedPreparedStatement uses metadata provided by the JDBC connection to understand how to quote identifiers,
 * so it is safe to use with any JDBC-compatible backend. Note that not all strings are allowed as identifiers; for
 * example, MySQL uses the backtick ` to quote identifiers, so identifiers cannot contain this character. The
 * EnhancedPreparedStatement will throw an IllegalArgumentException in the case that such illegal strings are used.
 *
 * The EnhancedPreparedStatement is a facade which remembers the parameters which are set on it and lazily prepares
 * an underlying JDBC prepared statement when it is time to actually execute the query. This has a number of
 * implications:
 *
 * * There is associated memory overhead with the EnhancedPreparedStatement remembering values passed to it. This
 *   shouldn't generally be a concern unless your application is building many EnhancedPreparedStatements
 *   simultaneously.
 * * Because the underlying JDBC implementation needs to recompile the query every identifiers change, the
 *   EnhancedPreparedStatement class builds a new JDBC PreparedStatement every time it is executed. If you are executing
 *   the same or similar queries many times, your performance may suffer. Consider using the ParameterizedStatement
 *   class in this case.
 * * Many of the advanced methods of the PreparedStatement class (such as batch commands or fetching column metadata
 *   before executing the query) are not supported, and any calls to those methods will result in a
 *   SQLFeatureNotSupportedException. Again, if these features are desired, consider using the ParameterizedStatement
 *   class.
 *
 */
public class EnhancedPreparedStatement extends MemoryPreparedStatement implements PreparedStatement {

    private final String[] queryParts;
    private final String[] identifiers;
    private final Connection conn;
    private PreparedStatement lastUsedStmt;
    private final IdentifierEscaper identifierEscaper;

    private EnhancedPreparedStatement(final Connection conn, final String[] queryParts) throws SQLException {
        super(queryParts.length-1);
        this.queryParts = queryParts;
        this.identifiers = new String[queryParts.length-1];
        this.conn = conn;
        this.lastUsedStmt = null;
        this.identifierEscaper = new IdentifierEscaper(conn);
    }

    /**
     * Creates an EnhancedPreparedStatement instance using the provided template string. Like a normal
     * PreparedStatement, the return statement must be closed() to avoid resource leaks.
     *
     * @param conn The JDBC connection.
     * @param sql The template string, which may use "?" placeholders for SQL identifiers in addition to the usual SQL
     *            data values in a normal PreparedStatement.
     * @return The EnhancedPreparedStatement instance.
     *
     * @throws SQLException If there is a problem fetching relevant metadata (necessary for quoting and/or validating
     * identifier strings) from the JDBC connection.
     */
    public static EnhancedPreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
        // TODO: Do proper tokenizing and parsing instead of regex matching
        EnhancedPreparedStatement result = new EnhancedPreparedStatement(conn, sql.split("\\?", -1));
        return result;
    }

    private PreparedStatement buildPreparedStatement() throws SQLException {
        StringBuilder sb = new StringBuilder(queryParts[0]);

        for (int i = 0; i < queryParts.length-1; i++) {
            if (identifiers[i] != null) {
                sb.append(identifierEscaper.escapeIdentifier(identifiers[i]));
            } else {
                sb.append('?');
            }

            sb.append(queryParts[i+1]);
        }

        PreparedStatement stmt = conn.prepareStatement(sb.toString());
        try {
            int paramIdx = 0;
            for (int i = 0; i < queryParts.length - 1; i++) {
                if (identifiers[i] == null) {
                    super.apply(stmt, i + 1, ++paramIdx);
                }
            }
        } catch (SQLException e) {
            stmt.close();
            throw e;
        } catch (RuntimeException e) {
            stmt.close();
            throw e;
        }

        return stmt;
    }

    /**
     * Sets the parameter to use the provided identifier. If the identifier is invalid (either because it is not a
     * valid identifier in the database's schema, or because it contains invalid characters), no exception will be
     * thrown until the query is actually executed.
     *
     * @param parameterIndex The index of the "?" placeholder to which this identifier applies, number from 1.
     * @param identifier The identifier. This string should not be quoted.
     */
    public void setIdentifier(int parameterIndex, String identifier) {
        identifiers[parameterIndex - 1] = identifier;
    }

    @Override
    public boolean execute() throws SQLException {
        if (lastUsedStmt != null) {
            lastUsedStmt.close();
        }
        lastUsedStmt = buildPreparedStatement();
        return lastUsedStmt.execute();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if (lastUsedStmt != null) {
            lastUsedStmt.close();
        }
        lastUsedStmt = buildPreparedStatement();
        return lastUsedStmt.executeQuery();
    }

    @Override
    public int executeUpdate() throws SQLException {
        if (lastUsedStmt != null) {
            lastUsedStmt.close();
        }
        lastUsedStmt = buildPreparedStatement();
        return lastUsedStmt.executeUpdate();
    }

    @Override
    public void clearParameters() {
        for (int i = 0; i < identifiers.length; i++) {
            identifiers[i] = null;
        }
        super.clearParameters();
    }

    @Override
    public void close() throws SQLException {
        lastUsedStmt.close();
    }

    // Unsupported methods

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void addBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    // Unsupported java.sql.Statement methods

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMaxFieldSize() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setMaxFieldSize(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getMaxRows() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setMaxRows(int max) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setEscapeProcessing(boolean enable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getQueryTimeout() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setQueryTimeout(int seconds) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void cancel() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setCursorName(String name) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getUpdateCount() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getMoreResults() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetConcurrency() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetType() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void addBatch(String sql) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void clearBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int[] executeBatch() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public Connection getConnection() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean getMoreResults(int current) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public int getResultSetHoldability() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isClosed() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void setPoolable(boolean poolable) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isPoolable() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public void closeOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
}
