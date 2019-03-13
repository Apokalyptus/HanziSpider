package de.brainschweig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class HanzeSpider {

	public static void main(String[] args) throws IOException {

		// Start logger
		final Logger logger = LogManager.getLogger(HanzeSpider.class.getName());
		
		try {
			Database.loadDriver();
			Database.connect();

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

			Thread ofh = new Thread(new OutputFileHandler(), "OutFileHandler");
			ofh.start();

			List<Thread> lt = new ArrayList<Thread>();

			// Spawn threads
			for (int a = 0; a < 25; a++) {
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
