package de.brainschweig.hanzispider;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutputFileHandler implements Runnable {

	private static String homeDirectory = System.getProperty("user.home");

	private static String outputFileFolder = homeDirectory + File.separator + ".HanziSpider/out";
	private static Queue<String> buffer = new LinkedList<>();
	private static int fileSize = 1024 * 100;

	static final Logger logger = LogManager.getLogger(OutputFileHandler.class.getName());

	public static synchronized void add(String bodyContent) {
		buffer.add(bodyContent);
	}

	public static synchronized String get() {
		return buffer.poll();
	}

	@Override
	public void run() {
		while (true) {
			StringBuilder buffer = new StringBuilder();

			do {
				String next = get();
				if (null == next || next.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					continue;
				}
				buffer.append(next);
				logger.info("Data consumed and added. Buffersize now: {}", buffer.length());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Failed to sleep 1000ms", e);
				}
			} while (buffer.length() < fileSize);

			long sfileName = System.currentTimeMillis();
			String fileName = String.valueOf(sfileName);

			try (Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFileFolder + File.separator + fileName), StandardCharsets.UTF_8))) {
				out.write(buffer.toString());
			} catch (IOException e) {
				logger.error("IOException", e);
				e.printStackTrace();
			}

			logger.info("Written to file: '{}'", fileName);

		}
	}

}
