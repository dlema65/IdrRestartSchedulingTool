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
package com.demo.management.idr.model;

import java.io.File;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@XmlRootElement(name = "configuration")
@XmlType(propOrder = {"accessServer", "port", "userId", "password", "subscriptions"})
public class Configuration {
	
	// Definición de logger
	private static final Logger log = LogManager.getLogger(Configuration.class.getName());

	private StringProperty accessServer = new SimpleStringProperty();
	private StringProperty port = new SimpleStringProperty();
	private StringProperty userId = new SimpleStringProperty();
	private StringProperty password = new SimpleStringProperty();
	
	private ObservableList<Subscription> subscriptions = FXCollections.observableArrayList();
	
	@XmlElementWrapper(name = "subscriptions")
	@XmlElements({@XmlElement(name = "subscription", type = Subscription.class) } )
	public List<Subscription> getSubscriptions() {
		return subscriptions;
	}
	
	public ObservableList<Subscription> subscriptionsProperty() {
		return subscriptions;
	}
	
	@XmlElement(name = "accessServer")
	public String getAccessServer() {
		return accessServer.get();
	}
	
	public void setAccessServer(String accessServer) {
		this.accessServer.set(accessServer);
	}
	
	public StringProperty accessServerProperty() {
		return accessServer;
	}
	
	@XmlElement(name = "port") 
	public String getPort() {
		return port.get();
	}
	
	public void setPort(String port) {
		this.port.set(port);

	}
	
	public StringProperty portProperty() {
		return port;
	}
	
	@XmlElement(name = "userId")
	public String getUserId() {
		return userId.get();
	}
	
	public void setUserId(String userId) {
		this.userId.set(userId);
	}
	
	public StringProperty userIdProperty() {
		return userId;
	}
	
	@XmlElement(name = "password") 
	public String getPassword() {
		return password.get();
	}
	
	public void setPassword(String password) {
		this.password.set(password);
	}
	
	public StringProperty passwordProperty() {
		return password;
	}
	
	/**
	 * @return String representation for the configuration
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Access Server: " + this.getAccessServer());
		sb.append("\nPort number: " + this.getPort());
		sb.append("\nUser Id: " + this.getUserId());
		sb.append("\nPassword: " + this.getPassword());
		sb.append("\nSubscripciones:");
		
		for (Subscription node : subscriptions) {
			sb.append("\n\tSuscription:" + node.toString());
		}
		return sb.toString();
	}
	
	/**
	 * Saves the configuration
	 * 
	 * @param configuration the configuration to be saved
	 * @param file where the configuration will be saved
	 */
	
	public static void marshal(Configuration configuration, File file) {
		log.traceEntry();
		try {
			JAXBContext context = JAXBContext.newInstance(Configuration.class);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(configuration, file);
		} catch (JAXBException e) {
			log.fatal(e);
			System.exit(-1);
		}
		
		log.traceExit();
	}
	
	/**
	 * Loads IdrSchedulerService configuration file.  IdrSchedulerService configuration file 
	 * is an XML file describing the subscriptions to be monitored and restarted.
	 * 
	 * @param filename for the configuration file
	 * @return Configuration, null if the configuration file was not found, could not be read or
	 * is invalid
	 */
	public static Configuration unmarshal(File file) {
		try {
			log.traceEntry(file.toString());
			JAXBContext context = JAXBContext.newInstance(Configuration.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			
			Configuration configuration = (Configuration)unmarshaller.unmarshal(file);
			log.traceExit(configuration.toString());
			return configuration;
		} catch (UnmarshalException e) {
			log.error(e);
			log.warn("Configuration file {} was not read. It does not exists or is invalid.  "
					+ "returns null", file.getAbsolutePath());
		} catch (JAXBException e) {
			log.error(e);
			System.exit(-1);
		}
		return log.traceExit((Configuration)null);
	}
}
