/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.help.internal.server;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.server.HelpServer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public class JettyHelpServer extends HelpServer {
	
	private String host;
	private int port = -1;
	private static final int AUTO_SELECT_JETTY_PORT = 0;
	
	public void start(final String webappName) throws Exception {
		final Dictionary d = new Hashtable();
		
		configurePort();
		d.put("http.port", new Integer(getPortParameter())); //$NON-NLS-1$

		// set the base URL
		d.put("context.path", "/help"); //$NON-NLS-1$ //$NON-NLS-2$
		d.put("other.info", "org.eclipse.help"); //$NON-NLS-1$ //$NON-NLS-2$
		
		// suppress Jetty INFO/DEBUG messages to stderr
		Logger.getLogger("org.mortbay").setLevel(Level.WARNING); //$NON-NLS-1$	

		Job startJob = new Job("Start Help Server") { //$NON-NLS-1$
			protected IStatus run(IProgressMonitor monitor) {
				try {
					JettyConfigurator.startServer(webappName, d);
				} catch (Throwable t) {
					return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, "", t); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}		
		};
		execute(startJob);		
		checkBundle();	
	}

	/*
	 * Ensures that the bundle with the specified name and the highest available
	 * version is started and reads the port number
	 */
	private void checkBundle() throws InvalidSyntaxException, BundleException {
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.http.registry"); //$NON-NLS-1$if (bundle != null) {
		if (bundle.getState() == Bundle.RESOLVED) {
			bundle.start(Bundle.START_TRANSIENT);
		}
		if (port == -1) {
			// Jetty selected a port number for us
			ServiceReference[] reference = bundle.getBundleContext().getServiceReferences("org.osgi.service.http.HttpService", "(other.info=org.eclipse.help)"); //$NON-NLS-1$ //$NON-NLS-2$
			Object assignedPort = reference[0].getProperty("http.port"); //$NON-NLS-1$
			port = Integer.parseInt((String)assignedPort);
		}
	}

	public void stop(final String webappName) throws CoreException {
		try {
			Job stopJob = new Job("") { //$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					try {
						JettyConfigurator.stopServer(webappName);
					} catch (Throwable t) {
						return new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, "", t); //$NON-NLS-1$
					}
					return Status.OK_STATUS;
				}		
			};
			execute(stopJob);
		}
		catch (Exception e) {
			HelpBasePlugin.logError("An error occured while stopping the help server", e); //$NON-NLS-1$
		}
	}
	
	private void execute(Job job) throws Exception {
		boolean interrupted = false;
		job.schedule();
		while(true) {
			try {
				job.join();
				break;
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
		if (interrupted)
			Thread.currentThread().interrupt();
		
		IStatus result = job.getResult();
		if (result != null && !result.isOK() && result.getException() != null) {
			Throwable t = result.getException();
			if (t instanceof Exception)
				throw (Exception)t;
			else
				throw (Error) t;
		}
	}

	public int getPort() {
		return port;
	}

	private void configurePort() {
		if (port == -1) {
			String portCommandLineOverride = HelpBasePlugin.getBundleContext().getProperty("server_port"); //$NON-NLS-1$
			if (portCommandLineOverride != null && portCommandLineOverride.trim().length() > 0) {
				try {
					port = Integer.parseInt(portCommandLineOverride);
				}
				catch (NumberFormatException e) {
					String msg = "Help server port specified in VM arguments is invalid (" + portCommandLineOverride + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					HelpBasePlugin.logError(msg, e);
				}
			}
		}
	}
	
	/*
	 * Get the port number which will be passed to Jetty
	 */
	private int getPortParameter() {
		if (port == -1) { 
			return AUTO_SELECT_JETTY_PORT;
		}
		return port;
	}

	public String getHost() {
		if (host == null) {
			String hostCommandLineOverride = HelpBasePlugin.getBundleContext().getProperty("server_host"); //$NON-NLS-1$
			if (hostCommandLineOverride != null && hostCommandLineOverride.trim().length() > 0) {
				host = hostCommandLineOverride;
			}
			else {
				host = "127.0.0.1"; //$NON-NLS-1$
			}
		}
		return host;
	}
	

}
