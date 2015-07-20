package com.coverity.security.sql;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import java.util.*;

class EnhancedNamedQuery extends EnhancedQuery {

    private static class NamedParameter<T> implements Parameter<T> {

        private final String name;
        private final Class<T> type;

        public NamedParameter(String name) {
            this(name, null);
        }
        public NamedParameter(String name, Class<T> type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Integer getPosition() {
            return null;
        }

        @Override
        public Class<T> getParameterType() {
            return type;
        }
    }

    private final String[] parameterNames;

    private Map<String, Object> objParams;
    private Map<String, Date> dateParams;
    private Map<String, Calendar> calParams;
    private Map<String, TemporalType> temporalTypes;
    private Map<String, String> identifiers;
    private final String[] queryPieces;

    EnhancedNamedQuery(EntityManager entityManager, boolean isNative, String[] queryPieces, String[] parameterNames) {
        super(entityManager, isNative);
        this.queryPieces = queryPieces;
        this.parameterNames = parameterNames;

        final int numParams = parameterNames.length;
        objParams = new HashMap<String, Object>(numParams);
        dateParams = new HashMap<String, Date>(numParams);
        calParams = new HashMap<String, Calendar>(numParams);
        temporalTypes = new HashMap<String, TemporalType>(numParams);
        identifiers = new HashMap<String, String>(numParams);
    }

    @Override
    public <T> EnhancedQuery setParameter(Parameter<T> param, T value) {
        String name = param.getName();
        if (name == null) {
            throw new IllegalArgumentException("Named parameter expected.");
        }
        return setParameter(name, value);
    }

    @Override
    public EnhancedQuery setParameter(Parameter<Calendar> param, Calendar value, TemporalType temporalType) {
        String name = param.getName();
        if (name == null) {
            throw new IllegalArgumentException("Named parameter expected.");
        }
        return setParameter(name, value, temporalType);
    }

    @Override
    public EnhancedQuery setIdentifier(int position, String value) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public EnhancedQuery setIdentifier(String name, String value) {
        validateIdentifier(value);
        identifiers.put(name, value);
        return this;
    }

    @Override
    public EnhancedQuery setIdentifiers(int position, String[] values) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public EnhancedQuery setIdentifiers(String name, String[] values) {
        if (values.length == 0) {
            throw new IllegalArgumentException("Identifiers list cannot be empty.");
        }
        StringBuilder sb = new StringBuilder();
        validateIdentifier(values[0]);
        sb.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            validateIdentifier(values[i]);
            sb.append(", ").append(values[i]);
        }
        identifiers.put(name, sb.toString());
        return this;
    }

    @Override
    public Query setParameter(Parameter<Date> param, Date value, TemporalType temporalType) {
        String name = param.getName();
        if (name == null) {
            throw new IllegalArgumentException("Named parameter expected.");
        }
        return setParameter(name, value, temporalType);
    }

    @Override
    public EnhancedQuery setParameter(String name, Object value) {
        objParams.put(name, value);
        return this;
    }

    @Override
    public EnhancedQuery setParameter(String name, Calendar value, TemporalType temporalType) {
        calParams.put(name, value);
        temporalTypes.put(name, temporalType);
        return this;
    }

    @Override
    public EnhancedQuery setParameter(String name, Date value, TemporalType temporalType) {
        dateParams.put(name, value);
        temporalTypes.put(name, temporalType);
        return this;
    }

    @Override
    public EnhancedQuery setParameter(int position, Object value) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public EnhancedQuery setParameter(int position, Calendar value, TemporalType temporalType) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public EnhancedQuery setParameter(int position, Date value, TemporalType temporalType) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public Set<Parameter<?>> getParameters() {
        Set<Parameter<?>> paramSet = new HashSet<Parameter<?>>(parameterNames.length);
        for (String parameter : parameterNames) {
            paramSet.add(new NamedParameter<Object>(parameter));
        }
        return paramSet;
    }

    @Override
    public Parameter<?> getParameter(String name) {
        return new NamedParameter<Object>(name);
    }

    @Override
    public <T> Parameter<T> getParameter(String name, Class<T> type) {
        return new NamedParameter<T>(name, type);
    }

    @Override
    public Parameter<?> getParameter(int position) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public <T> Parameter<T> getParameter(int position, Class<T> type) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    public boolean isBound(Parameter<?> param) {
        final String name = param.getName();
        if (name == null) {
            throw new IllegalArgumentException("Named parameter expected.");
        }
        return objParams.containsKey(name) || dateParams.containsKey(name) || calParams.containsKey(name);
    }

    @Override
    public <T> T getParameterValue(Parameter<T> param) {
        final String name = param.getName();
        if (name == null) {
            throw new IllegalArgumentException("Named parameter expected.");
        }
        return (T)getParameterValue(name);
    }

    @Override
    public Object getParameterValue(String name) {
        Object result = objParams.get(name);
        if (result == null) {
            result = dateParams.get(name);
            if (result == null) {
                result = calParams.get(name);
            }
        }
        return result;
    }

    @Override
    public Object getParameterValue(int position) {
        throw new IllegalArgumentException("Named parameter expected.");
    }

    @Override
    protected final Query buildQuery(EntityManager entityManager, boolean isNative) {
        StringBuilder sb = new StringBuilder().append(queryPieces[0]);
        for (int i = 0; i < parameterNames.length; i++) {
            final String param = parameterNames[i];
            String identifier = identifiers.get(param);
            if (identifier != null) {
                sb.append(identifier);
            } else {
                sb.append(':').append(param);
            }
            sb.append(queryPieces[i+1]);
        }

        Query query;
        if (isNative) {
            query = entityManager.createNativeQuery(sb.toString());
        } else {
            query = entityManager.createQuery(sb.toString());
        }

        for (int i = 0; i < parameterNames.length; i++) {
            final String param = parameterNames[i];
            if (identifiers.containsKey(param)) {
                continue;
            }

            if (objParams.containsKey(param)) {
                query.setParameter(param, objParams.get(param));
            } else if (dateParams.containsKey(param)) {
                query.setParameter(param, dateParams.get(param), temporalTypes.get(param));
            } else if (calParams.containsKey(param)) {
                query.setParameter(param, calParams.get(param), temporalTypes.get(param));
            }
        }

        return query;
    }
}
