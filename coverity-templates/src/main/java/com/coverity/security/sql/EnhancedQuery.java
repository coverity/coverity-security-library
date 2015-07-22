package com.coverity.security.sql;

import javax.persistence.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides an enhanced JPA Query implementation which allows the parameterization of query identifiers. For example,
 * instead of
 *
 *   Query query = em.createNativeQuery("SELECT * FROM " + tableName);
 *   query.getResultList();
 *
 * You can write:
 *
 *   EnhancedQuery query = EnhancedQuery.createNativeQuery(em, "SELECT * FROM :tableName");
 *   query.setIdentifier("tableName", tableName);
 *   query.getResultList();
 *
 * This interface allows for creating both native and JPQL queries via the static createNativeQuery and createQuery
 * methods. Both positional and named parameter styles are allowed. Identfiers must be set using the setIdentfier()
 * method instead of the setParameter(), since otherwise it may be ambiguous; consider "SELECT :foo FROM myTable".
 *
 */
public abstract class EnhancedQuery implements Query {

    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile(":[a-zA-Z0-9]+");
    private static final Pattern POS_PARAM_PATTERN = Pattern.compile("\\?[0-9]+");

    private final Map<String, Object> hints = new HashMap<String, Object>();
    private final EntityManager entityManager;
    private final boolean isNative;

    private int maxResults = Integer.MAX_VALUE;
    private int firstResult = 0;
    private FlushModeType flushMode;
    private LockModeType lockMode = null;

    protected EnhancedQuery(EntityManager entityManager, boolean isNative) {
        this.entityManager = entityManager;
        this.isNative = isNative;
        flushMode = entityManager.getFlushMode();
    }

    /**
     * Creates an EnhancedQuery instance using the provided JPQL query string. The query string may have either
     * positional or named parameters (but not both).
     *
     * @param entityManager The entity manager against which the query will be executed.
     * @param jpqlString The parameterized JPQL query string.
     * @return The EnhancedQuery instance.
     */
    public static EnhancedQuery createQuery(EntityManager entityManager, String jpqlString) {
        return createQuery(entityManager, jpqlString, false);
    }

    /**
     * Creates an EnhancedQuery instance using the provided native query string. The query string may have either
     * positional or named parameters (but not both).
     *
     * @param entityManager The entity manager against which the query will be executed.
     * @param sqlString The parameterized native query string.
     * @return The EnhancedQuery instance.
     */
    public static EnhancedQuery createNativeQuery(EntityManager entityManager, String sqlString) {
        return createQuery(entityManager, sqlString, true);
    }

    private static EnhancedQuery createQuery(EntityManager entityManager, String qlString, boolean isNative) {
        // TODO: Do proper tokenizing and parsing instead of regex matching
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(qlString);
        if (matcher.find()) {
            int start = 0;
            List<String> pieces = new ArrayList<String>();
            List<String> parameters = new ArrayList<String>();
            while (matcher.find(start)) {
                final String parameter = matcher.group().substring(1);
                pieces.add(qlString.substring(start, matcher.start()));
                parameters.add(parameter);
                start = matcher.end();
            }
            pieces.add(qlString.substring(start));
            return new EnhancedNamedQuery(entityManager, isNative, pieces.toArray(new String[pieces.size()]), parameters.toArray(new String[parameters.size()]));
        }

        matcher = POS_PARAM_PATTERN.matcher(qlString);
        if (matcher.find()) {
            int start = 0;
            List<String> pieces = new ArrayList<String>();
            while (matcher.find(start)) {
                pieces.add(qlString.substring(start, matcher.start()));
                start = matcher.end();
            }
            pieces.add(qlString.substring(start));
            return new EnhancedPositionalQuery(entityManager, isNative, pieces.toArray(new String[pieces.size()]));
        }

        return new EnhancedPositionalQuery(entityManager, isNative, new String[] { qlString });
    }

    protected abstract Query buildQuery(EntityManager entityManager, boolean isNative);

    @Override
    public abstract EnhancedQuery setParameter(int position, Object value);

    @Override
    public abstract EnhancedQuery setParameter(String name, Object value);

    @Override
    public abstract EnhancedQuery setParameter(int position, Date value, TemporalType temporalType);

    @Override
    public abstract EnhancedQuery setParameter(String name, Date value, TemporalType temporalType);

    @Override
    public abstract EnhancedQuery setParameter(int position, Calendar value, TemporalType temporalType);

    @Override
    public abstract EnhancedQuery setParameter(String name, Calendar value, TemporalType temporalType);

    @Override
    public abstract <T> EnhancedQuery setParameter(Parameter<T> param, T value);

    @Override
    public abstract EnhancedQuery setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType);

    /**
     * Sets a parameter as an identifier.
     *
     * @param position The positional parameter to be set.
     * @param value The identifier value. An exception will be thrown if the identifier value has invalid characters.
     * @return This EnhancedQuery instance.
     */
    public abstract EnhancedQuery setIdentifier(int position, String value);

    /**
     * Sets a parameter as an identifier.
     *
     * @param name The named parameter to be set.
     * @param value The identifier value. An exception will be thrown if the identifier value has invalid characters.
     * @return This EnhancedQuery instance.
     */
    public abstract EnhancedQuery setIdentifier(String name, String value);

    /**
     * Sets a parameter to a list of identifiers. For example:
     *   EnhancedQuery.createNativeQuery(em, "SELECT ?1 FROM myTable").setIdentifiers(1, new String[] {"a", "b"});
     * will result in the query "SELECT a, b FROM myTable".
     *
     * @param position The positional parameter to be set.
     * @param values The identifier values. An exception will be thrown if any of the identifiers value have invalid
     *               characters.
     * @return This EnhancedQuery instance.
     */
    public abstract EnhancedQuery setIdentifiers(int position, String[] values);

    /**
     * Sets a parameter to a list of identifiers. For example:
     *   EnhancedQuery.createNativeQuery(em, "SELECT :colNames FROM myTable").setIdentifiers("colNames", new String[] {"a", "b"});
     * will result in the query "SELECT a, b FROM myTable".
     *
     * @param name The named parameter to be set.
     * @param values The identifier values. An exception will be thrown if any of the identifiers value have invalid
     *               characters.
     * @return This EnhancedQuery instance.
     */
    public abstract EnhancedQuery setIdentifiers(String name, String[] values);

    /**
     * Sets a parameter to a list of identifiers. For example:
     *   EnhancedQuery.createNativeQuery(em, "SELECT ?1 FROM myTable").setIdentifiers(1, Arrays.asList("a", "b"));
     * will result in the query "SELECT a, b FROM myTable".
     *
     * @param position The positional parameter to be set.
     * @param values The identifier values. An exception will be thrown if any of the identifiers value have invalid
     *               characters.
     * @return This EnhancedQuery instance.
     */
    public EnhancedQuery setIdentifiers(int position, Collection<String> values) {
        return setIdentifiers(position, values.toArray(new String[values.size()]));
    }

    /**
     * Sets a parameter to a list of identifiers. For example:
     *   EnhancedQuery.createNativeQuery(em, "SELECT :colNames FROM myTable").setIdentifiers("colNames", Arrays.asList("a", "b"));
     * will result in the query "SELECT a, b FROM myTable".
     *
     * @param name The named parameter to be set.
     * @param values The identifier values. An exception will be thrown if any of the identifiers value have invalid
     *               characters.
     * @return This EnhancedQuery instance.
     */
    public EnhancedQuery setIdentifiers(String name, Collection<String> values) {
        return setIdentifiers(name, values.toArray(new String[values.size()]));
    }

    private Query prepareQuery() {
        Query query = buildQuery(entityManager, isNative);
        query.setMaxResults(maxResults);
        query.setFirstResult(firstResult);
        for (Map.Entry<String, Object> hintEntry : hints.entrySet()) {
            query.setHint(hintEntry.getKey(), hintEntry.getValue());
        }
        query.setFlushMode(flushMode);
        if (lockMode != null) {
            query.setLockMode(lockMode);
        }
        return query;
    }

    @Override
    public List getResultList() {
        return prepareQuery().getResultList();
    }

    @Override
    public Object getSingleResult() {
        return prepareQuery().getSingleResult();
    }

    @Override
    public int executeUpdate() {
        return prepareQuery().executeUpdate();
    }

    @Override
    public Query setMaxResults(int maxResult) {
        this.maxResults = maxResult;
        return this;
    }

    @Override
    public int getMaxResults() {
        return this.maxResults;
    }

    @Override
    public Query setFirstResult(int startPosition) {
        this.firstResult = startPosition;
        return this;
    }

    @Override
    public int getFirstResult() {
        return this.firstResult;
    }

    @Override
    public Query setHint(String hintName, Object value) {
        this.hints.put(hintName, value);
        return this;
    }

    @Override
    public Map<String, Object> getHints() {
        return this.hints;
    }

    @Override
    public Query setFlushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return this;
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    @Override
    public Query setLockMode(LockModeType lockMode) {
        this.lockMode = lockMode;
        return this;
    }

    @Override
    public LockModeType getLockMode() {
        if (lockMode == null) {
            throw new IllegalStateException("Cannot get lock mode from EnhancedQuery");
        }
        return lockMode;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        throw new UnsupportedOperationException("Cannot unwrap EnhancedQuery.");
    }

    protected final void validateIdentifier(String identifier) {
        if (identifier == null || identifier.length() == 0) {
            throw new IllegalArgumentException("Invalid identifier.");
        }

        final char[] chars = identifier.toCharArray();
        if (isNative) {

            // JPA does not provide underlying metadata, so we must fall back to SQL-standard whitelist of identifier
            // characters.

            for (char c : chars) {

                if ((c < 'a' || c > 'z')
                        && (c < 'A' || c > 'Z')
                        && (c < '0' || c > '9')
                        && (c != '_')) {

                    throw new IllegalArgumentException("Invalid identifier.");

                }
            }

        } else {

            // JPQL identifiers are specified by legal Java identifiers; see
            // https://docs.oracle.com/javaee/6/tutorial/doc/bnbuf.html#bnbuk

            if (!Character.isJavaIdentifierStart(chars[0])) {
                throw new IllegalArgumentException("Invalid identifier.");
            }

            for (int i = 1; i < chars.length; i++) {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    throw new IllegalArgumentException("Invalid identifier.");
                }
            }

        }
    }
}
