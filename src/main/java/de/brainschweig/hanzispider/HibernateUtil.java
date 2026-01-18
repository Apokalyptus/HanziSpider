package de.brainschweig.hanzispider;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import de.brainschweig.hanzispider.entities.Result;
import de.brainschweig.hanzispider.entities.Status;
import de.brainschweig.hanzispider.entities.Url;

public class HibernateUtil {

    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Fetch configuration from environment variables with defaults
            String connectionString = getEnv("DB_CONNECTION_STRING", null);
            boolean showSql = Boolean.parseBoolean(getEnv("HIBERNATE_SHOW_SQL", "false"));
            boolean formatSql = Boolean.parseBoolean(getEnv("HIBERNATE_FORMAT_SQL", "false"));

            if (connectionString == null || connectionString.trim().isEmpty()) {
                 throw new ExceptionInInitializerError("FATAL: Environment Variable DB_CONNECTION_STRING is not set and no default is available.");
            }

            Configuration configuration = new Configuration();
            configuration.addAnnotatedClass(Url.class);
            configuration.addAnnotatedClass(Status.class);
            configuration.addAnnotatedClass(Result.class);

            // Dynamically set dialect and driver based on connection string
            if (connectionString.startsWith("jdbc:postgresql:")) {
                configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            } else if (connectionString.startsWith("jdbc:mysql:") || connectionString.startsWith("jdbc:mariadb:")) {
                configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
                configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
            } else {
                throw new ExceptionInInitializerError(
                        "Unsupported database type. DB_CONNECTION_STRING must start with 'jdbc:postgresql:' or 'jdbc:mysql:'.");
            }

            configuration.setProperty("hibernate.connection.url", connectionString);
            configuration.setProperty("hibernate.hbm2ddl.auto", getEnv("HIBERNATE_HBM2DDL_AUTO", "update"));
            configuration.setProperty("hibernate.show_sql", String.valueOf(showSql));
            configuration.setProperty("hibernate.format_sql", String.valueOf(formatSql));
            configuration.setProperty("hibernate.id.new_generator_mappings", "false");

            // Build and return the SessionFactory
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }

    private static String getEnv(String name, String defaultValue) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        // Remove surrounding quotes if present
        value = value.trim();
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        } else if (value.startsWith("'") && value.endsWith("'") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
}

