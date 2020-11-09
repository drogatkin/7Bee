// $Id: Configuration.java,v 1.41 2011/08/31 04:03:05 dmitriy Exp $
// Bee Copyright (c) 2004 Dmitriy Rogatkin
package org.bee.processor;

/** @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
import java.util.Arrays;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Comparator;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static java.util.logging.Level.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;

import static org.bee.util.Logger.*;
import org.bee.util.InfoHolder;
import org.bee.util.Misc;
import org.bee.util.NullPrintStream;
import org.bee.util.StreamCatcher;
import org.bee.util.InFeeder;
import org.bee.util.LoggerConfig;

public class Configuration<FRIEND> {
	public static final String EXTENSIONS_PACKAGE = "org.bee.ext.";

	public static String OPERATORS_PACKAGE = "org.bee.oper.";

	public static String FUNCTIONS_PACKAGE = "org.bee.func.";

	public static final String OPERATOR_METHOD_NAME = "doOperator";

	public static final String FUNCTION_METHOD_NAME = "eval";

	public static final String[] RESERVED = { "-help", "-h", "-version", "-diagnostics", "-quiet", "-q", "-verbose",
			"-v", "-debug", "-d", "-lib", "-logfile", "-l", "-logger", "-listener", "-noinput", "-buildfile", "-file",
			"-f", "-keep-going", "-k", "-propertyfile", "-xpropertyfile", "-inputhandler", "-find", "-s", "-grammar",
			"-g", "-", "-r", "-th", "-targethelp", "--" };

	public String beeFile;

	protected Map<String, String> parameters;

	protected Map<String, String> defines;

	protected List<String> notRecognizableParam;

	protected Map<String, Object> reserved;

	protected Properties extraProperties;

	public List<String> targets;

	protected List<String> arguments;

	// friend
	Integer exitCode;

	public InfoHolder[] descriptors = { new InfoHolder<String, String, Object>("bee", "org.bee.processor.Bee"),
			new InfoHolder<String, String, Object>("bee/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/parameter/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/block/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/then/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/else/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("bee/target/dependency/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/switch/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/if/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/for/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("*/if/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("*/if/then", "org.bee.processor.Then"),
			new InfoHolder<String, String, Object>("*/if/else", "org.bee.processor.Else"),
			new InfoHolder<String, String, Object>("bee/target/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("*/switch/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("bee/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("*/task/onexception/block", "org.bee.processor.Block"),
			new InfoHolder<String, String, Object>("*/variable", "org.bee.processor.Variable"),
			new InfoHolder<String, String, Object>("*/value", "org.bee.processor.Value"),
			new InfoHolder<String, String, Object>("*/echo", "org.bee.processor.Echo"),
			new InfoHolder<String, String, Object>("*/target/dependency", "org.bee.processor.Dependency"),
			new InfoHolder<String, String, Object>("*/task/parameter", "org.bee.processor.Parameter"),
			new InfoHolder<String, String, Object>("*/function/parameter", "org.bee.processor.Parameter"),
			new InfoHolder<String, String, Object>("*/block/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/then/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/else/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/expression/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/operator/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/parameter/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/parameter/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("bee/target/dependency/function", "org.bee.processor.Function"),
			new InfoHolder<String, String, Object>("*/expression/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/block/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/then/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/else/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/operator/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/dependency/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/block/interrupt", "org.bee.processor.Interrupt"),
			new InfoHolder<String, String, Object>("*/then/interrupt", "org.bee.processor.Interrupt"),
			new InfoHolder<String, String, Object>("*/else/interrupt", "org.bee.processor.Interrupt"),
			new InfoHolder<String, String, Object>("*/for/interrupt", "org.bee.processor.Interrupt"),
			new InfoHolder<String, String, Object>("*/expression/for", "org.bee.processor.For"),
			new InfoHolder<String, String, Object>("bee/target/for", "org.bee.processor.For"),
			new InfoHolder<String, String, Object>("*/block/for", "org.bee.processor.For"),
			new InfoHolder<String, String, Object>("*/then/for", "org.bee.processor.For"),
			new InfoHolder<String, String, Object>("*/else/for", "org.bee.processor.For"),
			new InfoHolder<String, String, Object>("*/value", "org.bee.processor.Value"),
			new InfoHolder<String, String, Object>("*/block/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/then/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/else/if", "org.bee.processor.If"),
			//new InfoHolder<String, String, Object>("*/then/if", "org.bee.processor.If"),
			//new InfoHolder<String, String, Object>("*/else/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/expression/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/onexit/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/task/onexit", "org.bee.processor.OnExit"),
			new InfoHolder<String, String, Object>("*/task/onexception", "org.bee.processor.OnException"),
			new InfoHolder<String, String, Object>("bee/target", "org.bee.processor.Target"),
			new InfoHolder<String, String, Object>("*/block/task", "org.bee.processor.Task"),
			new InfoHolder<String, String, Object>("*/for/task", "org.bee.processor.Task"),
			new InfoHolder<String, String, Object>("*/for/if", "org.bee.processor.If"),
			new InfoHolder<String, String, Object>("*/block/switch", "org.bee.processor.Switch"),
			new InfoHolder<String, String, Object>("*/expression/switch", "org.bee.processor.Switch"),
			new InfoHolder<String, String, Object>("*/for/operator", "org.bee.processor.Operator"),
			new InfoHolder<String, String, Object>("*/for/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("*/operator/expression", "org.bee.processor.Expression"),
			new InfoHolder<String, String, Object>("bee/target/task", "org.bee.processor.Task") };

	public static String CONFIG_FILE_PROP = "java.util.logging.config.file";

	public static Configuration readConfiguration(String... args) {
		return new Configuration(args);
	}

	public Configuration(String... args) {
		parameters = new HashMap<String, String>() {
			public String put(String key, String val) {
				if (reserved.containsKey(key) == false) {
					if (notRecognizableParam == null)
						notRecognizableParam = new ArrayList<String>();
					notRecognizableParam.add(key);
				}
				return super.put(key, val);
			}
		};
		defines = new HashMap<String, String>();
		targets = new ArrayList<String>();
		initReserved();
		String key = null;
		boolean inRunArgs = false;
		for (String arg : args) {
			if (inRunArgs) {
				if (arguments == null)
					arguments = new ArrayList<String>();
				arguments.add(arg);
			} else {
				if (arg.startsWith("-")) {
					if (key != null)
						parameters.put(key, "");
					if (arg.equals("--")) {
						inRunArgs = true;
						continue;
					}
					if (arg.startsWith("-D")) {
						int ep = arg.indexOf('=');
						if (ep > 0) {
							key = null;
							defines.put(arg.substring(2, ep), arg.substring(ep + 1));
							continue;
						}
					}
					key = arg;
				} else if (key != null) {
					if (key.startsWith("-D"))
						defines.put(key.substring(2), arg);
					else
						parameters.put(key, arg);
					key = null;
				} else
					targets.add(arg);
			}
		}
		if (key != null)
			parameters.put(key, "");
		setLog();
		if (parameters.get("-easter-eggs") != null)
			System.out.printf("Author: Dmitriy Rogatkin\n");
		if (parameters.get("-version") != null) {
			System.out.printf("7Bee version %d.%d.%d compiled on %s\r\n", 1, 1, 2, org.bee.CompileStamp.getStamp());
			exit(0);
		}
		if (parameters.get("-h") != null || parameters.get("-help") != null)
			printHelp();
		if (exitCode != null)
			return;
		String extGrammar = parameters.get("-g");
		if (extGrammar == null)
			extGrammar = parameters.get("-grammar");
		if (extGrammar != null)
			reloadGrammar(extGrammar);
		beeFile = parameters.get("-f");
		if (beeFile == null)
			beeFile = parameters.get("-file");
		if (beeFile == null)
			beeFile = parameters.get("-buildfile");
		boolean searchUp = false;
		File currentDir = null;
		if (beeFile == null) {
			beeFile = parameters.get("-s");
			if (beeFile == null)
				beeFile = parameters.get("-find");
			if (beeFile != null) {
				searchUp = true;
				if (beeFile.length() == 0)
					beeFile = null;
				else if (new File(beeFile).exists() == false) {
					currentDir = new File("./").getAbsoluteFile().getParentFile();
					while (currentDir != null && new File(currentDir, beeFile).exists() == false) {
						currentDir = currentDir.getParentFile();
					}
				}
			}
		}
		if (beeFile == null) {
			try {
				final Pattern p = Pattern.compile("bee[^/\\?:*]*.xml");
				do {
					String[] beeFiles = (currentDir == null ? new File("./") : currentDir).list(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return p.matcher(name).matches();
						}
					});
					if (beeFiles.length > 0) {
						// TODO possible to process all matching build scripts file names
						Arrays.sort(beeFiles, new Comparator<String>() {
							public int compare(String o1, String o2) {
								return o1.substring(0, o1.length()-4).compareTo(o2.substring(0, o2.length()-4));
							}
						});
						
						//System.out.println(Arrays.toString(beeFiles));
						beeFile = beeFiles[0];
						break;
					} else {
						if (currentDir == null)
							currentDir = new File("./").getAbsoluteFile();
						currentDir = currentDir.getParentFile();
					}
				} while (searchUp && currentDir != null);
			} catch (NullPointerException npe) {
				logger.log(SEVERE, "Can't reach current directory.");
			} catch (ArrayIndexOutOfBoundsException aioobe) {
				logger.log(SEVERE, "No any files in current directory matching to build file pattern.");
			}
		}
		if (searchUp && currentDir != null) {
			System.setProperty("user.dir", currentDir.toString());
			List<String> cmdLine = new ArrayList<String>();
			cmdLine.add(System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java");
			cmdLine.add("-DJAVA_HOME=" + System.getProperty("JAVA_HOME"));
			cmdLine.add("-jar");
			cmdLine.add(new File(System.getenv("BEE_HOME"), "lib" + File.separatorChar + "bee.jar").toString());
			for (String arg : args)
				cmdLine.add(arg);
			try {
				// Process p = Runtime.getRuntime().exec((String[])cmdLine.toArray(new String[cmdLine.size()]), null, currentDir);
				ProcessBuilder pb = new ProcessBuilder(cmdLine);
				pb.directory(currentDir);
				logger.log(FINEST, "restart:" + cmdLine);
				final Process p = pb.start();
				new StreamCatcher(p.getErrorStream(), System.err).start();
				new StreamCatcher(p.getInputStream(), System.out).start();
				InFeeder ifr = new InFeeder(System.in, p.getOutputStream());
				Runtime.getRuntime().addShutdownHook(new Thread() {
					public void run() {
						p.destroy();
					}
				});
				ifr.start();
				try {
					p.waitFor();
				} catch (InterruptedException ie) {
					//
				}
				ifr.terminate();
				System.exit(0);
			} catch (IOException ioe) {
				logger.log(SEVERE, "Couldn't change current directory by restarting." + ioe);
			}
		}
		readExtraProperties();
		// TODO: command options need to be implemented: -lib, -inputhandler
		// System.out.printf("Work dir %s set %s\n", new File("./").getAbsolutePath(), System.getProperty("user.dir"));
		// TODO: check for dtd config file to fill descriptors
		logger.log(CONFIG, "Build file {0} in {1}", new Object[] { beeFile, System.getProperty("user.dir") });
		if (notRecognizableParam != null)
			logger.severe("The following options weren't recognized " + notRecognizableParam);
	}

	public List<String> getArguments(FRIEND firend) {
		return arguments;
	}

	public Map<String, String> getDefines(FRIEND firend) {
		return defines;
	}

	public Properties getExtraProperties(FRIEND firend) {
		return extraProperties;
	}

	// ////////////////// friend methods //////////////////////
	boolean isKeepGoing() {
		return parameters.get("-k") != null || parameters.get("-keep-going") != null;
	}

	boolean isNoInput() {
		return parameters.get("-noinput") != null;
	}

	boolean isReevaluate() {
		return parameters.get("-r") != null;
	}
	
	boolean isTargetHelp() {
		return parameters.get("-th") != null || parameters.get("-targethelp") != null;
	}

	/**
	 * reads lib command line argument to add more classes in CP considers its as pathSeparator separated string, looks in directories
	 * 
	 */
	List<URL> getExtendClassPath() {
		String extraClassPaths = parameters.get("-lib");
		if (extraClassPaths == null)
			return null;
		String libPaths[] = extraClassPaths.split(File.pathSeparator);
		List<URL> extraUrls = new ArrayList<URL>();
		for (String path : libPaths) {
			File filePath = new File(path);
			if (filePath.isDirectory()) {
				File[] files = filePath.listFiles(new FilenameFilter() {
					public boolean accept(File arg0, String name) {
						return Misc.hasValidClassLibExtension(name);
					}
				});
				for (File file : files) {
					try {
						extraUrls.add(file.toURL());
					} catch (MalformedURLException e) {
						logger.warning("Skipped :'" + file + "', because:" + e);
					}
				}
			} else if (filePath.isFile())
				if (Misc.hasValidClassLibExtension(filePath.getName()))
					try {
						extraUrls.add(filePath.toURL());
					} catch (MalformedURLException e) {
						logger.warning("Skipped :'" + filePath + "', because:" + e);
					}
		}
		return extraUrls;
	}

	// //////////////////////////////////////////////////////////////////

	protected void initReserved() {
		reserved = new HashMap<String, Object>();
		for (String rw : RESERVED)
			reserved.put(rw, null);
	}

	protected void readExtraProperties() {
		String propFile = parameters.get("-propertyfile");
		if (propFile != null) {
			try {
				extraProperties = new Properties();
				extraProperties.load(new FileInputStream(propFile));
			} catch (IOException ioe) {
				logger.log(SEVERE, "Specified properties file {0} can''t be read due {1}",
						new Object[] { propFile, ioe });
			}
		}
		propFile = parameters.get("-xpropertyfile");
		if (propFile != null) {
			try {
				Properties xmlProperties = new Properties();
				xmlProperties.loadFromXML(new FileInputStream(propFile));
				if (extraProperties != null)
					extraProperties.putAll(xmlProperties);
				else
					extraProperties = xmlProperties;
			} catch (IOException ioe) {
				logger.log(SEVERE, "Specified properties file {0} can''t be read due {1}",
						new Object[] { propFile, ioe });
			}
		}
	}

	protected void setLog() {
		LoggerConfig lc = null;
		Level logLevel = SEVERE;
		if (parameters.get("-diagnostics") != null)
			logLevel = FINEST;
		if (parameters.get("-verbose") != null || parameters.get("-v") != null)
			logLevel = FINER;
		if (parameters.get("-debug") != null || parameters.get("-d") != null)
			logLevel = FINE;
		if (parameters.get("-quiet") != null || parameters.get("-q") != null) {
			logLevel = null;
			System.setOut(new NullPrintStream(System.out));
			System.setErr(System.out);
		}
		String paramValue = parameters.get("-logger");
		if (paramValue != null)
			addLoggerClass(paramValue);

		boolean needLogConfigRead = logLevel != SEVERE;
		paramValue = parameters.get("-logfile");
		if (paramValue == null)
			paramValue = parameters.get("-l");
		needLogConfigRead |= paramValue != null;
		if (needLogConfigRead == false) {
			if (System.getProperty(CONFIG_FILE_PROP) == null && System.getenv(CONFIG_FILE_PROP) != null) {
				System.setProperty(CONFIG_FILE_PROP, System.getenv(CONFIG_FILE_PROP));
				needLogConfigRead = true;
			}
		} else {
			lc = new LoggerConfig();
			lc.setLogLevel(logLevel);
			if (paramValue != null)
				lc.setLogFile(paramValue);
			String logConfigFile = lc.create();
			if (logConfigFile != null) {
				System.setProperty(CONFIG_FILE_PROP, logConfigFile);
				// System.out.println("Config "+logConfigFile);
			}
		}
		if (needLogConfigRead)
			try {
				LogManager.getLogManager().readConfiguration();
			} catch (IOException ioe) {
				logger.setLevel(WARNING);
				logger.warning("Logger configuration cannot be read, " + ioe);
			}
		logger.setLevel(logLevel);
		// set some additional log parameters
	}

	protected void reloadGrammar(String fileName) {
		Properties grammarProps = new Properties();
		try {
			grammarProps.load(new FileInputStream(fileName));
			descriptors = new InfoHolder[grammarProps.size()];
			Enumeration<String> e = (Enumeration<String>) grammarProps.propertyNames();
			for (int i = 0; e.hasMoreElements(); i++) {
				String parsePath = e.nextElement();
				descriptors[i] = new InfoHolder<String, String, Object>(parsePath, grammarProps.getProperty(parsePath));
			}
		} catch (IOException ioe) {
			logger.severe("Grammar can't be reloaded from " + fileName + ", because " + ioe);
		}
	}

	protected void printHelp() {
		try {
			ResourceBundle resBundle = ResourceBundle.getBundle("bee", Locale.getDefault() /* , ClassLoader */);
			System.out.println(resBundle.getString("help"));
		} catch (MissingResourceException mre) {
			logger.severe("Resource bee is missed in the current locale.");
		}
		exit(1);
	}

	protected void exit(int code) {
		exitCode = code;
	}
}