package de.brainschweig.hanzispider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HanziSpider {

	public static void main(String[] args) throws IOException {

		// Constants
		final String DIR_EXISTING = "Directory {} exists";
		final String HANZISPIDER = "Hanzispider";

		// Start logger
		final Logger logger = LogManager.getLogger(HanziSpider.class.getName());

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
			if (f.exists() && f.isDirectory()) {
				logger.info(DIR_EXISTING, destinationDirectory);
			} else {
				createDir(logger, destinationDirectory, f);
			}

			File fl = new File(logDirectory);
			if (fl.exists() && fl.isDirectory()) {
				logger.info(DIR_EXISTING, logDirectory);
			} else {
				createDir(logger, logDirectory, fl);
			}

			File fo = new File(outDirectory);
			if (fo.exists() && fo.isDirectory()) {
				logger.info(DIR_EXISTING, outDirectory);
			} else {
				createDir(logger, outDirectory, fo);
			}

			Thread ofh = new Thread(new OutputFileHandler(), "OutFileHandler");
			ofh.start();

			List<Thread> lt = new ArrayList<>();

			// Spawn threads
			for (int a = 0; a < 5; a++) {
				lt.add(new Thread(new ProcessLoop(), "Joern-" + a));
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
