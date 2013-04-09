package com.quui.qlog.core.log4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.quui.qlog.core.Level;

/**
 * Log4j appender for a qLog server.
 * @author Max Kugland, Fabian Steeg
 */
public class Appender extends AppenderSkeleton {
	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 6666;
	private String host;
	private int port;
	private Socket socket;
	private PrintWriter sender;

	/** Creates an appender that connects to the default server. */
	public Appender() {
		this(DEFAULT_HOST, DEFAULT_PORT);
	}

	/**
	 * @param host The host to use
	 * @param port The port to use
	 */
	public Appender(String host, int port) {
		this.host = host;
		this.port = port;
		connect();
		setReadWrite();
		sendLoginMsg();
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.log4j.AppenderSkeleton#append(org.apache.log4j.spi.LoggingEvent)
	 */
	@Override
	protected void append(LoggingEvent event) {
		String classInfo = event.getLocationInformation().getClassName() + " "
				+ event.getLocationInformation().getMethodName() + " "
				+ event.getLocationInformation().getLineNumber();
		String msg = buildMessage(event.getLevel().toString(), classInfo
				+ " >> " + event.getMessage());
		send(msg);
	}

	private void sendLoginMsg() {
		send(String.format("<login><name>%s</name></login>", this.getClass()
				.getName()));
	}

	private String buildMessage(String level, String msg) {
		return String.format(
				"<log><color>%s</color><msg><![CDATA[%s]]></msg></log>",
				colorFor(level), StringEscapeUtils.escapeJava(msg));
		/*
		 * Escaping Java here is required to support logging of messages
		 * containing Java code, which would interfere with the XML embedding
		 * the actual message.
		 */
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.log4j.AppenderSkeleton#close()
	 */
	public void close() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.log4j.AppenderSkeleton#requiresLayout()
	 */
	public boolean requiresLayout() {
		return false;
	}

	private void setReadWrite() {
		try {
			if (socket != null) {
				sender = new PrintWriter(socket.getOutputStream(), true);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private void connect() {
		try {
			socket = new Socket(host, port);
		} catch (UnknownHostException e) {
			handle(e);
		} catch (IOException e) {
			handle(e);
		}
	}

	private void handle(IOException e) {
		System.err
				.println(String
						.format(
								"%s: could not connect to qLog server running on %s, port %s: %s",
								this.getClass().getSimpleName(), host, port, e
										.getMessage()));

	}

	private void send(String msg) {
		if (sender != null) {
			sender.print(msg + "\u0000");
			sender.flush();
		}
	}

	private String colorFor(String level) {
		return Level.valueOf(level).getColor();
	}
}
