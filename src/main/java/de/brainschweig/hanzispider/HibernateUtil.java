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
            String connectionString = getEnv("DB_CONNECTION_STRING", "jdbc:mysql://localhost:3306/crawler?user=root");
            boolean showSql = Boolean.parseBoolean(getEnv("HIBERNATE_SHOW_SQL", "false"));
            boolean formatSql = Boolean.parseBoolean(getEnv("HIBERNATE_FORMAT_SQL", "false"));

            if (connectionString == null || connectionString.trim().isEmpty()) {
                 throw new ExceptionInInitializerError("FATAL: Environment Variable DB_CONNECTION_STRING is not set and no default is available.");
            }

            Configuration configuration = new Configuration();
            configuration.addAnnotatedClass(Url.class);
            configuration.addAnnotatedClass(Status.class);
            configuration.addAnnotatedClass(Result.class);
            configuration.setProperty("hibernate.connection.url", connectionString);
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
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
        return value;
    }
}

