/*
 * Copyright © Région Nord Pas de Calais-Picardie.
 *
 * This file is part of OPEN ENT NG. OPEN ENT NG is a versatile ENT Project based on the JVM and ENT Core Project.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation (version 3 of the License).
 *
 * For the sake of explanation, any module that communicate over native
 * Web protocols, such as HTTP, with OPEN ENT NG is outside the scope of this
 * license and could be license under its own terms. This is merely considered
 * normal use of OPEN ENT NG, and does not fall under the heading of "covered work".
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.entcore.maxicours;

import java.net.MalformedURLException;
import java.net.URL;

import io.vertx.core.Promise;
import org.entcore.common.http.BaseServer;
import org.entcore.maxicours.controllers.MaxicoursController;
import io.vertx.core.http.HttpClient;

public class Maxicours extends BaseServer {

	@Override
	public void start(Promise<Void> startPromise) throws Exception {
		super.start(startPromise);

		final String endpoint = config.getString("webserviceEndpoint", "");

		URL endpointURL;
		try {
			endpointURL = new URL(endpoint);
			addController(new MaxicoursController(endpointURL));
		} catch (MalformedURLException e) {
			log.error("Invalid Maxicours url.", e);
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}

}
