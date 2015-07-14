package com.coverity.security.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

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

    public String escapeIdentifier(final String identifier) {
        if (identifierQuoteString != null) {
            if (identifier.contains(identifierQuoteString)) {
                throw new IllegalArgumentException("Identifier cannot contain quote string: " + identifierQuoteString);
            }
            return identifierQuoteString + identifier + identifierQuoteString;
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
            return identifier;
        }
    }
}
