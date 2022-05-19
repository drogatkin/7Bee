// $Id: Processor.java,v 1.14 2007/01/04 07:40:34 rogatkin Exp $
// Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 10, 2004
package org.bee.processor;

import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import static java.util.logging.Level.*;

import org.bee.util.Logger;
import static org.bee.util.Logger.*;
import org.bee.util.XmlPath;
import org.bee.util.InfoHolder;
import org.bee.util.Misc;

/**
 * @author Dmitriy
 * 
 */
public final class Processor {
	protected Configuration configuration;

	protected long startStamp;

	protected Map<XmlPath, Class> parseRegistry;

	public Processor(Configuration configuration) {
		this.configuration = configuration;
		if (configuration.exitCode != null)
			System.exit(configuration.exitCode);
		parseRegistry = new HashMap<XmlPath, Class>();
		for (Object o : configuration.descriptors) {
			InfoHolder<String, String, Integer> descriptor = (InfoHolder<String, String, Integer>) o;
			try {
				parseRegistry.put(XmlPath.fromString(descriptor.getKey()), Class.forName(descriptor.getValue()));
			} catch (Error er) {
				Logger.logger.log(SEVERE, "Problem in linkage of {0} {1}", new Object[] { descriptor.getValue(), er });
			} catch (Exception ex) {
				Logger.logger.log(SEVERE, "Problem in finding of {0} {1}", new Object[] { descriptor.getValue(), ex });
			}
		}
	}

	public void process() {
		if (configuration.beeFile == null) {
			Logger.logger.severe("No build file specified or it can't be found, use -h for help");
			return;
		}
		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setValidating(true);
			// TODO Config files from URL
			parserFactory.newSAXParser().parse(new File(configuration.beeFile), new ProcessHandler());
		} catch (Exception saxe) {
			Logger.logger.log(SEVERE, "Parsing exception: {0}", saxe.getMessage() != null ? saxe.getMessage() : saxe
					.toString());
			if (saxe instanceof SAXParseException) {
				Logger.logger.log(FINE, "** Parsing exception line {0}:col {1} id {2}", new Object[] {
						((SAXParseException) saxe).getLineNumber(), ((SAXParseException) saxe).getColumnNumber(),
						((SAXParseException) saxe).getSystemId() });
			} else if (saxe instanceof SAXException && ((SAXException) saxe).getException() != null)
				Logger.logger.log(FINER, "", ((SAXException) saxe).getException());
			else
				Logger.logger.log(FINEST, "Stack trace", saxe);
		} catch (ProcessException pe) {
			logger.info("Aborted in " + Misc.formatTime(System.currentTimeMillis() - startStamp));
			System.exit(pe.exitCode);
		}// no any other exception should happen
		logger.info("Processed in " + Misc.formatTime(System.currentTimeMillis() - startStamp));
		// System.out.println("--"+parseRegistry.get(XmlPath.fromString("*/variable")));
	}

	class ProcessHandler extends DefaultHandler {
		XmlPath path;

		Stack<Instruction> instrStack;

		public void startDocument() throws SAXException {
			startStamp = System.currentTimeMillis();
			path = new XmlPath();
			instrStack = new Stack<Instruction>();
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			path.push(qName);
			XmlPath workPath = path;
			Instruction instruction = null;
			Class instrClass;
			for (instrClass = parseRegistry.get(workPath); instrClass == null && workPath.empty() == false; workPath = XmlPath
					.wildCard(workPath))
				instrClass = parseRegistry.get(workPath);
			if (instrClass != null) {
				try {
					instruction = (Instruction) instrClass.getConstructor(String.class)
							.newInstance(workPath.toString());
				} catch (NoSuchMethodException nsme) {
					try {
						instruction = (Instruction) instrClass.getConstructor().newInstance();
					} catch (Exception ex2) {
						try {
							instruction = (Instruction) instrClass.getConstructor(String.class, Configuration.class).newInstance(workPath.toString(), configuration);
						} catch (Exception ex3) {		
							Logger.logger.log(SEVERE, "Can't create class for {0}", workPath);
							throw new ProcessException();
						}
					}
				} catch (Exception ex) {
					Logger.logger.log(SEVERE, "Exception at initialization of {0} {1}", new Object[] {
							instrClass.getName(), ex });
					if (ex instanceof InstantiationException)
						Logger.logger.log(SEVERE, "Causes of exception", ex.getCause());
					throw new ProcessException();
				}
				if (instruction != null) {
					instruction.setParent(instrStack.empty() ? null : instrStack.peek());
					instruction.getHandler().startElement(uri, localName, qName, attributes);
					instrStack.push(instruction);
				}
			} else {
				Logger.logger.log(SEVERE, "Unrecognizable tag: <{0}> in path {1}", new Object[] { qName, path });
				throw new ProcessException();
			}
		}

		public void endElement(String uri, String localName, String qName) throws SAXException {
			Instruction instruction = instrStack.empty() ? null : instrStack.pop();
			if (instruction != null) {
				instruction.getHandler().endElement(uri, localName, qName);

				if (instrStack.empty() == false && instrStack.peek() != null)
					instrStack.peek().childDone(instruction);
			}
			if (path.empty() == false)
				path.pop();
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			Instruction instruction;
			if (instrStack.empty() == false && (instruction = instrStack.peek()) != null)
				instruction.getHandler().characters(ch, start, length);
		}

		public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
			characters(ch, start, length);
		}

	}

}
