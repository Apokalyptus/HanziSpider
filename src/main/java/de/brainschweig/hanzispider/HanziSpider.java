package de.brainschweig.hanzispider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.System;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HanziSpider {

	public static void main(String[] args) throws IOException {

		// Start logger
		final Logger logger = LogManager.getLogger(HanziSpider.class.getName());
		final String connectionString = System.getenv("DB_CONNECTION_STRING");

		if(connectionString == null || connectionString.isBlank()){
			System.out.println("ERROR: Environement Variable DB_CONNECTION_STRING is empty.");
			logger.error("Environement Variable DB_CONNECTION_STRING is empty.");
			System.exit(-1);
		}
		
		try {
			Database.loadDriver();
			Database.connect(connectionString);

			// get Home Dir

			String homeDirectory = System.getProperty("user.home");
			String destinationDirectory = homeDirectory + File.separator + ".HanziSpider";
			String logDirectory = homeDirectory + File.separator + ".HanziSpider" + File.separator + "log";
			String outDirectory = homeDirectory + File.separator + ".HanziSpider" + File.separator + "out";

			// Create ~/.HanziSpider if not exists
			File f = new File(destinationDirectory);
			if (f.exists() && f.isDirectory()) {
				logger.info("Directory " + destinationDirectory + " exists");
			} else {
				try {
					f.mkdir();
				} catch (SecurityException e) {
					System.out.println("ERROR: Directory " + destinationDirectory + "could not be created. Exiting.");
					logger.error("Directory " + destinationDirectory + " could not be created. Exiting.");
					System.exit(-1);
					;
				}
			}

			File fl = new File(logDirectory);
			if (fl.exists() && fl.isDirectory()) {
				logger.info("Directory " + logDirectory + " exists");
			} else {
				try {
					fl.mkdir();
				} catch (SecurityException e) {
					System.out.println("ERROR: Directory " + logDirectory + "could not be created. Exiting.");
					logger.error("Directory " + logDirectory + " could not be created. Exiting.");
					System.exit(-1);
					;
				}
			}

			File fo = new File(outDirectory);
			if (fo.exists() && fo.isDirectory()) {
				logger.info("Directory " + outDirectory + " exists");
			} else {
				try {
					fo.mkdir();
				} catch (SecurityException e) {
					System.out.println("ERROR: Directory " + outDirectory + "could not be created. Exiting.");
					logger.error("Directory " + outDirectory + " could not be created. Exiting.");
					System.exit(-1);
					;
				}
			}

			Thread ofh = new Thread(new OutputFileHandler(), "OutFileHandler");
			ofh.start();
			
			List<Thread> lt = new ArrayList<Thread>();

			// Spawn threads
			for (int a = 0; a < 5; a++) {
				lt.add(new Thread(new ProcessLoop(), "Joern-" + a));
				logger.info("Thread 'Joern-" + a + "' spawned.");

			}

			// Start threads
			lt.forEach((t) -> {
				t.start();
				logger.info("Thread '" + t.getName() + "' started.");
			});

			logger.info("Number of Threads:" + lt.size());
			
		} catch (Exception ex) {
			logger.error("Found Unhandled exception", ex);
		}
	}
}
