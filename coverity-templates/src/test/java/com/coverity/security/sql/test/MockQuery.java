package com.coverity.security.sql.test;

import javax.persistence.*;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

public class MockQuery implements Query {

    private final String qlString;
    private final boolean isNative;
    private Map<String, Object> namedParams = new HashMap<String, Object>();
    private Map<Integer, Object> positionalParams = new HashMap<Integer, Object>();

    private final Map<String, Object> hints = new HashMap<String, Object>();
    private int maxResults = Integer.MAX_VALUE;
    private int firstResult = 0;
    private FlushModeType flushMode;
    private LockModeType lockMode = null;

    public static class TemporalHolder {
        private final Date date;
        private final Calendar calendar;
        private final TemporalType temporalType;

        public TemporalHolder(Date date, Calendar calendar, TemporalType temporalType) {
            this.date = date;
            this.calendar = calendar;
            this.temporalType = temporalType;
        }

        public Date getDate() {
            return date;
        }

        public Calendar getCalendar() {
            return calendar;
        }

        public TemporalType getTemporalType() {
            return temporalType;
        }
    }

    public MockQuery(String qlString, boolean isNative) {
        this.qlString = qlString;
        this.isNative = isNative;
    }

    public String getQlString() {
        return qlString;
    }

    public boolean isNative() {
        return isNative;
    }

    @Override
    public List getResultList() {
        return new ArrayList<Object>(0);
    }

    @Override
    public Object getSingleResult() {
        return "Single Result";
    }

    @Override
    public int executeUpdate() {
        return 12345;
    }

    @Override
    public Query setMaxResults(int maxResult) {
        this.maxResults = maxResult;
        return this;
    }

    @Override
    public int getMaxResults() {
        return maxResults;
    }

    @Override
    public Query setFirstResult(int startPosition) {
        this.firstResult = startPosition;
        return this;
    }

    @Override
    public int getFirstResult() {
        return firstResult;
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
    public <T> Query setParameter(Parameter<T> param, T value) {
        return null;
    }

    @Override
    public Query setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        return null;
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        return null;
    }

    @Override
    public Query setParameter(String name, Object value) {
        namedParams.put(name, value);
        return this;
    }

    @Override
    public Query setParameter(String name, Calendar value, TemporalType temporalType) {
        namedParams.put(name, new TemporalHolder(null, value, temporalType));
        return this;
    }

    @Override
    public Query setParameter(String name, Date value, TemporalType temporalType) {
        namedParams.put(name, new TemporalHolder(value, null, temporalType));
        return this;
    }

    @Override
    public Query setParameter(int position, Object value) {
        positionalParams.put(position, value);
        return this;
    }

    @Override
    public Query setParameter(int position, Calendar value, TemporalType temporalType) {
        positionalParams.put(position, new TemporalHolder(null, value, temporalType));
        return this;
    }

    @Override
    public Query setParameter(int position, Date value, TemporalType temporalType) {
        positionalParams.put(position, new TemporalHolder(value, null, temporalType));
        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        return null;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return null;
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return null;
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return null;
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return null;
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        return false;
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        return null;
    }

    @Override
    public Object getParameterValue(String name) {
        return namedParams.get(name);
    }

    @Override
    public Object getParameterValue(int position) {
        return positionalParams.get(position);
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
        return this.lockMode;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return null;
    }
}
