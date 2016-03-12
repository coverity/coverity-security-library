package com.coverity.security.sql;

import com.google.errorprone.annotations.CompileTimeConstant;

import java.sql.*;
import java.util.Collection;

/**
 * <p>A drop-in replacement for <code>PreparedStatements</code> which allows for SQL identifiers to be parameterized. For example, the
 * following original code:</p>
 *
 * <p><code>PreparedStatement stmt = conn.prepareStatement("SELECT MAX(" + colName + ") FROM mytable WHERE name=?");<br/>
 *   stmt.setString(1, "foo");<br/>
 *   ResultSet rs = stmt.executeQuery();<br/>
 *   ...<br/>
 *   rs.close();<br/>
 *   stmt.close();</code></p>
 *
 * <p>could be replaced with the following, which would safely parameterize the column name</p>
 *
 * <p><code>EnhancedPrepradeStatement stmt = EnhancedPreparedStatement.prepareStatement(conn, "SELECT MAX(?) FROM mytable WHERE name=?");<br/>
 *   stmt.setIdentifier(1, colName);<br/>
 *   stmt.setString(2, "foo");<br/>
 *   ResultSet rs = stmt.executeQuery();<br/>
 *   ...<br/>
 *   rs.close();<br/>
 *   stmt.close();</code></p>
 *
 * <p>The <code>EnhancedPreparedStatement</code> uses metadata provided by the JDBC connection to understand how to quote identifiers,
 * so it is safe to use with any JDBC-compatible backend. Note that not all strings are allowed as identifiers; for
 * example, MySQL uses the backtick ` to quote identifiers, so identifiers cannot contain this character. The
 * <code>EnhancedPreparedStatement</code> will throw an <code>IllegalArgumentException</code> in the case that such illegal strings are used.</p>
 *
 * <p>The <code>EnhancedPreparedStatement</code> is a facade which remembers the parameters which are set on it and lazily prepares
 * an underlying JDBC prepared statement when it is time to actually execute the query. This has a number of
 * implications:</p>
 *
 * <ul>
 *   <li>There is associated memory overhead with the <code>EnhancedPreparedStatement</code> remembering values passed to it. This
 *   shouldn't generally be a concern unless your application is building many <code>EnhancedPreparedStatements</code>
 *   simultaneously.</li>
 *   <li>Because the underlying JDBC implementation needs to recompile the query every identifiers change, the
 *   <code>EnhancedPreparedStatement</code> class builds a new JDBC <code>PreparedStatement</code> every time it is executed.
 *   If you are executing the same or similar queries many times, your performance may suffer. Consider using the
 *   {@link ParameterizedStatement} class in this case.</li>
 *   <li>Many of the advanced methods of the <code>PreparedStatement</code> class (such as batch commands or fetching column metadata
 *   before executing the query) are not supported, and any calls to those methods will result in a
 *   <code>SQLFeatureNotSupportedException</code>. Again, if these features are desired, consider using the {@link ParameterizedStatement}
 *   class.</li>
 * </ul>
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
     * Creates an <code>EnhancedPreparedStatement</code> instance using the provided template string. Like a normal
     * <code>PreparedStatement</code>, the return statement must be <code>closed()</code> to avoid resource leaks.
     *
     * @param conn The JDBC connection.
     * @param sql The template string, which may use <code>"?"</code> placeholders for SQL identifiers in addition to the usual SQL
     *            data values in a normal PreparedStatement.
     * @return The <code>EnhancedPreparedStatement</code> instance.
     *
     * @throws SQLException If there is a problem fetching relevant metadata (necessary for quoting and/or validating
     * identifier strings) from the JDBC connection.
     */
    public static EnhancedPreparedStatement prepareStatement(Connection conn, @CompileTimeConstant final String sql) throws SQLException {
        // TODO: Do proper tokenizing and parsing instead of regex matching
        EnhancedPreparedStatement result = new EnhancedPreparedStatement(conn, sql.split("\\?", -1));
        return result;
    }

    private PreparedStatement buildPreparedStatement() throws SQLException {
        StringBuilder sb = new StringBuilder(queryParts[0]);

        for (int i = 0; i < queryParts.length-1; i++) {
            if (identifiers[i] != null) {
                sb.append(identifiers[i]);
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
     * @param parameterIndex The index of the <code>"?"</code> placeholder to which this identifier applies, number from 1.
     * @param identifier The identifier. This string should not be quoted.
     */
    public void setIdentifier(int parameterIndex, String identifier) {
        identifierEscaper.validateIdentifier(identifier);
        identifiers[parameterIndex - 1] = identifierEscaper.escapeIdentifier(identifier);
    }

    /**
     * Sets the parameter to use the provided identifiers as a comma-separated list. If any identifier is invalid
     * (either because it is not a valid identifier in the database's schema, or because it contains invalid
     * characters), an <code>IllegalArgumentException</code> will be thrown.
     *
     * @param parameterIndex The index of the <code>"?"</code> placeholder to which this identifier applies, number from 1.
     * @param identifiers The identifiers. These strings should not be quoted.
     */
    public void setIdentifiers(int parameterIndex, String[] identifiers) {
        if (identifiers.length == 0) {
            throw new IllegalArgumentException("Identifier list cannot be empty.");
        }
        identifierEscaper.validateIdentifier(identifiers[0]);
        final StringBuilder sb = new StringBuilder().append(identifierEscaper.escapeIdentifier(identifiers[0]));
        for (int i = 1; i < identifiers.length; i++) {
            identifierEscaper.validateIdentifier(identifiers[i]);
            sb.append(", ").append(identifierEscaper.escapeIdentifier(identifiers[i]));
        }
        this.identifiers[parameterIndex - 1] = sb.toString();
    }

    /**
     * Sets the parameter to use the provided identifiers as a comma-separated list. If any identifier is invalid
     * (either because it is not a valid identifier in the database's schema, or because it contains invalid
     * characters), an <code>IllegalArgumentException</code> will be thrown.
     *
     * This is a convenience method, which is equivalent to calling <code>setIdentifiers(parameterIndex, identifiers.toArray(new String[0]));</code>
     *
     * @param parameterIndex The index of the <code>"?"</code> placeholder to which this identifier applies, number from 1.
     * @param identifiers The identifiers. These strings should not be quoted.
     */
    public void setIdentifiers(int parameterIndex, Collection<String> identifiers) {
        setIdentifiers(parameterIndex, identifiers.toArray(new String[identifiers.size()]));
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
        if (lastUsedStmt != null) {
            lastUsedStmt.close();
        }
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
