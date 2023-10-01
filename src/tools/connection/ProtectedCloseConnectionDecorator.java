package tools.connection;

import java.sql.Connection;
import java.sql.SQLException;

public class ProtectedCloseConnectionDecorator extends ConnectionDecorator {

    public ProtectedCloseConnectionDecorator(Connection impl) {
        super(impl);
    }

    @Override
    public void close() throws SQLException {
        close(false);
    }

    public void close(final boolean isAllowed) throws SQLException {
        if (isAllowed) {
            impl.close();
        }
    }
}
