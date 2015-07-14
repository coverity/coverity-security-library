package com.coverity.security.sql.test;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ExceptionThrowingConnection extends MockConnection {
    public ExceptionThrowingConnection(String identifierQuote, String extraNameChars) {
        super(identifierQuote, extraNameChars);
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        MockPreparedStatement stmt = new ExceptionThrowingPreparedStatement(sql);
        super.getMockStatements().add(stmt);
        return stmt;
    }
}
