package com.demo.management.idr.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

//@XmlType(name = "subscription")
@XmlType(name = "subscription", propOrder={"subscriptionName","sourceDataStore","cronPattern","loaderClass","enabled"})
public class Subscription {
	
	private StringProperty subscriptionId = new SimpleStringProperty();
	private StringProperty subscriptionName = new SimpleStringProperty();
	private StringProperty sourceDataStore = new SimpleStringProperty();
	private StringProperty cronPattern = new SimpleStringProperty();
	private StringProperty loaderClass = new SimpleStringProperty();
	private BooleanProperty enabled = new SimpleBooleanProperty();
	
	public Subscription() {}
	
	public Subscription(
			String subscriptionId, 
			String subscriptionName,
			String sourceDataStore,
			String cronPattern,
			String loaderClass,
			boolean enabled) {
		this.subscriptionId.set(subscriptionId);
		this.subscriptionName.set(subscriptionName);
		this.sourceDataStore.set(sourceDataStore);
		this.cronPattern.set(cronPattern);
		this.loaderClass.set(loaderClass);
		this.enabled.set(enabled);
	}
		
	@XmlAttribute(name = "subscriptionId")
	public String getSubscriptionId() {
		return subscriptionId.get();
	}
	
	public void setSubscriptionId(String value) {
		this.subscriptionId.set(value);
	}
	
	public StringProperty subscriptionIdProperty() {
		return subscriptionId;
	}
	
	@XmlElement(name = "subscriptionName")
	public String getSubscriptionName() {
		return subscriptionName.get();
	}
	
	public void setSubscriptionName(String value) {
		this.subscriptionName.set(value);
	}
	
	public StringProperty subscriptionNameProperty() {
		return subscriptionName;
	}
	
	@XmlElement(name = "sourceDataStore")
	public String getSourceDataStore() {
		return sourceDataStore.get();
	}
	
	public void setSourceDataStore(String sourceDataStore) {
		this.sourceDataStore.set(sourceDataStore);
	}
	
	public StringProperty sourceDataStoreProperty() {
		return sourceDataStore;
	}
	
	@XmlElement(name = "cronPattern")
	public String getCronPattern() {
		return cronPattern.get();
	}
	
	public void setCronPattern(String cronPattern) {
		this.cronPattern.set(cronPattern);
	}
	
	public StringProperty cronPatternProperty() {
		return cronPattern;
	}
	
	@XmlElement(name = "loaderClass")
	public String getLoaderClass() {
		return loaderClass.get();
	}
	
	public void setLoaderClass(String loaderClass) {
		this.loaderClass.set(loaderClass);
	}
	
	public StringProperty loaderClassProperty() {
		return loaderClass;
	}
	
	@XmlElement (name = "enabled") 
	public boolean isEnabled() {
		return enabled.get();
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled.set(enabled);
	}
	
	public BooleanProperty enabledProperty() {
		return enabled;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\tSubscription Id: " + getSubscriptionId());
		sb.append("\n\tSubscription name: " + getSubscriptionName());
		sb.append("\n\tSource data store: " + getSourceDataStore());
		sb.append("\n\tCron Pattern: " + getCronPattern());
		sb.append("\n\tloader class: " + getLoaderClass());
		sb.append("\n\tEnabled: " + isEnabled());
		return sb.toString(); 
	}

}
