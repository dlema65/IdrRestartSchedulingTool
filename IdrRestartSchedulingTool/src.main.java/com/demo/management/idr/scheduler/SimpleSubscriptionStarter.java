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
package com.demo.management.idr.scheduler;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.demo.management.idr.util.AccessServerUtil;

/**
 * Class implementing a basic procedure for restarting subscriptions
 * 
 * @author dlema
 */
public class SimpleSubscriptionStarter implements Job {

	private Logger logger = LogManager.getLogger(SimpleSubscriptionStarter.class.getName());

	/**
	 * Wraps the subscription starter as a job. Subscription starter is implemented in AccessServerUtil
	 * 
	 * @param context execution context
	 */
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.traceEntry(context.toString());

		try {
			Map<String, Object> dataMap = context.getMergedJobDataMap();
			
			String accessServer = (String)dataMap.get("accessServer");
			String portNumber = (String)dataMap.get("portNumber");
			String userId =	(String)dataMap.get("userId");
			String password = (String)dataMap.get("password");
			String dataStore = (String)dataMap.get("dataStore");
			String subscription = (String)dataMap.get("subscription");
			
			AccessServerUtil accessServerUtil = new AccessServerUtil(
					accessServer, portNumber, userId, password);

			if (dataStore != null && dataStore.length() > 0) {
				logger.info("Checking subscription status... DataStore: {}, Subscription: {}", 
						dataStore, subscription);
				accessServerUtil.startMirroring(dataStore, subscription);
				logger.info("Subscription status checked.");
			} else {
				logger.error("the datastore is null or blank");
			}

		} catch (Exception e) {
			logger.error("Error executing task.", e);
		}
		
		logger.traceExit();
	}
}
