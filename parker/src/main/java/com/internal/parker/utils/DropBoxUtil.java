package com.internal.parker.utils;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuth;
import com.dropbox.core.DbxWebAuth.Request;
import com.dropbox.core.InvalidAccessTokenException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.internal.parker.core.PropertyService;
//*************************************************************************************************
//Class:DropBoxUtil
//The dependencies added in pom.xml are used here to use the inbuilt Jar files of Dropbox.
//All the functions of dropbox Jar are called here.Just we are giving the inputs to the Jar 
//which has all the predefine function of dropbox which can upload a file.The implementation of 
//dropbox is coded here.
//This will also create a slot.txt file in the location specified here(in dropbox account).The 
//overwrite function make sures that only the last line of output from command prompt is uploaded 
//and the new available data will overwrite the preious output.We can also use append function if 
//we want to append the data to the dropbox.
//*************************************************************************************************
@Service
public class DropBoxUtil {

	private PropertyService properties;
	private final String APP_TOKEN = "dropBox.token";
	private final String APP_KEY = "dropBox.key";
	private final String APP_SECRET = "dropBox.secret";
	private DbxAppInfo appInfo;
	private DbxRequestConfig config;
	private DbxWebAuth webAuth;
	private DbxClientV2 client;

	@Autowired
	public DropBoxUtil(PropertyService properties) {
		this.properties = properties;
	}

	@PostConstruct
	private void initialize() {
		appInfo = new DbxAppInfo(properties.getProperty(APP_KEY), properties.getProperty(APP_SECRET));
		config = new DbxRequestConfig("Parker");
		webAuth = new DbxWebAuth(config, appInfo);
		client = new DbxClientV2(config, getToken(), "");
	}

	private String getToken() {
		return properties.getProperty(APP_TOKEN);
	}

	private void authorize() {
		Request request = DbxWebAuth.newRequestBuilder().withNoRedirect().build();
		String url = webAuth.authorize(request);
		try {
			Desktop.getDesktop().browse(new URL(url).toURI());
			System.out.println("Copy the authorization code.");
			String code = new BufferedReader(new InputStreamReader(System.in)).readLine().trim();
			DbxAuthFinish authFinish;
			authFinish = webAuth.finishFromCode(code);
			String accessToken = authFinish.getAccessToken();
			properties.setProperty(APP_TOKEN, accessToken);
			client = new DbxClientV2(config, getToken(), "");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean upload(String data) throws IOException, DbxException {
		InputStream stream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		try {
			FileMetadata metadata = client.files().uploadBuilder("/parking/slots.txt").withMode(WriteMode.OVERWRITE)
					.withClientModified(new Date()).uploadAndFinish(stream);
		} catch (InvalidAccessTokenException e) {
			authorize();
			return upload(data);
		} finally {
			stream.close();
		}
		return true;
	}

}
