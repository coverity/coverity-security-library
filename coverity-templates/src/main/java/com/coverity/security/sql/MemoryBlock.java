package com.coverity.security.sql;

class MemoryBlock {
    private final MemoryPreparedStatement.ParameterType paramType;
    private final Object[] params;

    public MemoryBlock(MemoryPreparedStatement.ParameterType paramType, Object ... params) {
        this.paramType = paramType;
        this.params = params;
    }

    public MemoryPreparedStatement.ParameterType getParamType() {
        return paramType;
    }

    public Object getParam(int index) {
        return params[index];
    }
}
