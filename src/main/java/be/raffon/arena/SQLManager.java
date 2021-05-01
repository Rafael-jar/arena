package be.raffon.arena;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;


public class SQLManager {

    private static SQLManager INSTANCE;

    private HikariDataSource hikariDataSource;

    public SQLManager(final String hostname, final int port, final String database, final String user, final String password) {
        initMySQLConnection(hostname, port, database, user, password);

        INSTANCE = this;
    }

    private void initMySQLConnection(final String hostname, final int port, final String database, final String user, final String password) {

        final HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + hostname + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false"); //
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMaxLifetime(600000L);
        hikariConfig.setLeakDetectionThreshold(300000L);
        hikariConfig.setConnectionTimeout(10000L);

        hikariDataSource = new HikariDataSource(hikariConfig);

        try {
            createTables();
        } catch (final SQLException e) {
            e.printStackTrace();
        }

    }

    public void closePool() {

        hikariDataSource.close();

    }

    public Connection getConnection() throws SQLException {

        return hikariDataSource.getConnection();

    }

    public void createTables() throws SQLException {
        //TODO CREER LES DB A LA MAIN
    }

    public void update(final String qry) {
        try {
            final Connection c = getConnection();
            final PreparedStatement s = c.prepareStatement(qry);
            s.executeUpdate();
            c.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public Object query(final String qry, final Function<ResultSet, Object> consumer) {
        try {
            final Connection c = getConnection();
            final PreparedStatement s = c.prepareStatement(qry);
            final ResultSet rs = s.executeQuery();
            final Object consumerapply = consumer.apply(rs);
            c.close();
            return consumerapply;
        } catch (final SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public void query(final String qry, final Consumer<ResultSet> consumer) {
        try {
            final Connection c = getConnection();
            final PreparedStatement s = c.prepareStatement(qry);
            final ResultSet rs = s.executeQuery();
            consumer.accept(rs);
            c.close();
        } catch (final SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    public static SQLManager getInstance() {
        return INSTANCE;
    }
}