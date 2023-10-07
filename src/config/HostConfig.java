package config;

public class HostConfig {
    // Maplestory Server Host: Defaults to loopback address.
    public String HOST = "127.0.0.1";

    // Persistent Layer Host: Defaults to jdbc mysql on localhost 3306
    public String DB_URL = "jdbc:mysql://localhost:3306/heavenms";

    // Data Access Credentials: Defaults to root without password
    public String DB_USER = "root";
    public String DB_PASS = "";

    // Re-use DB connections by default
    public boolean DB_CONNECTION_POOL = true;
}