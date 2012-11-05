// $Id: Bee.java,v 1.25 2008/04/21 04:32:23 dmitriy Exp $
// Bee Copyright (c) 2004 Dmitriy Rogatkin
/*
 * I dedicated this project to my children Anna and Phillip.
 */
package org.bee.processor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import static org.bee.util.Logger.logger;
import org.bee.util.InfoHolder;

// import static org.bee.processor.Instruction.*;

/**
 * @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
public class Bee extends AbstractBlock {
    // TODO: add global variables set from command line
    public static final String DEDICATION = "To my children Anna and Phillip";

    protected String homeDirectory;

    protected String lastTarget;

    protected List<Instruction> children;

    protected static Configuration<Bee> configuration;

    public static void main(String... args) {
        new Processor(configuration = (Configuration<Bee>) Configuration.readConfiguration(args)).process();
    }

    public Bee() {
        super("bee");
    }

    public InfoHolder eval() {
        // TODO: important consider to completely re-evaluate everything before processing next target
        InfoHolder result = null;
        if (configuration.targets.size() == 0)
            if (lastTarget != null)
                configuration.targets.add(lastTarget);
            else
                logger.info("No targets found.");
        logger.info("Targets: " + configuration.targets);
        // TODO: an option to go through all targets
        for (String target : configuration.targets) {
            InfoHolder<String, InfoHolder, Object> th = getNameSpace().lookup(target);
            if (th != null) {
                if (children != null)
                    for (Instruction i : children)
                        i.eval();
                result = ((Target) th.getValue().getValue()).eval();
            } else
                logger.severe("No target '" + target + "' found.");
        }
        return result;
    }

    public void childDone(Instruction child) {
        if (child instanceof Target) {
            lastTarget = child.getName();
            getNameSpace().inScope(
                    new InfoHolder<String, InfoHolder, Object>(child.getName(),
                            new InfoHolder<String, Target, Object>(child.getName(), (Target) child)));
            if (printTargetHelp())
                System.out.printf("  %s - %s%n", lastTarget, ((Target) child).getComment() == null ? ""
                        : ((Target) child).getComment());
        } else {
            child.eval();
            if (children != null)
                children.add(child);
                // TODO can it be homeDirectory var name so revalidate it in RESERVE_NAME_DIR
            // else TODO why it can be?
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        homeDirectory = attributes.getValue("", ATTR_DIR);
        if (configuration.isReevaluate())
            children = new ArrayList<Instruction>(32);
        List<String> args = configuration.getArguments(this);
        if (args != null)
            getNameSpace().inScope(
                    new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_ARGS,
                            new InfoHolder<String, String, List<String>>(RESERVE_NAME_ARGS, args.toString(),
                                    args)));
        // logger.info("Run args:"+args);
        System.getProperties().putAll(configuration.getDefines(this));
        Properties properties = configuration.getExtraProperties(this);
        if (properties != null)
            System.getProperties().putAll(properties);
        // logger.config("Run props:"+configuration.getDefines(this));
        // TODO consider homeDirectory AS NAME OF VARIABLE ESPECIALLY IF CAN"T BE resolved as a directory
        // with late definition 
        if (homeDirectory != null)
            getNameSpace().inScope(
                    new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_DIR,
                            new InfoHolder<String, String, Object>(RESERVE_NAME_DIR, homeDirectory)));
        if (configuration.isNoInput())
            getNameSpace().inScope(
                    new InfoHolder<String, InfoHolder, Object>(RESERVE_OPTION_NOINPUT, new InfoHolder(
                            RESERVE_OPTION_NOINPUT, RESERVE_OPTION_NOINPUT)));
        getNameSpace().inScope(
                new InfoHolder<String, InfoHolder, Object>(RESERVE_BUILD_FILE, new InfoHolder(
                        RESERVE_BUILD_FILE, configuration.beeFile)));
        List<URL> extCP = configuration.getExtendClassPath();
        if (extCP != null) {
            StringBuffer classPath = new StringBuffer();
            for (URL url : extCP)
                classPath.append(url.getFile().toString().substring(1)).append(File.pathSeparatorChar);
            getNameSpace().inScope(
                    new InfoHolder<String, InfoHolder, Object>(RESERVE_CLASS_LIB,
                            new InfoHolder<String, String, List<URL>>(RESERVE_CLASS_LIB,
                                    classPath.toString(), extCP)));
        }
        type = Type.project;
        super.startElement(uri, localName, qName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (printTargetHelp() == false) {
            if (homeDirectory != null) {
                homeDirectory = this.lookupStringValue(homeDirectory);
                getNameSpace().inScope(
                        new InfoHolder<String, InfoHolder, Object>(RESERVE_NAME_DIR,
                                new InfoHolder<String, String, Object>(RESERVE_NAME_DIR, homeDirectory)));
            }
            eval();
        }
    }

    private boolean printTargetHelp() {
        return configuration.isTargetHelp();
    }
}