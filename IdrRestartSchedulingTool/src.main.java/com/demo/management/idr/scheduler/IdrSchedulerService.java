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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.demo.management.idr.model.Configuration;
import com.demo.management.idr.model.Subscription;
import com.demo.management.idr.util.Encryptor;

/**
 * Starts a Quartz Scheduler, get IdrScheduler configuration, 
 * register each subscription in the scheduler and waits for stop
 * signal
 * 
 * @author dlema
 */
public class IdrSchedulerService implements Runnable {
	
	// logger definition
	private static final Logger log = LogManager.getLogger(IdrSchedulerService.class.getName());
	
	// Defines the wait time to check for stop signal (in milliseconds)
	private static final int WAIT_INTERVAL = 60000;

	// Attributes
	private Configuration configuration;
	private Scheduler scheduler;
	private boolean stopped = false; // controls if the service has received a stop signal

	private String configFile = null;
	
	/**
	 * Instantiate and starts an IdrScheduler, using a Quartz scheduler
	 */
	public IdrSchedulerService() {
		log.traceEntry();
		
		try {
			SchedulerFactory sf = new StdSchedulerFactory();
		    scheduler = sf.getScheduler();
		    
		} catch (SchedulerException e) {
			log.fatal("Failure getting scheduler: ", e);
			System.exit(-1);
		}

		log.traceExit();
	}

	/**
	 * Refresh the setup of the scheduler by cleansing and reloading them.
	 */
	public void refresh() {
		log.traceEntry();
		
		try {
			scheduler.clear();
			
			schedule();
		} catch (Exception e) {
			log.trace("Unmanaged error: {}", e);
		}
		
		log.traceExit();
	}

	/**
	 * receive stop signal
	 */
	private void stop() {
		log.traceEntry();

		stopped = true;
		try {
			scheduler.shutdown();
		} catch (SchedulerException e) {
			log.error("error turning down scheduler");
		}
		
		synchronized (this) {
			this.notifyAll();
		}

		log.traceExit();
	}

	/**
	 * for each subscription in the configuration file, creates a job and a trigger
	 * and adds them to Quartz scheduler
	 * @throws SchedulerException 
	 */
	private void schedule() throws IOException, SchedulerException {
		log.traceEntry();
		
		// decriptor instance for decrypt passwords
		Encryptor encryptor = new Encryptor();
		
		// iterates on the subscription list
		Iterator<Subscription> iterator = configuration.getSubscriptions().iterator();
		while (iterator.hasNext()) {
			Subscription subscription = iterator.next();
			
			if (!subscription.isEnabled()) {
				log.info("Restart for subscription {} is inactive. It will be ignored", 
						subscription.getSubscriptionId());
				continue; 
			}
			
			log.info("Restart for subscription {} is active. It will be scheduled", 
					subscription.getSubscriptionId());
			
			// assembles a datamap with required data for restarts.
			JobDataMap dataMap = new JobDataMap();
			dataMap.put("accessServer", configuration.getAccessServer());
			dataMap.put("portNumber", configuration.getPort());
			dataMap.put("userId", configuration.getUserId());
			dataMap.put("password", encryptor.decrypt(configuration.getPassword()));
			dataMap.put("dataStore", subscription.getSourceDataStore());
			dataMap.put("subscription", subscription.getSubscriptionName());
			dataMap.put("encryptor", encryptor);
			if (log.isTraceEnabled()) {
				log.trace("access server: {}", configuration.getAccessServer());
				log.trace("port number: {}", configuration.getPort());
				log.trace("user id: {}", configuration.getUserId());
				log.trace("contraseña sin descifrar es {}", configuration.getPassword());
				log.trace("contraseña decifrada es {}" , encryptor.decrypt(configuration.getPassword()));
				log.trace("data store: {}", subscription.getSourceDataStore());
				log.trace("subscription: {}", subscription.getSubscriptionName());
			}
			
			try {
			    @SuppressWarnings("unchecked")
				Class<Job> clase = (Class<Job>) Class.forName(subscription.getLoaderClass());

			    JobDetail job = newJob(clase)
			    		.withIdentity(subscription.getSubscriptionId(), subscription.getSourceDataStore())
			    		.setJobData(dataMap)
			    		.build();
			    
			    CronTrigger trigger = newTrigger()
			    		.withIdentity(subscription.getSubscriptionId(), subscription.getSourceDataStore())
			    		.withSchedule(cronSchedule(subscription.getCronPattern()))
			    		.build();

			    Date ft = scheduler.scheduleJob(job, trigger);
				
			    log.info("{} has been scheduled to run at: {}  and repeat based on expression: {}",
			    		job.getKey(), ft, trigger.getCronExpression());
			} catch (ClassNotFoundException e) {
				log.error("the provided class {} was not found.  Scheduling for subscription {} will be skipped", 
						subscription.getLoaderClass(), subscription.getLoaderClass());
			}
		}
		
		log.traceExit();
	}

	/**
	 * thread execution
	 */
	@Override
	public void run() {
		log.traceEntry();
		log.info("==== Starts automatic IDR subscription restart services ====");
		
		if (configFile == null) {
			log.error("Configuration file was not established");
			return;
		}
		
		log.info("==== Configuration file: {} ====", configFile);
		Thread idrConfigWatcherThread = null;
		
		// creates IdrScheduler instance
		configuration = Configuration.unmarshal(new File(configFile));
		if (configuration == null) {
			log.error("No such configuration file.  Program stops.");
			return;
		}
		

		try {
			// creates a scheduler, loads configuration.
			schedule();
			
		    // starts the Quartz scheduler
		    scheduler.start();

		    // creates and starts thread for the watcher detecting configuration changes
			IdrConfigWatcher idrConfigWatcher = new IdrConfigWatcher(this);
			idrConfigWatcherThread = new Thread(idrConfigWatcher, "IdrConfigWatcher");
			idrConfigWatcherThread.start();
			
			// waits for stop signal
			while(!stopped) {
				log.trace("waits {} milliseconds before checking for stop signal", WAIT_INTERVAL);
				synchronized(this) {
					this.wait(WAIT_INTERVAL);
				}
			}
		} catch (SchedulerException e) {
			log.fatal("Error in scheduler operation: {}", e);
		} catch (InterruptedException e) {
			log.info("Stop signal received");
			stopped = true;
		} catch (IOException e) {
			log.fatal("Error processing configuration file: {}", e);
		} finally {
			try {
				log.info("Stopping scheduler...");
				this.scheduler.shutdown();
				log.info("Scheduler stopped");
				
				if (idrConfigWatcherThread != null) {
					log.info("Stopping configuration watcher");
					idrConfigWatcherThread.interrupt();
				}
			} catch (SchedulerException e) {
				log.fatal("error stopping scheduler: ", e);
			}
		}
	}
	
	/**
	 * Main program that starts and keep running Scheduler Service.
	 * It expects a parameter with the configuration file, which is an XML with the
	 * connection data and list of subscriptions to keep track.
	 * 
	 * See a sample configuration file at resources/configurationFile.xml
	 * 
	 * Usage: nohup java -D... -X... -jar IdrSchedulerService-0.0.1.jar configFile.xml &lt; /dev/null &gt; output.log 2&gt;&amp;1 &amp;
	 * @param args invocation parameters
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			log.error("No configuration file name was provided.");
			System.exit(-1);
		}
		String configurationFile = args[0];
		// get singleton for IdrSchedulerService class
		IdrSchedulerService idrSchedulerService = new IdrSchedulerService();
		idrSchedulerService.setConfigFile(configurationFile);
		idrSchedulerService.run();
		
		// stop quartz schedulers
		idrSchedulerService.stop();
		log.trace("sale de main");
	}

	/**
	 * @param configurationFile
	 */
	private void setConfigFile(String configurationFile) {
		this.configFile = configurationFile;
	}

	/**
	 * @return configuration file
	 */
	public String getConfigFile() {
		return configFile;
	}
}
