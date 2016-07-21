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

package org.entcore.maxicours.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserInfos;
import org.entcore.common.user.UserUtils;
import org.entcore.common.utils.Config;
import org.entcore.common.soap.SoapHelper;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpHeaders;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import fr.wseduc.rs.*;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;

public class MaxicoursController extends ControllerHelper{

	private final Logger log = LoggerFactory.getLogger(MaxicoursController.class);
	private final HttpClient soapClient;
	private final URL soapEndpoint;

	public MaxicoursController(HttpClient soapClient, URL endpoint){
		this.soapClient = soapClient;
		soapClient
			.setHost(endpoint.getHost())
			.setPort(endpoint.getPort() == -1 ? 80 : endpoint.getPort())
			.setMaxPoolSize(32)
			.setKeepAlive(false);
		soapEndpoint = endpoint;
	}

	@Get("/conf")
	@SecuredAction(type = ActionType.AUTHENTICATED, value = "")
	public void getConf(final HttpServerRequest request){
		JsonObject config = new JsonObject()
			.putString("connectorEndpoint", Config.getConf().getString("connectorEndpoint", ""));

		renderJson(request, config);
	}

	@Get("/getUserStatus")
	@SecuredAction(type = ActionType.AUTHENTICATED, value = "")
	public void getUserStatus(final HttpServerRequest request){
		final String soapAction = "getUserStatus";

		UserUtils.getUserInfos(eb, request, new Handler<UserInfos>() {
			@Override
			public void handle(UserInfos user) {
				if(user == null){
					badRequest(request);
					return;
				}

				SoapHelper.SoapDescriptor messageDescriptor = new SoapHelper.SoapDescriptor(soapAction);
				messageDescriptor
					.addNamespace("urn","urn:mxc-wsdl")
					.setBodyNamespace("", "urn")
					.createElement("entId", user.getUserId())
						.addAttribute("xsi:type", "string");
				List<String> uai = user.getUai();
				if(uai.size() > 0)
					messageDescriptor
						.createElement("etaRne", uai.get(0))
						.addAttribute("xsi:type", "string");

				processMessage(request, messageDescriptor);
			}
		});
	}

	@Get("/getUserInfo/:mxcId")
	@SecuredAction(type = ActionType.AUTHENTICATED, value = "")
	public void getUserInfos(final HttpServerRequest request){
		final String soapAction = "getUserInfo";
		final String mxcId = request.params().get("mxcId");

		if(mxcId == null || mxcId.trim().length() == 0){
			badRequest(request);
			return;
		}

		SoapHelper.SoapDescriptor messageDescriptor = new SoapHelper.SoapDescriptor(soapAction);
		messageDescriptor
			.addNamespace("urn","urn:mxc-wsdl")
			.setBodyNamespace("", "urn")
			.createElement("mxcId", mxcId)
				.addAttribute("xsi:type", "xsd:long");

		processMessage(request, messageDescriptor);
	}

	private void processMessage(final HttpServerRequest request, SoapHelper.SoapDescriptor messageDescriptor){
		String xml = "";
		try {
			xml = SoapHelper.createSoapMessage(messageDescriptor);
		} catch (SOAPException | IOException e) {
			log.error("["+MaxicoursController.class.getSimpleName()+"]("+messageDescriptor.getBodyTagName()+") Error while building the soap request.");
			renderError(request);
			return;
		}

		HttpClientRequest req = soapClient.post(soapEndpoint.getPath(), new Handler<HttpClientResponse>() {
			public void handle(final HttpClientResponse response) {
				response.bodyHandler(new Handler<Buffer>() {
					@Override
					public void handle(Buffer body) {
						request.response().end(body);
					}
				});
			}
		});
		req
			.putHeader("SOAPAction", messageDescriptor.getBodyTag())
			.putHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");
		req.end(xml);
	}

}
