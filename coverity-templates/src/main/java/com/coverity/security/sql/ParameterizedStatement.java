package com.coverity.security.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An templated SQL statement that allows for safely setting SQL identifiers. Once the identifiers have been set, you
 * may call the prepareStatement() method which will return a JDBC PreparedStatement which can be used as usual.
 *
 * For example, the following original code:
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
 *   ParameterizedStatement pStmt = ParameterizedStatement.prepare(conn, "SELECT MAX(:theColName) FROM mytable WHERE name=?");
 *   pStmt.setIdentifier("theColName", colName);
 *   PreparedStatement stmt = stmt.prepareStatement();
 *   pStmt.setString(1, "foo");
 *   ResultSet rs = stmt.executeQuery();
 *   ...
 *   rs.close();
 *   stmt.close();
 *
 * Note that parameters take the place of entire identifiers. For example, the following would throw an exception
 * because it represents invalid SQL syntax.
 *
 *   ParameterizedStatement pStmt = ParameterizedStatement.prepare(conn, "SELECT * FROM :tablePrefix_myTable");
 *   pStmt.setIdentifier("tablePrefix", "table_prefix");
 *   PreparedStatement stmt = stmt.prepareStatement(); // Exception thrown here
 *
 * Instead the entire table name should be used as a parameter thusly:
 *
 *   ParameterizedStatement pStmt = ParameterizedStatement.prepare(conn, "SELECT * FROM :tableName");
 *   pStmt.setIdentifier("tableName", "table_prefix" + "_myTable");
 *   PreparedStatement stmt = stmt.prepareStatement();
 *   ...
 *
 */
public class ParameterizedStatement {

    private static final Pattern PARSER_PATTERN = Pattern.compile(":[a-zA-Z0-9]+");

    private final Connection connection;
    private final String[] sqlPieces;
    private final String[] parameters;
    private final Map<String, String> parameterValues = new HashMap<String, String>();
    private final IdentifierEscaper identifierEscaper;

    private ParameterizedStatement(Connection connection, String[] sqlPieces, String[] parameters) throws SQLException {
        this.connection = connection;
        this.sqlPieces = sqlPieces;
        this.parameters = parameters;
        this.identifierEscaper = new IdentifierEscaper(connection);
    }

    /**
     * Creates a ParameterizedStatement instance using the supplied template string. The template string should be a
     * valid PreparedStatement query string (i.e. with "?" placeholder values), with the addition of named parameters
     * which represent placeholders for SQL identifiers. Named parameters are represented by a colon ":" followed by
     * one or more alphanumeric characters, i.e. ":fooBar1234". Other characters (such as punctuation, hyphens, and
     * underscores) are not allowed in parameter names and will be interpreted as the end of the end of the identifier
     * (e.g. in "SELECT :foo-10" would be syntatically equivalent to "SELECT :foo - 10"). Named parameters may be
     * repeated, in which case all such parameters will use the same identifier value.
     *
     * For example, you can parameterize the schema name with:
     *
     *   ParameterizedStatement pStmt = ParameterizedStatement.prepare(conn, "SELECT * FROM :schemaName.myTable T INNER JOIN :schemaName.otherTable U ON U.id=T.id");
     *   pStmt.setIdentifier("schemaName", "mySchemaName");
     *   PreparedStatement stmt = pStmt.prepareStatement();
     *   ...
     *
     * @param connection The JDBC connection for the query.
     * @param sql The PreparedStatement template string, as described above.
     * @return The ParameterizedStatement instance. Unlike a PreparedStatement, this object has no resources which need
     * to be freed, so it does not need to be closed.
     * @throws SQLException Thrown if there is an exception thrown by the JDBC connection when this class tries to fetch
     * relevant metadata from the connection.
     */
    public static ParameterizedStatement prepare(Connection connection, String sql) throws SQLException {
        // TODO: Do proper tokenizing and parsing instead of regex matching
        final Matcher matcher = PARSER_PATTERN.matcher(sql);
        int start = 0;
        final List<String> sqlPieces = new ArrayList<String>();
        final List<String> parameters = new ArrayList<String>();
        while (matcher.find(start)) {
            final String parameter = matcher.group().substring(1);
            sqlPieces.add(sql.substring(start, matcher.start()));
            parameters.add(parameter);
            start = matcher.end();
        }
        if (start < sql.length()) {
            sqlPieces.add(sql.substring(start));
        }
        return new ParameterizedStatement(connection,
                sqlPieces.toArray(new String[sqlPieces.size()]),
                parameters.toArray(new String[parameters.size()]));
    }

    /**
     * Sets the identifier on the query string.
     *
     * @param parameterName The name of the identifier placeholder from the template string (without the leading colon).
     * @param parameterValue The value of the identifier to use in the query. If this identifier is invalid (either
     *                       because it is not a valid identifier in the database schema or because it contains an
     *                       invalid character), an IllegalArgumentException will be thrown.
     * @return This object; useful for chaining calls to this object's methods.
     */
    public ParameterizedStatement setIdentifier(String parameterName, String parameterValue) {
        identifierEscaper.validateIdentifier(parameterValue);
        parameterValues.put(parameterName, identifierEscaper.escapeIdentifier(parameterValue));
        return this;
    }

    /**
     * Sets the parameter on the query string as a comma-separated list of identifiers.
     *
     * @param parameterName The name of the identifier placeholder from the template string (without the leading colon).
     * @param paramValues The array value of the identifier values to be used in the query. If any identifier is
     *                       invalid (either because it is not a valid identifier in the database schema or because it
     *                       contains an invalid character), an IllegalArgumentException will be thrown.
     * @return This object; useful for chaining calls to this object's methods.
     */
    public ParameterizedStatement setIdentifiers(String parameterName, String[] paramValues) {
        if (paramValues.length == 0) {
            throw new IllegalArgumentException("Identifier list cannot be empty.");
        }
        identifierEscaper.validateIdentifier(paramValues[0]);
        final StringBuilder sb = new StringBuilder().append(identifierEscaper.escapeIdentifier(paramValues[0]));
        for (int i = 1; i < paramValues.length; i++) {
            identifierEscaper.validateIdentifier(paramValues[i]);
            sb.append(", ").append(identifierEscaper.escapeIdentifier(paramValues[i]));
        }
        parameterValues.put(parameterName, sb.toString());
        return this;
    }

    /**
     * Sets the parameter on the query string as a comma-separated list of identifiers. This is a convenience method;
     * it is equivalent to calling setIdentifiers(parameterName, paramValues.toArray(new String[0]));
     *
     * @param parameterName The name of the identifier placeholder from the template string (without the leading colon).
     * @param paramValues The collection of identifier values to be used in the query. If any identifier is
     *                       invalid (either because it is not a valid identifier in the database schema or because it
     *                       contains an invalid character), an IllegalArgumentException will be thrown.
     * @return This object; useful for chaining calls to this object's methods.
     */
    public ParameterizedStatement setIdentifiers(String parameterName, Collection<String> paramValues) {
        return setIdentifiers(parameterName, paramValues.toArray(new String[paramValues.size()]));
    }

    /**
     * Returns a PreparedStatement instance using the identifiers previously set on this object. If any of the
     * identifiers are invalid (because they contain illegal characters according to the JDBC connection), an exception
     * will be thrown.
     *
     * @return An instance of a JDBC PreparedStatement. This object should be closed as with any usual
     * PreparedStatement.
     * @throws SQLException Thrown if the JDBC connection cannot compile the query using the identifiers previously
     * set on this object.
     */
    public PreparedStatement prepareStatement() throws SQLException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (!parameterValues.containsKey(parameters[i])) {
                throw new SQLException("Unset parameter: " + parameters[i]);
            }
            final String paramValue = parameterValues.get(parameters[i]);
            sb.append(sqlPieces[i]).append(paramValue);
        }
        for (int i = parameters.length; i < sqlPieces.length; i++) {
            sb.append(sqlPieces[i]);
        }
        return connection.prepareStatement(sb.toString());
    }

}
