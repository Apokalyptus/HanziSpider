package de.brainschweig.hanzespider;

import java.io.BufferedWriter;
import java.io.File;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OutputHandlerFile implements OutputHandlerInterface {

	static private String homeDirectory = System.getProperty("user.home");

	static private String outputFileFolder = homeDirectory + File.separator + ".HanzeSpider/out";
	static private Queue<String> buffer = new LinkedList<String>();
	static private int fileSize = 1024 * 100;

	static final Logger logger = LogManager.getLogger(OutputHandlerFile.class.getName());

	public synchronized void addToBuffer(String bodyContent) {
		buffer.add(bodyContent);
	}

	public synchronized String getBuffer() {
		return buffer.poll();
	}

	@Override
	public void run() {
		while (true) {
			StringBuilder buffer = new StringBuilder();

			do {
				String next = getBuffer();
				if (null == next || next.isEmpty()) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
					continue;
				}
				buffer.append(next);
				logger.info("Data consumed and added. Buffersize now: " + buffer.length());
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Failed to sleep 1000ms", e);
				}
			} while (buffer.length() < fileSize);

			Writer out = null;
			long sfileName = System.currentTimeMillis();
			String fileName = String.valueOf(sfileName);

			try {
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFileFolder + File.separator + fileName), "UTF-8"));
				out.write(buffer.toString());

			} catch (IOException e) {
				logger.error("IOException", e);
				e.printStackTrace();
			}

			finally {
				try {
					out.close();
				} catch (IOException e) {
					logger.error("IOException", e);
					e.printStackTrace();
				}
			}

			logger.info("Written to file: '" + fileName + "'");

		}
	}

}
