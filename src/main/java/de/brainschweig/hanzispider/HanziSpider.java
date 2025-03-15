package de.brainschweig.hanzispider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HanziSpider {

	public static void main(String[] args) throws IOException {

		final int MAX_THREADS = 10;
		final String DIR_EXISTING = "Directory {} exists";
		final String HANZISPIDER = "Hanzispider";

		// Start logger
		final Logger logger = LogManager.getLogger(HanziSpider.class.getName());
		final String connectionString = System.getenv("DB_CONNECTION_STRING");
		final String webHandler = System.getenv("WEBHANDLER");
		final String outputHandler = System.getenv("OUTPUTHANDLER");
		final String proxy = System.getenv("PROXY");
		final String proxyPort = System.getenv("PROXYPORT");

		logger.info("DB_CONNECTION_STRING: {}", connectionString);
		logger.info("WEBHANDLER: {}", webHandler);
		logger.info("OUTPUTHANDLER: {}", outputHandler);
		logger.info("PROXY: {}", proxy);
		logger.info("PROXYPORT: {}", proxyPort);

		if (connectionString == null || connectionString.isEmpty()) {
			System.out.println("ERROR: Environement Variable DB_CONNECTION_STRING is empty.");
			logger.error("Environement Variable DB_CONNECTION_STRING is empty.");
			System.exit(-1);
		}

		try {

			// get Home Dir

			String homeDirectory = System.getProperty("user.home");
			String destinationDirectory = homeDirectory.concat(File.separator).concat(HANZISPIDER);
			String logDirectory = homeDirectory.concat(File.separator).concat(HANZISPIDER).concat(File.separator)
					.concat("log");
			String outDirectory = homeDirectory.concat(File.separator).concat(HANZISPIDER).concat(File.separator)
					.concat("out");

			// Create ~/.HanziSpider if not exists
			File f = new File(destinationDirectory);
			checkForExistanceOrCreate(DIR_EXISTING, logger, destinationDirectory, f);

			File fl = new File(logDirectory);
			checkForExistanceOrCreate(DIR_EXISTING, logger, logDirectory, fl);

			File fo = new File(outDirectory);
			checkForExistanceOrCreate(DIR_EXISTING, logger, outDirectory, fo);

			Thread ofh = new Thread(new OutputHandlerFile(), "OutFileHandler");
			ofh.start();

			List<Thread> lt = new ArrayList<>();

			// Spawn threads
			for (int a = 0; a < MAX_THREADS; a++) {
				ProcessLoop pl = new ProcessLoop(webHandler, proxy, proxyPort);
				pl.setOutputHandler(outputHandler);
				lt.add(new Thread(pl, "Joern-" + a));
				logger.info("Thread 'Joern-{}' spawned.", a);

			}

			// Start threads
			lt.forEach(t -> {
				t.start();
				logger.info("Thread '{}' started.", t.getName());
			});

			logger.info("Number of Threads: {}", lt.size());

		} catch (Exception ex) {
			logger.error("Found Unhandled exception", ex);
		}
	}

	private static void checkForExistanceOrCreate(final String DIR_EXISTING, final Logger logger, String destinationDirectory, File f) {
		if (f.exists() && f.isDirectory()) {
			logger.info(DIR_EXISTING, destinationDirectory);
		} else {
			createDir(logger, destinationDirectory, f);
		}
	}

	private static void createDir(final Logger logger, String outDirectory, File fo) {
		try {
			fo.mkdir();
		} catch (SecurityException e) {
			System.out.printf("ERROR: Directory %s could not be created. Exiting.", outDirectory);
			logger.error("Directory {} could not be created. Exiting.", outDirectory);
			System.exit(-1);
		}
	}
}