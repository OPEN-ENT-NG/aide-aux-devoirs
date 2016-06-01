package org.entcore.maxicours;

import java.net.MalformedURLException;
import java.net.URL;

import org.entcore.common.http.BaseServer;
import org.entcore.maxicours.controllers.MaxicoursController;
import org.vertx.java.core.http.HttpClient;

public class Maxicours extends BaseServer {

	private HttpClient soapClient;

	@Override
	public void start() {
		super.start();

		final String endpoint = container.config().getString("webserviceEndpoint", "");
		soapClient = vertx.createHttpClient();

		URL endpointURL;
		try {
			endpointURL = new URL(endpoint);
			addController(new MaxicoursController(soapClient, endpointURL));
		} catch (MalformedURLException e) {
			log.error("Invalid Maxicours url.", e);
		}
	}

	@Override
	public void stop(){
		super.stop();
		soapClient.close();
	}

}
