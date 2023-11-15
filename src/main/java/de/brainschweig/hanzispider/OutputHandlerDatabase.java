package de.brainschweig.hanzispider;

import java.util.LinkedList;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.brainschweig.interfaces.IOutputHandler;

public class OutputHandlerDatabase implements IOutputHandler {

	static private Queue<String> buffer = new LinkedList<String>();

	static final Logger logger = LogManager.getLogger(OutputHandlerDatabase.class.getName());

	private static final String name = "MySQLDatabase";

	public String getName() {
		return name;
	}

	public synchronized void addToBuffer(String bodyContent) {
		buffer.add(bodyContent);
	}

	public synchronized String getBuffer() {
		return buffer.poll();
	}

	@Override
	public void run() {
		String next = "";
		while (true) {

			next = getBuffer();

			if (null == next || next.isEmpty()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				continue;
			}

			String[] resultChunks = next.split("\\n");
			for (String resultChunk : resultChunks) {
				if (resultChunk.isEmpty()) {
					continue;
				}
				//Database.insertCrawlResult(resultChunk);
				logger.info("Result written to Database");
			}

		}
	}

}
