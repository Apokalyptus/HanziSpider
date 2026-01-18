package de.brainschweig.hanzispider;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HanziSpider {

	public static void main(String[] args) throws IOException {

		final String DIR_EXISTING = "Directory {} exists";
		final String HANZISPIDER = "Hanzispider";

		// Start logger
		final Logger logger = LogManager.getLogger(HanziSpider.class.getName());

		// Fetch configuration from environment variables with defaults
		final int maxThreads = getEnvInt("MAX_THREADS", 10);
		final int sleepIntervalMs = getEnvInt("SLEEP_INTERVAL_MS", 5000);
		final String webHandler = getEnv("WEBHANDLER", "jsoup");
		final String outputHandler = getEnv("OUTPUTHANDLER", "mysqldatabase");
		final String proxy = getEnv("PROXY", null);
		final String proxyPort = getEnv("PROXYPORT", null);
		final String connectionString = System.getenv("DB_CONNECTION_STRING"); // Handled in HibernateUtil

		logger.info("--- Configuration ---");
		logger.info("MAX_THREADS: {}", maxThreads);
		logger.info("SLEEP_INTERVAL_MS: {}", sleepIntervalMs);
		logger.info("WEBHANDLER: {}", webHandler);
		logger.info("OUTPUTHANDLER: {}", outputHandler);
		logger.info("PROXY: {}", proxy != null ? proxy : "not set");
		logger.info("PROXYPORT: {}", proxyPort != null ? proxyPort : "not set");
		logger.info("---------------------");

		if (connectionString == null || connectionString.isEmpty()) {
			logger.error("FATAL: Environment Variable DB_CONNECTION_STRING is not set.");
			System.exit(-1);
		}

		try {
			// Create a scheduled thread pool
			final ScheduledExecutorService executor = Executors.newScheduledThreadPool(maxThreads);

			// get Home Dir
			String homeDirectory = System.getProperty("user.home");
			String destinationDirectory = homeDirectory.concat(File.separator).concat(HANZISPIDER);
			String logDirectory = destinationDirectory.concat(File.separator).concat("log");
			String outDirectory = destinationDirectory.concat(File.separator).concat("out");

			// Create ~/.HanziSpider if not exists
			checkForExistanceOrCreate(DIR_EXISTING, logger, destinationDirectory, new File(destinationDirectory));
			checkForExistanceOrCreate(DIR_EXISTING, logger, logDirectory, new File(logDirectory));
			checkForExistanceOrCreate(DIR_EXISTING, logger, outDirectory, new File(outDirectory));

			// Add a shutdown hook to close the executor and session factory
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				logger.info("Initiating shutdown...");
				executor.shutdown();
				try {
					if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
						logger.warn("Executor did not terminate in 60 seconds. Forcing shutdown...");
						executor.shutdownNow();
						if (!executor.awaitTermination(60, TimeUnit.SECONDS))
							logger.error("Executor did not terminate even after forcing.");
					}
				} catch (InterruptedException ie) {
					executor.shutdownNow();
					Thread.currentThread().interrupt();
				}
				logger.info("Shutting down Hibernate SessionFactory...");
				HibernateUtil.shutdown();
				logger.info("Shutdown complete.");
			}));

			Thread ofh = new Thread(new OutputHandlerFile(), "OutFileHandler");
			ofh.start();

			// Schedule worker tasks
			for (int a = 0; a < maxThreads; a++) {
				ProcessLoop pl = new ProcessLoop(webHandler, proxy, proxyPort);
				pl.setOutputHandler(outputHandler);
				executor.scheduleWithFixedDelay(pl, 0, sleepIntervalMs, TimeUnit.MILLISECONDS);
			}

			logger.info("Total number of worker tasks scheduled: {}", maxThreads);

		} catch (Exception ex) {
			logger.error("Found Unhandled exception in main", ex);
		}
	}

	private static void checkForExistanceOrCreate(final String DIR_EXISTING, final Logger logger,
			String destinationDirectory, File f) {
		if (f.exists() && f.isDirectory()) {
			logger.info(DIR_EXISTING, destinationDirectory);
		} else {
			createDir(logger, destinationDirectory, f);
		}
	}

	private static void createDir(final Logger logger, String outDirectory, File fo) {
		try {
			if (!fo.mkdir()) {
				logger.error("Directory {} could not be created. Check permissions.", outDirectory);
			}
		} catch (SecurityException e) {
			logger.error("Directory {} could not be created due to a security exception. Exiting.", outDirectory, e);
			System.exit(-1);
		}
	}

	private static String getEnv(String name, String defaultValue) {
		String value = System.getenv(name);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		return value;
	}

	private static int getEnvInt(String name, int defaultValue) {
		String value = System.getenv(name);
		if (value == null || value.trim().isEmpty()) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			LogManager.getLogger(HanziSpider.class.getName()).warn(
					"Failed to parse environment variable '{}' with value '{}'. Using default value '{}'.",
					name, value, defaultValue);
			return defaultValue;
		}
	}
}