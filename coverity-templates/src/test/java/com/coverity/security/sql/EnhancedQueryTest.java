package com.coverity.security.sql;

import com.coverity.security.sql.test.MockEntityManager;
import com.coverity.security.sql.test.MockQuery;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Parameter;
import javax.persistence.TemporalType;

import java.util.*;

import static org.testng.Assert.*;

public class EnhancedQueryTest {

    @Test
    public void testNativeNamedQuery() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM :tableName WHERE :colName = :value ORDER BY ponies");
        query.setIdentifier("tableName", "foo")
                .setIdentifier("colName", "bar")
                .setParameter("value", 123)
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.isNative(), true);
        assertEquals(mockQuery.getQlString(), "SELECT * FROM foo WHERE bar = :value ORDER BY ponies");
        assertEquals(mockQuery.getParameterValue("value"), 123);
    }

    @Test
    public void testNativePositionalQuery() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM ?1 WHERE ?2 = ?3 ORDER BY ?4");
        query.setIdentifier(1, "foo")
                .setIdentifier(2, "bar")
                .setParameter(3, 123)
                .setIdentifier(4, "ponies")
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.isNative(), true);
        assertEquals(mockQuery.getQlString(), "SELECT * FROM foo WHERE bar = ?1 ORDER BY ponies");
        assertEquals(mockQuery.getParameterValue(1), 123);
    }

    @Test
    public void testNamedQuery() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createQuery(entityManager, "FROM :tableName WHERE :colName = :value ORDER BY ponies");
        query.setIdentifier("tableName", "foo")
                .setIdentifier("colName", "bar")
                .setParameter("value", 123)
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.isNative(), false);
        assertEquals(mockQuery.getQlString(), "FROM foo WHERE bar = :value ORDER BY ponies");
        assertEquals(mockQuery.getParameterValue("value"), 123);
    }

    @Test
    public void testPositionalQuery() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createQuery(entityManager, "FROM ?1 WHERE ?2 = ?3 ORDER BY ?4");
        query.setIdentifier(1, "foo")
                .setIdentifier(2, "bar")
                .setParameter(3, 123)
                .setIdentifier(4, "ponies")
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.isNative(), false);
        assertEquals(mockQuery.getQlString(), "FROM foo WHERE bar = ?1 ORDER BY ponies");
        assertEquals(mockQuery.getParameterValue(1), 123);
    }

    @Test
    public void testParameterlessQuery() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo");
        query.getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.isNative(), true);
        assertEquals(mockQuery.getQlString(), "SELECT * FROM foo");
    }

    @Test
    public void testQueryVariables() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo");
        query.setMaxResults(123)
                .setFirstResult(234)
                .setHint("foo", "bar")
                .setHint("oof", "rab")
                .setFlushMode(FlushModeType.COMMIT)
                .setLockMode(LockModeType.OPTIMISTIC);

        assertEquals(query.getMaxResults(), 123);
        assertEquals(query.getFirstResult(), 234);
        assertEquals(query.getHints().get("foo"), "bar");
        assertEquals(query.getHints().get("oof"), "rab");
        assertEquals(query.getFlushMode(), FlushModeType.COMMIT);
        assertEquals(query.getLockMode(), LockModeType.OPTIMISTIC);

        query.getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.getMaxResults(), 123);
        assertEquals(mockQuery.getFirstResult(), 234);
        assertEquals(mockQuery.getHints().get("foo"), "bar");
        assertEquals(mockQuery.getHints().get("oof"), "rab");
        assertEquals(mockQuery.getFlushMode(), FlushModeType.COMMIT);
        assertEquals(mockQuery.getLockMode(), LockModeType.OPTIMISTIC);

        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo");
        boolean exception = false;
        try {
            query.getLockMode();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalStateException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            query.unwrap(Object.class);
        } catch (Exception e) {
            assertEquals(e.getClass(), UnsupportedOperationException.class);
            exception = true;
        }
        assertTrue(exception);

    }

    @Test
    public void testExecutions() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo");

        assertEquals(query.getResultList().toArray(), new Object[0]);
        assertEquals(query.getSingleResult(), "Single Result");
        assertEquals(query.executeUpdate(), 12345);
    }

    @Test(dataProvider = "identifierTests")
    public void testInvalidIdentifiers(boolean isNative, String sql, String identifier, String expected) {
        MockEntityManager entityManager = new MockEntityManager();

        EnhancedQuery query;
        if (isNative) {
            query = EnhancedNamedQuery.createNativeQuery(entityManager, sql);
        } else {
            query = EnhancedNamedQuery.createQuery(entityManager, sql);
        }

        boolean exception = false;
        try {
            query.setIdentifier(1, identifier).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        if (expected == null) {
            assertTrue(exception);
        } else {
            assertFalse(exception);
            assertEquals(entityManager.getMockQueries().size(), 1);
            MockQuery mockQuery = entityManager.getMockQueries().get(0);
            assertEquals(mockQuery.getQlString(), expected);
        }
    }

    @DataProvider
    public Object[][] identifierTests() {
        return new Object[][]{
                // isNative, SQL, identifier, expected result (null => exception)
                {true, "SELECT * FROM ?1", "foo_bar", "SELECT * FROM foo_bar"},
                {true, "SELECT * FROM ?1", "foo12_21bar", "SELECT * FROM foo12_21bar"},
                {true, "SELECT * FROM ?1", "foo12_21bar!", null},
                {true, "SELECT * FROM ?1", "!foo", null},
                {true, "SELECT * FROM ?1", "", null},
                {true, "SELECT * FROM ?1", null, null},

                {false, "FROM ?1", "foo_bar", "FROM foo_bar"},
                {false, "FROM ?1", "foo12_21bar", "FROM foo12_21bar"},
                {false, "FROM ?1", "foo12_21bar!", null},
                {false, "FROM ?1", "!foo", null},
                {false, "FROM ?1", "12foo", null},
                {false, "FROM ?1", "foo12", "FROM foo12"},
                {false, "FROM ?1", "", null},
                {false, "FROM ?1", null, null},
        };
    }

    @Test
    public void testTemporalParams() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = :date AND baz = :cal");
        final Date date = new Date();
        final Calendar cal = Calendar.getInstance();
        query.setParameter("date", date, TemporalType.DATE)
                .setParameter("cal", cal, TemporalType.TIME)
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.getParameterValue("date").getClass(), MockQuery.TemporalHolder.class);
        assertEquals(mockQuery.getParameterValue("cal").getClass(), MockQuery.TemporalHolder.class);

        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("date")).getDate(), date);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("date")).getTemporalType(), TemporalType.DATE);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("cal")).getCalendar(), cal);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("cal")).getTemporalType(), TemporalType.TIME);


        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = ?1 AND baz = ?2");
        query.setParameter(1, date, TemporalType.DATE)
                .setParameter(2, cal, TemporalType.TIME)
                .getResultList();

        assertEquals(entityManager.getMockQueries().size(), 2);
        mockQuery = entityManager.getMockQueries().get(1);
        assertEquals(mockQuery.getParameterValue(1).getClass(), MockQuery.TemporalHolder.class);
        assertEquals(mockQuery.getParameterValue(2).getClass(), MockQuery.TemporalHolder.class);

        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(1)).getDate(), date);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(1)).getTemporalType(), TemporalType.DATE);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(2)).getCalendar(), cal);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(2)).getTemporalType(), TemporalType.TIME);
    }

    @Test
    public void testMixedParameterizations() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery namedQuery = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM :table");
        EnhancedQuery posQuery = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM ?1");

        boolean exception = false;
        try {
            namedQuery.setIdentifier(1, "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(1, "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(1, new Date(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(1, Calendar.getInstance(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(posQuery.getParameter(1, String.class), "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(posQuery.getParameter(1, Date.class), new Date(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(posQuery.getParameter(1, Calendar.class), Calendar.getInstance(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.setParameter(posQuery.getParameter(1), null);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.isBound(posQuery.getParameter(1));
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.getParameter(1);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.getParameter(1, String.class);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.getParameterValue(1);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            namedQuery.getParameterValue(posQuery.getParameter(1));
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);


        // Now test all this on the position query
        exception = false;
        try {
            posQuery.setIdentifier("name", "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter("name", "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter("name", new Date(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter("name", Calendar.getInstance(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter(namedQuery.getParameter("name", String.class), "foo").getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter(namedQuery.getParameter("name", Date.class), new Date(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter(namedQuery.getParameter("name", Calendar.class), Calendar.getInstance(), TemporalType.DATE).getResultList();
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.setParameter(namedQuery.getParameter("name"), null);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.isBound(namedQuery.getParameter("name"));
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.getParameter("name");
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.getParameter("name", String.class);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.getParameterValue("name");
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            posQuery.getParameterValue(namedQuery.getParameter("name"));
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

    }


    @Test
    public void testIsBound() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = :var AND baz = :xyz");
        query.setParameter("var", 123);
        assertEquals(query.isBound(query.getParameter("var")), true);
        assertEquals(query.isBound(query.getParameter("xyz")), false);

        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = ?1 AND baz = ?2");
        query.setParameter(1, 123);
        assertEquals(query.isBound(query.getParameter(1)), true);
        assertEquals(query.isBound(query.getParameter(2)), false);
    }

    @Test
    public void testGetParameters() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = :var AND baz = :xyz AND town = :zzz");
        List<Parameter<?>> params = new ArrayList<Parameter<?>>(query.getParameters());
        Collections.sort(params, new Comparator<Parameter<?>>() {
            @Override
            public int compare(Parameter<?> o1, Parameter<?> o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        assertEquals(params.get(0).getName(), "var");
        assertEquals(params.get(1).getName(), "xyz");
        assertEquals(params.get(2).getName(), "zzz");
        assertEquals(query.getParameter("var", String.class).getParameterType(), String.class);

        final Date date = new Date();
        final Calendar cal = Calendar.getInstance();
        query.setParameter(query.getParameter("var", Integer.class), 123);
        query.setParameter(query.getParameter("xyz", Date.class), date, TemporalType.DATE);
        query.setParameter(query.getParameter("zzz", Calendar.class), cal, TemporalType.TIME);

        assertEquals(query.getParameterValue(query.getParameter("var", Integer.class)).intValue(), 123);
        assertEquals(query.getParameterValue(query.getParameter("xyz", Date.class)), date);
        assertEquals(query.getParameterValue(query.getParameter("zzz", Calendar.class)), cal);

        query.getResultList();
        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);

        assertEquals(mockQuery.getParameterValue("var"), 123);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("xyz")).getDate(), date);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("xyz")).getTemporalType(), TemporalType.DATE);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("zzz")).getCalendar(), cal);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue("zzz")).getTemporalType(), TemporalType.TIME);


        query = EnhancedQuery.createNativeQuery(entityManager, "SELECT * FROM foo WHERE bar = ?1 AND baz = ?2 AND town = ?3");
        params = new ArrayList<Parameter<?>>(query.getParameters());
        Collections.sort(params, new Comparator<Parameter<?>>() {
            @Override
            public int compare(Parameter<?> o1, Parameter<?> o2) {
                return o1.getPosition().compareTo(o2.getPosition());
            }
        });
        assertEquals(params.get(0).getPosition().intValue(), 1);
        assertEquals(params.get(1).getPosition().intValue(), 2);
        assertEquals(params.get(2).getPosition().intValue(), 3);
        assertEquals(query.getParameter(1, String.class).getParameterType(), String.class);

        query.setParameter(query.getParameter(1, Integer.class), 123);
        query.setParameter(query.getParameter(2, Date.class), date, TemporalType.DATE);
        query.setParameter(query.getParameter(3, Calendar.class), cal, TemporalType.TIME);

        assertEquals(query.getParameterValue(query.getParameter(1, Integer.class)).intValue(), 123);
        assertEquals(query.getParameterValue(query.getParameter(2, Date.class)), date);
        assertEquals(query.getParameterValue(query.getParameter(3, Calendar.class)), cal);

        query.getResultList();
        assertEquals(entityManager.getMockQueries().size(), 2);
        mockQuery = entityManager.getMockQueries().get(1);

        assertEquals(mockQuery.getParameterValue(1), 123);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(2)).getDate(), date);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(2)).getTemporalType(), TemporalType.DATE);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(3)).getCalendar(), cal);
        assertEquals(((MockQuery.TemporalHolder) mockQuery.getParameterValue(3)).getTemporalType(), TemporalType.TIME);

    }


    @Test
    public void testSetIdentifiers() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "TRUNCATE :tableNames");

        boolean exception = false;
        try {
            query.setIdentifiers("tableNames", new String[0]);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            query.setIdentifiers(1, new String[]{"a", "b", "c"});
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        query.setIdentifiers("tableNames", new String[]{"a", "b", "c"}).getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.getQlString(), "TRUNCATE a, b, c");


        query = EnhancedQuery.createNativeQuery(entityManager, "TRUNCATE ?1");

        exception = false;
        try {
            query.setIdentifiers(1, new String[0]);
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        exception = false;
        try {
            query.setIdentifiers("tableNames", new String[]{"a", "b", "c"});
        } catch (Exception e) {
            assertEquals(e.getClass(), IllegalArgumentException.class);
            exception = true;
        }
        assertTrue(exception);

        query.setIdentifiers(1, new String[]{"a", "b", "c"}).getResultList();

        assertEquals(entityManager.getMockQueries().size(), 2);
        mockQuery = entityManager.getMockQueries().get(1);
        assertEquals(mockQuery.getQlString(), "TRUNCATE a, b, c");
    }

    @Test
    public void testSetIdentifiersList() {
        MockEntityManager entityManager = new MockEntityManager();
        EnhancedQuery query = EnhancedQuery.createNativeQuery(entityManager, "TRUNCATE :tableNames");
        query.setIdentifiers("tableNames", Arrays.asList("a", "b", "c")).getResultList();

        assertEquals(entityManager.getMockQueries().size(), 1);
        MockQuery mockQuery = entityManager.getMockQueries().get(0);
        assertEquals(mockQuery.getQlString(), "TRUNCATE a, b, c");

        query = EnhancedQuery.createNativeQuery(entityManager, "TRUNCATE ?1");
        query.setIdentifiers(1, Arrays.asList("a", "b", "c")).getResultList();

        assertEquals(entityManager.getMockQueries().size(), 2);
        mockQuery = entityManager.getMockQueries().get(1);
        assertEquals(mockQuery.getQlString(), "TRUNCATE a, b, c");
    }
}