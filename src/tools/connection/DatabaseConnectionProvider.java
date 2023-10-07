package tools.connection;

import config.HostYamlConfig;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author Adasi
 */
@Log4j2
public class DatabaseConnectionProvider {
    private final Map<String, Integer> databaseConnectionIndicesByKey;
    private final List<ProtectedCloseConnectionDecorator> databaseConnections;
    private final List<String> databaseKeys;
    private static final int MAX_CONNS = 1;

    public Connection getConnection() throws SQLException {
        final Connection connectionToReturn;
        if(HostYamlConfig.config.DB_CONNECTION_POOL) {
            connectionToReturn = getConnectionPoolConnection();
        } else {
            connectionToReturn = getDriverManagerConnection();
        }
        return connectionToReturn;
    }

    private ProtectedCloseConnectionDecorator getConnectionPoolConnection() throws SQLException {
        final String key = getKey();
        return getConnectionPoolConnection(key);
    }

    private ProtectedCloseConnectionDecorator getConnectionPoolConnection(final String key) throws SQLException {
        if (databaseConnectionIndicesByKey.containsKey(key)) {
            final int connectionIndex = databaseConnectionIndicesByKey.get(key);
            return databaseConnections.get(connectionIndex);
        }
        final ProtectedCloseConnectionDecorator newConnection = getDriverManagerConnection();
        addDatabaseConnection(key, newConnection);
        log.info("Added database connection to cache with key {}", key);
        return newConnection;
    }

    private ProtectedCloseConnectionDecorator getDriverManagerConnection() throws SQLException {
        return new ProtectedCloseConnectionDecorator(DriverManager.getConnection(
                HostYamlConfig.config.DB_URL,
                HostYamlConfig.config.DB_USER,
                HostYamlConfig.config.DB_PASS));
    }

    private String getKey() {
        final int randomInt = ThreadLocalRandom.current().nextInt(0, MAX_CONNS);
        return String.format("DatabaseConnectionKey:%s", randomInt);
    }

    private void addDatabaseConnection(final String key, final ProtectedCloseConnectionDecorator connection) {
        if (databaseConnectionIndicesByKey.containsKey(key)) {
            return;
        }
        final int newIndex = databaseConnections.size();
        databaseConnectionIndicesByKey.put(key, newIndex);
        databaseConnections.add(connection);
        databaseKeys.add(key);
    }

    private void removeDatabaseConnection(final String key) throws SQLException {
        if (!databaseConnectionIndicesByKey.containsKey(key)) {
            return;
        }
        final int lastIndex = databaseConnections.size() - 1;
        final int indexToSwitch = databaseConnectionIndicesByKey.get(key);
        if (indexToSwitch != lastIndex) {
            swapIndices(indexToSwitch, lastIndex);
        }
        removeLast();
    }

    private void swapIndices(final int index1, final int index2) {
        final ProtectedCloseConnectionDecorator item1 = databaseConnections.get(index1);
        final ProtectedCloseConnectionDecorator item2 = databaseConnections.get(index2);
        final String key1 = databaseKeys.get(index1);
        final String key2 = databaseKeys.get(index2);
        // swap items in arrays and in map
        databaseConnections.set(index1, item2);
        databaseConnections.set(index2, item1);
        databaseKeys.set(index1, key2);
        databaseKeys.set(index2, key1);
        databaseConnectionIndicesByKey.put(key1, index2);
        databaseConnectionIndicesByKey.put(key2, index1);
    }

    private void removeLast() throws SQLException {
        final int lastIndex = databaseConnections.size() - 1;
        final ProtectedCloseConnectionDecorator connectionRemoved = databaseConnections.remove(lastIndex);
        final String keyRemoved = databaseKeys.remove(lastIndex);
        databaseConnectionIndicesByKey.remove(keyRemoved);
        if (!connectionRemoved.isClosed()) {
            connectionRemoved.close(true);
        }
    }

    public DatabaseConnectionProvider() {
        final long startTime = System.currentTimeMillis();
        this.databaseConnections = new ArrayList<>();
        this.databaseConnectionIndicesByKey = new HashMap<>();
        this.databaseKeys = new ArrayList<>();
        final long endTime = System.currentTimeMillis();
        log.info("DatabaseConnectionProvider initialized after {} ms.", endTime - startTime);
    }
}
