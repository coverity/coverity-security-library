package com.coverity.security.sql.it;

import com.coverity.security.sql.EnhancedPreparedStatement;
import com.coverity.security.sql.EnhancedQuery;
import com.coverity.security.sql.ParameterizedStatement;
import com.coverity.security.sql.it.entity.Person;
import com.coverity.security.sql.it.entity.Pet;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.sql.*;
import java.util.*;

import static org.testng.Assert.assertEquals;

public abstract class AbstractDatabaseIT {

    private Connection conn;
    private DatabaseMetaData databaseMetaData;
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;

    protected abstract String getJdbcConnectionString();
    protected abstract String getConnectionUsername();
    protected abstract String getConnectionPassword();
    protected abstract String getHibernateDialect();

    @BeforeClass
    public void setup() throws Exception {


        Map<String, Object> configOverrides = new HashMap<String, Object>();
        configOverrides.put("hibernate.connection.driver_class",
                DriverManager.getDriver(getJdbcConnectionString()).getClass().getName());
        configOverrides.put("hibernate.connection.password", getConnectionPassword());
        configOverrides.put("hibernate.connection.url", getJdbcConnectionString());
        configOverrides.put("hibernate.connection.username", getConnectionUsername());
        configOverrides.put("hibernate.dialect", getHibernateDialect());

        entityManagerFactory = Persistence.createEntityManagerFactory("jpatest", configOverrides);
        entityManager = entityManagerFactory.createEntityManager();

        conn = DriverManager.getConnection(getJdbcConnectionString(), getConnectionUsername(), getConnectionPassword());
        databaseMetaData = conn.getMetaData();

        /*
        stmt.execute("CREATE TABLE Person ("
                + "id BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
                + "name VARCHAR(255) NOT NULL,"
                + "city VARCHAR(255),"
                + "zipcode VARCHAR(10),"
                + "PRIMARY KEY (id))");

        stmt.execute("CREATE TABLE Pet ("
                + "id BIGINT(10) UNSIGNED NOT NULL AUTO_INCREMENT,"
                + "name VARCHAR(255) NOT NULL,"
                + "owner_id BIGINT(10) UNSIGNED,"
                + "city VARCHAR(255),"
                + "PRIMARY KEY (id))");

        stmt.execute("ALTER TABLE Pet ADD FOREIGN KEY (owner_id) REFERENCES Person (id)");
*/
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO Person (id,firstName,city,zipcode) VALUES "
                + "(1,'Alice','San Francisco','94107'),"
                + "(2,'Charlie','San Francisco', '94106'),"
                + "(3,'Bob','San Francisco', '94106'),"
                + "(4,'Dave','Seattle', '98104'),"
                + "(5,'Elvis','Seattle', '98103'),"
                + "(6,'Frank','Seattle', '98105')");

        stmt.execute("INSERT INTO Pet (id,name,owner_id,location) VALUES "
                + "(1,'Rudolph',1,'San Francisco'),"
                + "(2,'Pitah',1,'San Francisco'),"
                + "(3,'Remy',2,'Seattle'),"
                + "(4,'Lyra',2,'San Francisco'),"
                + "(5,'BonBon',3,'Seattle'),"
                + "(6,'Colgate',3,'Seattle'),"
                + "(7,'Mocha',5,'Seattle'),"
                + "(8,'Ditzy',NULL,'Seattle')");
        stmt.close();
    }

    @AfterClass
    public void teardown() throws SQLException {
        conn.close();
        entityManager.close();
        entityManagerFactory.close();
    }

    @Test
    public void testParameterizedStatement() throws SQLException {
        ParameterizedStatement pstmt = ParameterizedStatement.prepare(conn, "SELECT firstName,zipcode FROM :table WHERE city=? ORDER BY :orderBy");

        pstmt.setIdentifier("table", formatIdentifier("Person"));
        pstmt.setIdentifiers("orderBy", formatIdentifiers(Arrays.asList("zipcode", "firstName")));
        PreparedStatement stmt = pstmt.prepareStatement();
        stmt.setString(1, "San Francisco");
        Object[][] rs = extractResultSet(stmt.executeQuery());
        stmt.close();
        assertArrayEquals(rs, new Object[][]{{"Bob", "94106"}, {"Charlie", "94106"}, {"Alice", "94107"}});

        pstmt = ParameterizedStatement.prepare(conn, "SELECT T.name,location,OT.firstName AS owner_name FROM :table T INNER JOIN :ownerTable OT ON T.owner_id=OT.id WHERE OT.city=? ORDER BY :orderBy");
        pstmt.setIdentifier("table", formatIdentifier("Pet"));
        pstmt.setIdentifier("ownerTable", formatIdentifier("Person"));
        pstmt.setIdentifiers("orderBy", formatIdentifiers(Arrays.asList("location", "name")));
        stmt = pstmt.prepareStatement();
        stmt.setString(1, "San Francisco");
        rs = extractResultSet(stmt.executeQuery());
        stmt.close();
        assertArrayEquals(rs, new Object[][]{
                {"Lyra", "San Francisco", "Charlie"},
                {"Pitah", "San Francisco", "Alice"},
                {"Rudolph", "San Francisco", "Alice"},
                {"BonBon", "Seattle", "Bob"},
                {"Colgate", "Seattle", "Bob"},
                {"Remy", "Seattle", "Charlie"},
        });
    }

    @Test
    public void testEnhancedPreparedStatement() throws SQLException {
        EnhancedPreparedStatement stmt = EnhancedPreparedStatement.prepareStatement(conn, "SELECT firstName,zipcode FROM ? WHERE city=? ORDER BY ?");

        stmt.setIdentifier(1, formatIdentifier("Person"));
        stmt.setString(2, "San Francisco");
        stmt.setIdentifiers(3, formatIdentifiers(Arrays.asList("zipcode", "firstName")));
        Object[][] rs = extractResultSet(stmt.executeQuery());
        assertArrayEquals(rs, new Object[][]{{"Bob", "94106"}, {"Charlie", "94106"}, {"Alice", "94107"}});

        stmt = EnhancedPreparedStatement.prepareStatement(conn, "SELECT T.name,T.location,OT.firstName AS owner_name FROM ? T INNER JOIN ? OT ON T.owner_id=OT.id WHERE OT.city=? ORDER BY ?");
        stmt.setIdentifier(1, formatIdentifier("Pet"));
        stmt.setIdentifier(2, formatIdentifier("Person"));
        stmt.setString(3, "San Francisco");
        stmt.setIdentifiers(4, formatIdentifiers(Arrays.asList("location", "name")));
        rs = extractResultSet(stmt.executeQuery());
        stmt.close();
        assertArrayEquals(rs, new Object[][]{
                {"Lyra", "San Francisco", "Charlie"},
                {"Pitah", "San Francisco", "Alice"},
                {"Rudolph", "San Francisco", "Alice"},
                {"BonBon", "Seattle", "Bob"},
                {"Colgate", "Seattle", "Bob"},
                {"Remy", "Seattle", "Charlie"},
        });
    }

    @Test
    public void testEnhancedQuery() throws SQLException {
        // Test as JPQL query
        EnhancedQuery query = EnhancedQuery.createQuery(entityManager, "FROM :entityName WHERE city=:city ORDER BY :orderBy");
        query.setIdentifier("entityName", "Person");
        query.setParameter("city", "San Francisco");
        query.setIdentifiers("orderBy", Arrays.asList("zipcode", "firstName"));
        List<Person> persons = query.getResultList();
        assertEquals(persons.size(), 3);
        assertEquals(persons.get(0).getFirstName(), "Bob");
        assertEquals(persons.get(0).getZipcode(), "94106");
        assertEquals(persons.get(1).getFirstName(), "Charlie");
        assertEquals(persons.get(1).getZipcode(), "94106");
        assertEquals(persons.get(2).getFirstName(), "Alice");
        assertEquals(persons.get(2).getZipcode(), "94107");

        query = EnhancedQuery.createQuery(entityManager, "FROM :entityName T WHERE T.owner.city=:ownerCity ORDER BY :orderBy");
        query.setIdentifier("entityName", "Pet");
        query.setParameter("ownerCity", "San Francisco");
        query.setIdentifiers("orderBy", Arrays.asList("location", "name"));
        List<Pet> pets = query.getResultList();
        assertEquals(pets.size(), 6);
        assertEquals(pets.get(0).getName(), "Lyra");
        assertEquals(pets.get(1).getName(), "Pitah");
        assertEquals(pets.get(2).getName(), "Rudolph");
        assertEquals(pets.get(3).getName(), "BonBon");
        assertEquals(pets.get(4).getName(), "Colgate");
        assertEquals(pets.get(5).getName(), "Remy");

        // Test as native query
        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT zipcode,firstName FROM :tableName WHERE city=:city ORDER BY :orderBy");
        query.setIdentifier("tableName", formatIdentifier("Person"));
        query.setParameter("city", "San Francisco");
        query.setIdentifiers("orderBy", Arrays.asList("zipcode", "firstName"));
        List<?> nativeResult = query.getResultList();
        assertEquals(nativeResult.size(), 3);
        assertEquals(((Object[]) nativeResult.get(0))[0], "94106");
        assertEquals(((Object[]) nativeResult.get(0))[1], "Bob");
        assertEquals(((Object[]) nativeResult.get(1))[0], "94106");
        assertEquals(((Object[]) nativeResult.get(1))[1], "Charlie");
        assertEquals(((Object[]) nativeResult.get(2))[0], "94107");
        assertEquals(((Object[]) nativeResult.get(2))[1], "Alice");

        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT T.name,T.location,OT.firstName AS owner_name FROM :table T INNER JOIN :ownerTable OT ON T.owner_id=OT.id WHERE OT.city=:ownerCity ORDER BY :orderBy");
        query.setIdentifier("table", formatIdentifier("Pet"));
        query.setIdentifier("ownerTable", formatIdentifier("Person"));
        query.setParameter("ownerCity", "San Francisco");
        query.setIdentifiers("orderBy", formatIdentifiers(Arrays.asList("location", "name")));
        nativeResult = query.getResultList();
        assertEquals(nativeResult.size(), 6);
        assertEquals(((Object[]) nativeResult.get(0))[0], "Lyra");
        assertEquals(((Object[]) nativeResult.get(1))[0], "Pitah");
        assertEquals(((Object[]) nativeResult.get(2))[0], "Rudolph");
        assertEquals(((Object[]) nativeResult.get(3))[0], "BonBon");
        assertEquals(((Object[]) nativeResult.get(4))[0], "Colgate");
        assertEquals(((Object[]) nativeResult.get(5))[0], "Remy");
    }

    private static void assertArrayEquals(Object[][] actual, Object[][] expected) {
        if (actual.length != expected.length) {
            throw new AssertionError("Arrays not equal: " + Arrays.toString(expected) + " and " + Arrays.toString(actual));
        }
        for (int i = 0; i < actual.length; i++) {
            assertEquals(actual[i], expected[i]);
        }
    }

    private static Object[][] extractResultSet(ResultSet rs) throws SQLException {
        List<Object[]> output = new ArrayList<Object[]>();
        final int columnCount = rs.getMetaData().getColumnCount();
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                row[i] = rs.getObject(i+1);
            }
            output.add(row);
        }
        rs.close();
        return output.toArray(new Object[output.size()][]);
    }

    private String formatIdentifier(String identifier) throws SQLException {
        if (databaseMetaData.storesLowerCaseIdentifiers()) {
            return identifier.toLowerCase();
        }
        if (databaseMetaData.storesUpperCaseIdentifiers()) {
            return identifier.toUpperCase();
        }
        return identifier;
    }
    private List<String> formatIdentifiers(List<String> identifiers) throws SQLException {
        for (int i = 0; i < identifiers.size(); i++) {
            identifiers.set(i, formatIdentifier(identifiers.get(i)));
        }
        return identifiers;
    }

}
