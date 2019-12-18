/**
 * Licensed Materials - Property of IBM 
 * 
 * (c) Copyright IBM Corp. 2019 All rights reserved.
 * 
 * The following sample of source code ("Sample") is owned by International 
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is 
 * copyrighted and licensed, not sold. You may use, copy, modify, and 
 * distribute the Sample in any form without payment to IBM.
 * 
 * The Sample code is provided to you on an "AS IS" basis, without warranty of 
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR 
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do 
 * not allow for the exclusion or limitation of implied warranties, so the above 
 * limitations or exclusions may not apply to you. IBM shall not be liable for 
 * any damages you suffer as a result of using, copying, modifying or 
 * distributing the Sample, even if IBM has been advised of the possibility of 
 * such damages.
 */
package com.demo.management.idr.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.datamirror.ea.api.ApiException;
import com.datamirror.ea.api.DataSource;
import com.datamirror.ea.api.DefaultContext;
import com.datamirror.ea.api.ReplicationRole;
import com.datamirror.ea.api.Toolkit;
import com.datamirror.ea.api.publisher.Publisher;
import com.datamirror.ea.api.publisher.Subscription;


/**
 * Connects to the source datastore, prints out the refresh statistics for the
 * subscription.
 */
public class AccessServerUtil {

	Logger logger = LogManager.getLogger(AccessServerUtil.class.getName());

	private DataSource accessServer;

	private String server;
	private String port;
	private String user;
	private String password;

	public AccessServerUtil(
			String server, String port, String user, 
			String password) 
	{
		logger.traceEntry("AccessServerUtil - Parameters server = {}, port = {}, user = {}, password = ****", server, port, user);
		
		this.server = server;
		this.port = port;
		this.user = user;
		this.password = password;
		
		logger.traceExit();
	}

	/**
	 * Connects to a replication agent.
	 * 
	 * @param datastoreName
	 *            the name of the datastore.
	 * @param source
	 *            true to request source statistics, false to request target
	 *            statistics.
	 * @throws ApiException
	 *             if an error occurred.
	 */
	public ReplicationRole connectDatastore(String datastoreName, boolean source) throws ApiException {
		logger.traceEntry("connecting to datastore {}. (is source? {})", datastoreName, source);
		if (accessServer == null || !accessServer.isOpen()) {
			logger.error("connection to access server {} at port {} with user {} has not been estabished", 
					this.server, this.port, this.user);
			throw new ApiException("Connection to Access Server is not established");
		}

		ReplicationRole datastore;

		if (source) {
			logger.trace("datastore {} is expected to be source", datastoreName);
			datastore = accessServer.getPublisher(datastoreName);
		} else {
			logger.trace("datastore {} is expected to be target", datastoreName);
			datastore = accessServer.getSubscriber(datastoreName);
		}

		if (datastore == null) {
			throw logger.throwing(new ApiException("Failed to locate a datastore: " + datastoreName));
		}

		if (!datastore.isConnected()) {
			try {
				logger.info("Connecting to {}...", datastoreName);
				datastore.connect();
				logger.info("Connected.");
			} catch (ApiException e) {
				throw logger.throwing(new ApiException("Failed to connect to datastore " + 
						datastoreName + ". " + e.getMessage()));
			}
		}

		return logger.traceExit(datastore);
	}

	/**
	 * starts a specific subscription
	 * @param sourceDatastore publisher or source datastore holding the subscription
	 * @param subscriptionName name for the subscription
	 * @throws ApiException
	 */
	public void startMirroring(String sourceDatastore, String subscriptionName) throws ApiException {
		logger.traceEntry("start mirroring for datastore {}, subscription {}", sourceDatastore, subscriptionName);
		try {

			connectAccessServer();

			if (sourceDatastore != null && sourceDatastore.length() > 0) {
				ReplicationRole datastore = null;

				datastore = connectDatastore(sourceDatastore, true);

				if (logger.isTraceEnabled()) {
					String[] availableSuscriptions = ((Publisher) datastore).getSubscriptionNames();

					logger.trace("list of available subscriptions on datastore {}", datastore);
					for (String suscription : availableSuscriptions) {
						logger.trace(suscription);
					}
				}

				Subscription subscription = ((Publisher) datastore).getSubscription(subscriptionName);

				if (subscription != null) {
					
					byte status = subscription.getLiveActivityStatus()[1];
					String statusDesc = describeSubscriptionStatus(status);
					
					logger.info("Subscription status: {}", statusDesc);
					
					if(status != Subscription.LIVE_STATUS_ACTIVE){
						
						logger.warn("Subscription {} found not running. It is in status {}", 
								subscription,
								statusDesc);							
						logger.info("Starting subscription {}.", subscription);
						subscription.startMirror(true);
						subscription.refresh();
					}
				} else {
					logger.warn("Subscription {} does not exists in source datastore {}", 
							subscription, sourceDatastore);
				}
			} else {
				logger.warn("Received Source Data Store is null or blank.  Cannot be started");
			}
		} catch (ApiException e) {
			logger.error("Error suscripción", e);
		} finally {
			disconnectAccessServer();
		}
		logger.traceExit();
	}

	/**
	 * Disconnects from the datastore.
	 * 
	 * @param datastore
	 *            the datastore.
	 * @throws ApiException
	 *             If an error occurred
	 */
	public void disconnectDatastore(ReplicationRole datastore) throws ApiException {
		logger.traceEntry(datastore.getName());
		
		boolean disconnecting = false;

		if (datastore.isConnected()) {
			logger.info("Disconnecting from {}...", datastore.getName());
			disconnecting = true;
			datastore.disconnect();
		}

		if (datastore.isConnected()) {
			throw logger.throwing(new ApiException("Failed to disconnect from a dataStore " 
					+ datastore.getName()));
		} else if (disconnecting) {
			logger.info("Disconnected.");
		}
		logger.traceExit();
	}

	/**
	 * Connects to access server.
	 *
	 * @throws ApiException
	 *             if an error occurred.
	 */
	public void connectAccessServer() throws ApiException {
		logger.traceEntry("connecting to Access Server...");
		try {

			accessServer = Toolkit.getDefaultToolkit().createDataSource();

			DefaultContext eaAccessContext = new DefaultContext();
			eaAccessContext.setString(DataSource.User, user);
			eaAccessContext.setString(DataSource.Password, password);
			eaAccessContext.setString(DataSource.Hostname, server);
			eaAccessContext.setInt(DataSource.Port, Integer.parseInt(port));
			accessServer.connect(eaAccessContext);

			logger.trace("Connected.");
		} catch (ApiException e) {
			throw logger.throwing(new ApiException("Failed to connect to " + server + 
					"@" + port + " as " + user));
		}
		logger.traceExit("connected...");
	}

	/**
	 * Disconnects from access server.
	 * 
	 * @throws ApiException
	 *             if an error occurred.
	 */
	public void disconnectAccessServer() throws ApiException {
		logger.traceEntry();
		
		boolean disconnecting = false;

		if (accessServer != null && accessServer.isOpen()) {
			logger.trace("Disconnecting from Access Server...");
			disconnecting = true;
			accessServer.close();
		}

		if (accessServer != null && accessServer.isOpen()) {
			logger.error("Failed to disconnect from AccessServer");
			throw new ApiException("Failed to disconnect from AccessServer");
		} else if (disconnecting) {
			logger.trace("Disconnected.");
		}
		logger.traceExit();
	}

	/**
	 * Returns an example of arguments for the class.
	 * 
	 * @return the help string.
	 */
	public static String getHelp() {
		return "java -classpath \"{pathToSample and api.jar}\" " + "MonitorPerformanceRefreshSample\n" //$NON-NLS-2$
				+ "  [-host accessServerHost -port accessServerPort]\n" + "   -user accessServerUser -password accessServerPassword\n" + "   -source sourceDataStoreName -subscriptionName subscriptionName\n" + "If -host or -port are not specified, \"localhost\" and \"10101\" " + " will be used.\n" //$NON-NLS-2$
				+ "-source and -subscriptionName are required.";
	}

	/**
	 * Maps status codes into readable texts.
	 * @param status status code.
	 * @return status description
	 */
	public String describeSubscriptionStatus(byte status) {
		logger.traceEntry(Byte.toString(status));

		String describedStatus = "";

		switch (status) {

		case Subscription.LIVE_STATUS_ACTIVE:
			describedStatus = "LIVE_STATUS_ACTIVE";
			break;
		case Subscription.LIVE_STATUS_BLOCKED:
			describedStatus = "LIVE_STATUS_BLOCKED";
			break;
		case Subscription.LIVE_STATUS_IDLE:
			describedStatus = "LIVE_STATUS_IDLE";
			break;
		case Subscription.LIVE_STATUS_RECOVERY:
			describedStatus = "LIVE_STATUS_RECOVERY";
			break;
		case Subscription.LIVE_STATUS_START:
			describedStatus = "LIVE_STATUS_START";
			break;
		case Subscription.LIVE_STATUS_WAIT:
			describedStatus = "LIVE_STATUS_WAIT";
			break;
		case Subscription.LIVE_STATUS_DS_STARTING_JOB:
			describedStatus = "LIVE_STATUS_DS_STARTING_JOB";
			break;
		case Subscription.LIVE_STATUS_DS_WAITING_FOR_JOB_TO_START:
			describedStatus = "LIVE_STATUS_DS_WAITING_FOR_JOB_TO_START";
			break;
		case Subscription.LIVE_STATUS_DS_CONNECTING_WITH_TARGET:
			describedStatus = "LIVE_STATUS_DS_CONNECTING_WITH_TARGET";
			break;
		case Subscription.LIVE_STATUS_DS_JOB_ENDING:
			describedStatus = "LIVE_STATUS_DS_JOB_ENDING";
			break;
		default:
			describedStatus = "NOT_RECOGNIZED AS A VALID STATE"; 
			break;
		}
		
		return logger.traceExit(describedStatus);
	}
	
	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}