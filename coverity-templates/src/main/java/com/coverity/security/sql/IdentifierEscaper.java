package com.coverity.security.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Internal class used to validate and escape identifiers for queries using JDBC connections.
 */
class IdentifierEscaper {
    private final String identifierQuoteString;
    private final char[] extraNameChars;

    public IdentifierEscaper(final Connection connection) throws SQLException {
        final DatabaseMetaData dbMetaData = connection.getMetaData();
        String quoteStr = dbMetaData.getIdentifierQuoteString();
        if (!quoteStr.equals(" ")) {
            identifierQuoteString = quoteStr;
        } else {
            identifierQuoteString = null;
        }
        extraNameChars = dbMetaData.getExtraNameCharacters().toCharArray();
    }

    /**
     * Validates that the identifier is legal according to the underlying JDBC implementation. Throws an
     * IllegalArgumentException if not.
     *
     * @param identifier The identifier to be validated.
     */
    public void validateIdentifier(final String identifier) {
        if (identifierQuoteString != null) {
            if (identifier.contains(identifierQuoteString)) {
                throw new IllegalArgumentException("Identifier cannot contain quote string: " + identifierQuoteString);
            }
            return;
        } else {
            final char[] identifierChars = identifier.toCharArray();
            for (int i = 0; i < identifierChars.length; i++) {
                final char c = identifierChars[i];
                if ((c < 'a' || c > 'z')
                        && (c < 'A' || c > 'Z')
                        && (c < '0' || c > '9')
                        && (c != '_')) {

                    boolean allowed = false;
                    for (int j = 0; j < extraNameChars.length; j++) {
                        if (c == extraNameChars[j]) {
                            allowed = true;
                            break;
                        }
                    }
                    if (!allowed) {
                        throw new IllegalArgumentException("Invalid character in identifier: " + c);
                    }
                }
            }
            return;
        }
    }

    /**
     * Returns an appropriate escaped/quoted version of the identifier. Assumes that the identifier has already
     * been validated, i.e. by passing it to the validateIdentifier method.
     *
     * @param identifier The identifier to be escaped.
     * @return The escaped version of the identifier.
     */
    public String escapeIdentifier(final String identifier) {
        if (identifierQuoteString != null) {
            return identifierQuoteString + identifier + identifierQuoteString;
        } else {
            return identifier;
        }
    }
}
