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

import de.brainschweig.interfaces.IOutputHandler;

public class OutputHandlerFile implements IOutputHandler {

	private static String homeDirectory = System.getProperty("user.home");

	private static String outputFileFolder = homeDirectory + File.separator + ".HanziSpider/out";
	private static Queue<String> bufferList = new LinkedList<>();
	private static int fileSize = 1024 * 100;

	static final Logger logger = LogManager.getLogger(OutputHandlerFile.class.getName());

	private static final String NAME = "File";

	public String getName() {
		return NAME;
	}

	public synchronized void addToBuffer(String bodyContent) {
		bufferList.add(bodyContent);
	}

	public synchronized String getBuffer() {
		return bufferList.poll();
	}

	@Override
	public void run() {
		boolean goOn = true;
		while (goOn) {
			StringBuilder buffer = new StringBuilder();

			do {
				String next = getBuffer();
				if (null == next || next.isEmpty()) {
					waitMs(1000);
					continue;
				}
				buffer.append(next);
				logger.info("Data consumed and added. Buffersize now: {}", buffer.length());

				waitMs(100);

			} while (buffer.length() < fileSize);

			Writer out = null;
			long sfileName = System.currentTimeMillis();
			String fileName = String.valueOf(sfileName);

			try {
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFileFolder.concat(File.separator).concat(fileName)),
						StandardCharsets.UTF_8));
				out.write(buffer.toString());
				out.close();
				logger.info("Written to file: '{}'", fileName);

			} catch (IOException | NullPointerException e) {
				logger.error("IOException: {}", e.toString());
			}
		}
	}

	private void waitMs(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			logger.error("Failed to sleep {} ms: {}", ms, e);
		}
	}

}
