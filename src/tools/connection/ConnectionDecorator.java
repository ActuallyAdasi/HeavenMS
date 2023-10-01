package tools.connection;


import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionDecorator implements Connection {
    protected final Connection impl;
    public ConnectionDecorator(final Connection impl) {
        this.impl = impl;
    }

    @Override
    public Statement createStatement() throws SQLException {
        return impl.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return impl.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return impl.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return impl.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        impl.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return impl.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        impl.commit();
    }

    @Override
    public void rollback() throws SQLException {
        impl.rollback();
    }

    @Override
    public void close() throws SQLException {
        impl.close();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return impl.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return impl.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        impl.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return impl.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        impl.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return impl.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        impl.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return impl.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return impl.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        impl.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return impl.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return impl.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return impl.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return impl.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        impl.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        impl.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return impl.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return impl.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return impl.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        impl.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        impl.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return impl.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return impl.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return impl.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return impl.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return impl.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return impl.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return impl.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return impl.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return impl.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return impl.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return impl.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        impl.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        impl.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return impl.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return impl.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return impl.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return impl.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        impl.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return impl.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        impl.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        impl.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return impl.getNetworkTimeout();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return impl.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return impl.isWrapperFor(iface);
    }
}
