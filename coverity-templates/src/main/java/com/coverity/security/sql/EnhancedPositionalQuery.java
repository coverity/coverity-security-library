package com.coverity.security.sql;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.*;

class EnhancedPositionalQuery extends EnhancedQuery {

    private static class PositionalParameter<T> implements Parameter<T> {

        private final int position;
        private final Class<T> type;

        public PositionalParameter(int position) {
            this(position, null);
        }
        public PositionalParameter(int position, Class<T> type) {
            this.position = position;
            this.type = type;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Integer getPosition() {
            return position;
        }

        @Override
        public Class<T> getParameterType() {
            return type;
        }
    }

    private Object[] objParams;
    private Date[] dateParams;
    private Calendar[] calParams;
    private TemporalType[] temporalTypes;
    private String[] identifiers;
    private final String[] queryPieces;

    EnhancedPositionalQuery(EntityManager entityManager, boolean isNative, String[] queryPieces) {
        super(entityManager, isNative);
        this.queryPieces = queryPieces;

        final int numParams = queryPieces.length-1;
        objParams = new Object[numParams];
        dateParams = new Date[numParams];
        calParams = new Calendar[numParams];
        temporalTypes = new TemporalType[numParams];
        identifiers = new String[numParams];
    }

    @Override
    public <T> EnhancedQuery setParameter(Parameter<T> param, T value) {
        Integer position = param.getPosition();
        if (position == null) {
            throw new IllegalArgumentException("Positional parameter expected.");
        }
        return setParameter(position.intValue(), value);
    }

    @Override
    public EnhancedQuery setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        Integer position = param.getPosition();
        if (position == null) {
            throw new IllegalArgumentException("Positional parameter expected.");
        }
        return setParameter(position.intValue(), value, temporalType);
    }

    @Override
    public EnhancedQuery setIdentifier(int position, String value) {
        validateIdentifier(value);
        identifiers[position-1] = value;
        return this;
    }

    @Override
    public EnhancedQuery setIdentifier(String name, String value) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        Integer position = param.getPosition();
        if (position == null) {
            throw new IllegalArgumentException("Positional parameter expected.");
        }
        return setParameter(position.intValue(), value, temporalType);
    }

    @Override
    public EnhancedQuery setParameter(String name, Object value) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public EnhancedQuery setParameter(String name, Calendar value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public EnhancedQuery setParameter(String name, Date value, TemporalType temporalType) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public EnhancedQuery setParameter(int position, Object value) {
        objParams[position-1] = value;
        return this;
    }

    @Override
    public EnhancedQuery setParameter(int position, Calendar value, TemporalType temporalType) {
        calParams[position-1] = value;
        temporalTypes[position-1] = temporalType;
        return this;
    }

    @Override
    public EnhancedQuery setParameter(int position, Date value, TemporalType temporalType) {
        dateParams[position-1] = value;
        temporalTypes[position-1] = temporalType;
        return this;
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        Set<Parameter<?>> paramSet = new HashSet<Parameter<?>>(objParams.length);
        for (int i = 0; i < objParams.length; i++) {
            paramSet.add(new PositionalParameter<Object>(i+1));
        }
        return paramSet;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public Parameter<?> getParameter(int position) {
        return new PositionalParameter<Object>(position);
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        return new PositionalParameter<T>(position, type);
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        Integer position = param.getPosition();
        if (position == null) {
            throw new IllegalArgumentException("Positional parameter expected.");
        }
        int pos = position.intValue()-1;
        return objParams[pos] != null || dateParams[pos] != null || calParams[pos] != null;
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        Integer position = param.getPosition();
        if (position == null) {
            throw new IllegalArgumentException("Positional parameter expected.");
        }
        return (T)getParameterValue(position.intValue());
    }

    @Override
    public Object getParameterValue(String name) {
        throw new IllegalArgumentException("Positional parameter expected.");
    }

    @Override
    public Object getParameterValue(int position) {
        Object result = objParams[position-1];
        if (result == null) {
            result = dateParams[position-1];
            if (result == null) {
                result = calParams[position-1];
            }
        }
        return result;
    }

    @Override
    protected final Query buildQuery(EntityManager entityManager, boolean isNative) {
        StringBuilder sb = new StringBuilder().append(queryPieces[0]);
        int paramIndex = 1;
        for (int i = 0; i < identifiers.length; i++) {
            String identifier = identifiers[i];
            if (identifier != null) {
                sb.append(identifier);
            } else {
                sb.append('?').append(paramIndex++);
            }
            sb.append(queryPieces[i+1]);
        }

        Query query;
        if (isNative) {
            query = entityManager.createNativeQuery(sb.toString());
        } else {
            query = entityManager.createQuery(sb.toString());
        }

        int applyIndex = 1;
        for (int i = 0; i < objParams.length; i++) {
            if (identifiers[i] != null) {
                continue;
            }

            if (objParams[i] != null) {
                query.setParameter(applyIndex++, objParams[i]);
            } else if (dateParams[i] != null) {
                query.setParameter(applyIndex++, dateParams[i], temporalTypes[i]);
            } else if (calParams[i] != null) {
                query.setParameter(applyIndex++, calParams[i], temporalTypes[i]);
            }
        }

        return query;
    }
}
