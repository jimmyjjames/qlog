package com.quui.qlog.core.log4j;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the appender.
 * @author Max Kugland, Fabian Steeg
 */
public class AppenderTests {
	private static Logger logger = Logger.getRootLogger();

	@Before
	public void setup() {
		try {
			SimpleLayout layout = new SimpleLayout();
			ConsoleAppender consoleAppender = new ConsoleAppender(layout);
			Appender qlogAppender = new Appender();
			logger.addAppender(qlogAppender);
			logger.addAppender(consoleAppender);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	@Test
	public void testSmall() {
		print();
	}

	//@Test //Enable for stress testing, disable for demo mode
	public void testLots() {
		print();
		for (int i = 0; i < 50; i++) {
			logger.debug("Debug message");
			sleep();
		}
		print();
	}

	private void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void print() {
		logger.debug("Debug message");
		logger.info("Info message");
		logger.warn("Warn message");
		logger.error("Error message");
		logger.fatal("Fatal message");
	}
}
