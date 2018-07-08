package com.internal.parker.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

//*****************************************************************************************************
//Class: PropertyService
//This function is used to import the values from the env.properties file.The imported values are 
//loaded in the variable properties.
//*****************************************************************************************************
@Service
public class PropertyService implements ApplicationContextAware {

	Properties properties = new Properties();

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		loadPropertyFile("env.properties");
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public void setProperty(String key, String value){
		properties.setProperty(key, value);
	}
//*****************************************************************************************************
//This function is used to import the values from the env.properties file.The imported values are 
//loaded in the variable properties.
//*****************************************************************************************************
	private void loadPropertyFile(String propertyFile) {
		InputStream is = null;
		is = PropertyService.class.getClassLoader().getResourceAsStream(propertyFile);
		Properties env = new Properties();
		try {
			env.load(is);
			properties.putAll(env);
		} catch (Exception e) {
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
