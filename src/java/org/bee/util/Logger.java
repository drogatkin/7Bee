// $Id: Logger.java,v 1.2 2004/04/24 07:42:45 rogatkin Exp $
// Bee Copyright (c) 2004 Dmitriy Rogatkin
package org.bee.util;

/** @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
//import java.util.logging.Logger;
import java.util.logging.LogManager;

public class Logger {

	public static java.util.logging.Logger logger;

	static {
		logger = java.util.logging.Logger.getLogger("Bee" /*, resourceBundleName*/);
		LogManager.getLogManager().addLogger(logger);
	}

	public static void addLoggerClass(String className) {
		try {
			LogManager.getLogManager().addLogger(logger = (java.util.logging.Logger)Class.forName(className).newInstance());
		} catch (Error e) {
			logger.severe("An error happened at initiation or adding logger class " + className + " " + e);
		} catch (Exception e) {
			logger.severe("An exception happened at initiation or adding logger class " + className + " " + e);
		}
	}
}