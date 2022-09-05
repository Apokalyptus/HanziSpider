package de.brainschweig.hanzespider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.lang.System;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.interfaces.IOutputHandler;

public class HanzeSpider {

	public static void main(String[] args) throws IOException {
		
		final int MAX_THREADS = 10;
		// Start logger
		final Logger logger = LogManager.getLogger(HanzeSpider.class.getName());
		final String connectionString = System.getenv("DB_CONNECTION_STRING");
		final String webHandler = System.getenv("WEBHANDLER");
		final String outputHandler = System.getenv("OUTPUTHANDLER");
		final String proxy = System.getenv("PROXY");
		final String proxyPort = System.getenv("PROXYPORT");

		logger.info("DB_CONNECTION_STRING: " + connectionString);
		logger.info("WEBHANDLER: " + webHandler);
		logger.info("OUTPUTHANDLER: " + outputHandler);
		logger.info("PROXY: " + proxy);
		logger.info("PROXYPORT: " + proxyPort);

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
			String destinationDirectory = homeDirectory + File.separator + ".HanzeSpider";
			String logDirectory = homeDirectory + File.separator + ".HanzeSpider" + File.separator + "log";
			String outDirectory = homeDirectory + File.separator + ".HanzeSpider" + File.separator + "out";

			// Create ~/.HanzeSpider if not exists
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

			IOutputHandler oh = null;

			if(outputHandler == null || outputHandler.isBlank()){
				oh = new OutputHandlerDatabase();
			} else if (outputHandler.toLowerCase().equals("MySQLDatabase".toLowerCase())){
				oh = new OutputHandlerDatabase();
			} else if (outputHandler.toLowerCase().equals("File".toLowerCase())){
				oh = new OutputHandlerFile();
			}
			
			logger.info("OutputHandler set to: " + oh.getName());
			
			Thread ofh = new Thread(oh, oh.getName());
			ofh.start();

						
			List<Thread> lt = new ArrayList<Thread>();

			// Spawn threads
			for (int a = 0; a < MAX_THREADS; a++) {
				ProcessLoop pl = new ProcessLoop(webHandler, proxy, proxyPort);
				pl.setOutputHandler(outputHandler);
				lt.add(new Thread(pl, "Joern-" + a));
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
