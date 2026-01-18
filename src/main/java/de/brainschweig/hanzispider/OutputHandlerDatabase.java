package de.brainschweig.hanzispider;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.hanzispider.interfaces.IOutputHandler;

public class OutputHandlerDatabase implements IOutputHandler {

	static private Queue<String> buffer = new LinkedList<String>();

	static final Logger logger = LogManager.getLogger(OutputHandlerDatabase.class.getName());

	private static final String name = "MySQLDatabase";

	public String getName() {
		return name;
	}

	public synchronized void addToBuffer(String bodyContent) {
		if (bodyContent != null) {
			buffer.add(bodyContent);
		}
	}

	public static synchronized void addToBufferStatic(String bodyContent) {
		if (bodyContent != null) {
			buffer.add(bodyContent);
		}
	}

	public synchronized String getBuffer() {
		return buffer.poll();
	}

	@Override
	public void run() {
		String next = "";
		while (!Thread.currentThread().isInterrupted()) {

			next = getBuffer();

			if (null == next || next.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.info("Thread interrupted, stopping.");
					Thread.currentThread().interrupt();
					break;
				}
				continue;
			}

			String[] resultChunks = next.split("\\n");
			for (String resultChunk : resultChunks) {
				if (resultChunk.isEmpty()) {
					continue;
				}
				Database.insertCrawlResult(resultChunk);
				logger.info("Result written to Database");
			}

		}
	}

}
