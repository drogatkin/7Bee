// $Id: Task.java,v 1.47 2007/06/06 08:03:44 rogatkin Exp $
// Bee Copyright (c) 2004 Dmitriy Rogatkin
package org.bee.processor;

import java.lang.reflect.Method;
import java.security.Policy;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.io.IOException;
import java.io.File;
import java.security.Permission;
import java.net.URLClassLoader;
import java.net.URL;
import java.net.MalformedURLException;
import static java.util.logging.Level.*;
import org.bee.util.InfoHolder;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import static org.bee.util.Logger.*;
import org.bee.util.Misc;
import org.bee.util.StreamCatcher;
import org.bee.util.InFeeder;

//import javax.tools.ToolProvider;
import java.util.spi.ToolProvider;

import javax.tools.JavaCompiler;

/**
 * @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
public class Task extends Function {
	// TODO review loggin in case of error handling
	// TODO add run exception report
	public static final String RESULT_CODE_VAR_NAME = "resultcode";

	public static final String PATH = "path";

	public static final String CLASSPATH = "CLASSPATH";

	protected String exec;

	protected String code;
	
	protected String tool;

	protected String path;

	protected String stdErrVar, stdOutVar, stdInVar;

	protected OnExit onExitHandler;
	
	protected boolean lockSM;

	protected OnException onExceptionHandler;

	public Task(String xpath) {
		super(xpath);
	}

	public void childDone(Instruction child) {
		if (child instanceof OnException) // not quite robust, since order is important
			onExceptionHandler = (OnException) child;
		else if (child instanceof OnExit)
			onExitHandler = (OnExit) child;
		else
			super.childDone(child);
	}

	public InfoHolder eval() {
		// logger.entering("task","eval");
		String pathValue = path == null ? null : lookupStringValue(path);
		InfoHolder result = null;
		InfoHolder<String, InfoHolder, Object> extCPVal = lookupOnTop(RESERVE_CLASS_LIB);
		if (exec != null) {
			String e = lookupStringValue(exec);
			if (e != null) {
				List<String> command = new ArrayList<String>(parameters.size() + 1);
				command.add(e);
				Map<String,String> addEnv = new HashMap<String,String>();
				fillParameters(command, addEnv);
				try {
					logger.fine(command.toString());
					logger.finest(System.getSecurityManager() != null ? "Security maneger set." : "");
					ProcessBuilder pb = new ProcessBuilder(command);
					Map<String, String> environment = pb.environment();
					if (pathValue != null) {
						environment.put(PATH, pathValue + File.pathSeparatorChar + environment.get(PATH));
					}
					// add class path
					if (extCPVal != null && extCPVal.getValue() != null) {
						String classPath = environment.get(CLASSPATH);
						if (classPath == null)
							classPath = (String) extCPVal.getValue().getValue();
						else
							classPath = classPath + File.pathSeparatorChar + (String) extCPVal.getValue().getValue();
						environment.put(CLASSPATH, classPath);
					}
					environment.putAll(addEnv);
					String workingDirectory = lookupStringValue(RESERVE_NAME_DIR);
					if (workingDirectory != null)
						pb.directory(new File(workingDirectory));
					else
						pb.directory(new File(System.getProperty("user.dir")));
					Process p = pb.start();
					StreamCatcher errorGobbler = new StreamCatcher(p.getErrorStream(), stdErrVar == null ? System.err
							: null);
					StreamCatcher outputGobbler = new StreamCatcher(p.getInputStream(), stdOutVar == null ? System.out
							: null);

					errorGobbler.start();
					outputGobbler.start();

					InFeeder infr = null;
					if (stdInVar != null) {
						infr = new InFeeder(lookupStringValue(stdInVar), p.getOutputStream());
						infr.start();
					} else if (lookupStringValue(RESERVE_OPTION_NOINPUT) == null) {
						infr = new InFeeder(System.in, p.getOutputStream());
						infr.start();
					}
					try {
						result = new InfoHolder<String, String, Object>("onexit", String.valueOf(p.waitFor()));
						if (infr != null) {
							infr.terminate();
							infr.join();
						}
						outputGobbler.join();
						errorGobbler.join();
					} catch (InterruptedException ie) {
					}
					// TODO: use flag to understand when out needed
					if (outputGobbler.isEmpty() == false)
						if (stdOutVar != null)
							getNameSpace()
									.inScope(
											new InfoHolder<String, InfoHolder, Object>(stdOutVar,
													new InfoHolder<String, String, Object>(stdOutVar, outputGobbler
															.toString())));
						else
							logger.finer(outputGobbler.toString());
					if (errorGobbler.isEmpty() == false)
						if (stdErrVar != null)
							getNameSpace()
									.inScope(
											new InfoHolder<String, InfoHolder, Object>(stdErrVar,
													new InfoHolder<String, String, Object>(stdErrVar, errorGobbler
															.toString())));
						else
							logger.severe(errorGobbler.toString());
					// TODO: make result code constant
					getNameSpace().inScope(new InfoHolder<String, InfoHolder, Object>(RESULT_CODE_VAR_NAME, result));
					if (onExitHandler != null)
						onExitHandler.eval();
				} catch (IOException ioe) {
					getNameSpace().inScope(
							new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_ERROR,
									new InfoHolder<String, String, IOException>(RESERVE_NAME_ERROR, ioe.getMessage(),
											ioe)));
					logger.severe("Can't start " + e + " " + ioe);
					if (onExceptionHandler != null)
						onExceptionHandler.eval();
				}
			}
		} else if(tool != null && Misc.getInt(System.getProperty("java.version"), 0) > 10) {
			int resultCode ;
			List<String> args = new ArrayList<String>(parameters.size());
			fillParameters(args, null);
			try {
				if (NAME_JAVA_COMPILER.equalsIgnoreCase(tool)) {
					JavaCompiler compiler =  javax.tools.ToolProvider.getSystemJavaCompiler();
					resultCode = compiler.run(System.in, System.out, System.err, args.toArray(new String[args.size()]));
				} else {
					ToolProvider toolExec = ToolProvider.findFirst(tool).orElseThrow();
					resultCode = toolExec.run(System.out, System.err, args.toArray(new String[args.size()]));
				}
				result = new InfoHolder<String, String, Object>(RESULT_CODE_VAR_NAME, String
						.valueOf(resultCode));
			} catch(NullPointerException  npe) {
				logger.severe("Can't find " + tool);
			}
		} else if (code != null) {
			code = lookupStringValue(code);
			int resultCode = -1;
			String workDir = null;
			Properties origSystemProps = System.getProperties();
			System.setProperties((Properties) origSystemProps.clone());
			SecurityManager origSM = System.getSecurityManager();
			// can't pull it out because has side effect on System properties
			List<String> args = new ArrayList<String>(parameters.size());
			fillParameters(args, null);
			final boolean useSM = System.getProperty("java.security.manager") != null;
			// TODO: use actually requested SM
			List<URL> extCP = (List<URL>) (extCPVal == null ? null : extCPVal.getValue() == null ? null : extCPVal
					.getValue().getType());
			ClassLoader origLoader = null;
			try {
				ClassLoader extClassLoader = null;
				Class codeClass = null;
				if (pathValue != null) {
					String[] pathComponents = pathValue.split(File.pathSeparator);
					List<URL> pathComponentUrls = new ArrayList<URL>(pathComponents.length);
					for (String pathComponent : pathComponents) {
						File pathFile = new File(pathComponent);
						try {
							if (pathFile.exists())
								pathComponentUrls.add(pathFile.toURI().toURL());
							else
								pathComponentUrls.add(new URL(pathComponent));
							pathComponentUrls.get(pathComponentUrls.size() - 1).openStream();
						} catch (MalformedURLException mfue) {
							logger.warning("Path component is not well formed, or doesn't exist => '" + pathComponent + "'(" + pathFile.exists()
									+ "), " + mfue);
						} catch (IOException ioe) {
							logger.warning("Path component " + pathComponent + " can't be accessed, " + ioe);
						}
					}
					if (extCP != null)
						pathComponentUrls.addAll(extCP);
					// logger.finest("class path:" + Arrays.asList(pathComponentUrls));
					codeClass = Class.forName(code, false, extClassLoader = new URLClassLoader(pathComponentUrls
							.toArray(new URL[pathComponentUrls.size()]), this.getClass().getClassLoader()));
				} else {
					if (extCP == null)
						codeClass = Class.forName(code);
					else
						codeClass = Class.forName(code, false, extClassLoader = new URLClassLoader(extCP.toArray(new URL[extCP.size()]),
								this.getClass().getClassLoader()));
				}
				workDir = lookupStringValue(RESERVE_NAME_DIR);
				if (workDir != null) 
					workDir = System.setProperty("user.dir", new File(workDir).getAbsolutePath());
				if (logger.isLoggable(FINE))
					logger.fine(code + ".main(" + args.toString() + ") in " + System.getProperty("user.dir"));
				if (useSM)
					Policy.getPolicy().refresh();
				lockSM = true;
				System.setSecurityManager(new SecurityManager() {
					public void checkExit(int status) {
						/*
						 * getNameSpace().inScope( new InfoHolder < String, InfoHolder, Object > (RESULT_CODE_VAR_NAME, new InfoHolder < String, String, Object >
						 * (RESULT_CODE_VAR_NAME, String.valueOf(status))));
						 */
						throw new SecurityException("Please do not leave.", new ProcessException(
								"Called Java class asked for exit.", status));
					}

					public void checkPackageAccess(String pkg) {
					}

					public void checkWrite(String file) {
					}

					public void checkRead(String file) {
					}

					public void checkDelete(String file) {
					}

					public void checkPermission(Permission perm) {
						if (!lockSM && perm.getName().equals("setSecurityManager")) {
							// TODO: make sure that it's called from right place
							return;
						}
						if (useSM)
							// try {
							super.checkPermission(perm);
						// } catch(SecurityException se_) {
						// logger.log(FINE, "Stack trace:", se_);
						// throw se_;
						// }
					}
				});
				if (extClassLoader != null) {
					origLoader = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(extClassLoader);
				}
				
				Object resultObject = getMethod(codeClass).invoke(null, new Object[] { args.toArray(new String[args.size()]) });
				if (resultObject instanceof Number)
					resultCode = ((Number)resultObject).intValue();
				else
					resultCode = 0;
			} catch (Error er) {
				// logger.severe("Error in call " + code + ".main(String...args) " + er);
				getNameSpace().inScope(
						new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_ERROR,
								new InfoHolder<String, String, Error>(RESERVE_NAME_ERROR, er.getMessage(), er)));
				if (logger.isLoggable(WARNING))
					logger.throwing(code, ".main(" + args + ") ", er);
				if (onExceptionHandler != null)
					onExceptionHandler.eval();
			} catch (NullPointerException npe) {
				// already reported ??
				logger.throwing(code, "Unexpected internal problem:", npe);
			} catch (ClassNotFoundException cnfe) {
				logger.severe("Can't find " + code + " " + cnfe);
			} catch (NoSuchMethodException nsme) {
				logger.severe("Can't find main() " + code + " " + nsme);
			} catch (IllegalAccessException iae) {
				logger.severe("Illegal access " + code + " " + iae);
			} catch (java.security.AccessControlException ace) {
				logger.severe("Access Control " + code + " " + ace);
				if (logger.isLoggable(FINE))
					logger.log(FINE, "Stack trace:", ace);
			} catch (java.lang.reflect.InvocationTargetException ite) {
				Throwable cause = ite.getCause();
				if (cause instanceof SecurityException) {
					Throwable securityCause = cause.getCause();
					if (securityCause instanceof ProcessException) {
						resultCode = ((ProcessException) securityCause).getExitCode();
					} else
						logger.log(SEVERE, "Invocation " + code + ".main(" + args + ")", cause);
				} else {
					getNameSpace().inScope(
							new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_ERROR,
									new InfoHolder<String, String, Throwable>(RESERVE_NAME_ERROR,
											ite.getCause() != null ? ite.getCause().toString() : ite.toString(), ite
													.getCause() != null ? ite.getCause() : ite)));
					logger.log(SEVERE, "Invocation " + code + ".main(" + args + ")", cause);
					if (onExceptionHandler != null)
						onExceptionHandler.eval();
				}
			} finally {
				lockSM = false;
				System.setSecurityManager(origSM);
				System.setProperties(origSystemProps);
				if (origLoader != null)
					Thread.currentThread().setContextClassLoader(origLoader);
				// if (workDir != null)
				// System.setProperty("user.dir", workDir);
			}
			// if (resultCode != 0)
			getNameSpace().inScope(
					new InfoHolder<String, InfoHolder, Object>(RESULT_CODE_VAR_NAME,
							result = new InfoHolder<String, String, Object>(RESULT_CODE_VAR_NAME, String
									.valueOf(resultCode))));
			if (onExitHandler != null)
				onExitHandler.eval();
		}
		return result;
	}

	public String getName() {
		String result = super.getName();
		return result == null || result.length() == 0 ? "main" : lookupStringValue(result);
	}

	protected Method getMethod(Class codeClass) throws NoSuchMethodException {
		Method result = null;
		try {
			result = codeClass.getMethod(getName(), new Class[] { String[].class });
		} catch (NoSuchMethodException nsme) {
			result = codeClass.getMethod("main", new Class[] { String[].class });
		} catch (Throwable t) {
			logger.severe("Other exception happened at obtaining method " + getName() + " at " + code + " " + t);
		}
		if (result != null && result.isAccessible() == false)
			result.setAccessible(true);
		return result;
	}

	protected void fillParameters(List<String> result, Map<String,String> environment) {
		for (Parameter parameter : parameters) {
			InfoHolder<String, String, Object> pv = parameter.eval();
			if (pv != null) {
				String parameterValue = pv.getValue();
				Object o = pv.getType();
				if (o != null) {
					if (o instanceof Object[]) {
						for (Object ov : (Object[]) o) {
							if (ov != null)
								result.add(ov.toString());
						}
						continue;
					} else if (o instanceof List) {
						for (Object s : (List<Object>) o) {
							if (s != null)
								result.add(s.toString());
						}
						continue;
					}
				}
				if (parameter.getName() != null) {
					if (parameterValue == null)
						parameterValue = "";
					if (type != Type.environment) {
						System.setProperty(parameter.getName(), parameterValue);
					} else if (environment != null)
						environment.put(parameter.getName(), parameterValue);
					logger.finest("Parameter '" + parameter.getName()
							+ "' was used to set a run System property or environment with " + parameterValue
							+ ", and hasn't been added in a parameter list.");
				} else if (parameterValue != null)
					result.add(parameterValue);
			} else
				logger.warning("Parameter " + parameter + " was evaluated as 'null' and ignored.");
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		exec = attributes.getValue("", ATTR_EXEC);
		code = attributes.getValue("", ATTR_CODE);
		path = attributes.getValue("", ATTR_PATH);
		tool = attributes.getValue("", ATTR_TOOL);
		stdErrVar = attributes.getValue("", ATTR_ERROUT);
		stdOutVar = attributes.getValue("", ATTR_STDOUT);
		stdInVar = attributes.getValue("", ATTR_STDIN);
	}

	public String[] getAllowedAttributeNames() {
		return Misc.merge(new String[] { ATTR_CODE, ATTR_EXEC, ATTR_TOOL, ATTR_PATH, ATTR_STDOUT, ATTR_ERROUT, ATTR_STDIN }, super
				.getAllowedAttributeNames());
	}
}