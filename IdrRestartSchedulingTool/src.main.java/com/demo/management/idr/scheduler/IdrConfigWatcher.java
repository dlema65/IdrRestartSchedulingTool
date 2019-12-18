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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Creates a Watcher to detect changes in the IDR Scheduler Configuration file
 * 
 * @author dlema
 */
public class IdrConfigWatcher implements Runnable {

	// Logger definition
	private static final Logger log = LogManager.getLogger(IdrConfigWatcher.class.getName());
	
	// Reference to the scheduler service
	private IdrSchedulerService idrSchedulerService = null;
	
	/**
	 * Watcher creator
	 * 
	 * @param idrSchedulerService scheduler service being watched 
	 */
	public IdrConfigWatcher(IdrSchedulerService idrSchedulerService) {

		log.traceEntry();
		this.idrSchedulerService = idrSchedulerService;
		log.traceExit();
	}

	public void run() {
		log.traceEntry("Starting configuration file watcher");
		
		final Path path = Paths.get(idrSchedulerService.getConfigFile()); 
		log.trace("configFile: {}", path);
		try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
		    path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
		    while (true) {
		        final WatchKey wk = watchService.take();
		        for (WatchEvent<?> event : wk.pollEvents()) {
		        	log.trace("event: {}", event.context().toString());
		            //we only register "ENTRY_MODIFY" so the context is always a Path.
	            	log.info("Change detected for file {}. Scheduler will be refreshed", path);
	            	idrSchedulerService.refresh();
		        }
		        // reset the key
		        boolean valid = wk.reset();
		        if (!valid) {
		            log.warn("Key has been unregistered");
		        }
		    }
		} catch (InterruptedException e) {
			log.error(e);
			Thread.currentThread().interrupt();
		} catch (IOException e) {
			log.error(e);
		}
		log.traceExit();
	}

}
