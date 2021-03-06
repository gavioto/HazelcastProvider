/****************************************************************************
 * Copyright (c) 2015 Composent, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Composent, Inc. - initial API and implementation
 *****************************************************************************/
package org.eclipse.ecf.provider.jms.hazelcast;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.ecf.core.ContainerCreateException;
import org.eclipse.ecf.core.ContainerTypeDescription;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.provider.internal.jms.hazelcast.Activator;
import org.eclipse.ecf.provider.jms.identity.JMSID;
import org.eclipse.ecf.provider.jms.identity.JMSNamespace;
import org.eclipse.ecf.remoteservice.provider.PeerRemoteServiceContainerInstantiator;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryXmlConfig;
import com.hazelcast.config.UrlXmlConfig;
import com.hazelcast.config.XmlConfigBuilder;

public abstract class AbstractHazelcastContainerInstantiator extends PeerRemoteServiceContainerInstantiator {

	public static final String DEFAULT_SERVER_ID = "hazelcast://localhost/exampleTopic";

	public static final String ID_PARAM = "id";
	public static final String KEEPALIVE_PARAM = "keepAlive";
	public static final String CONFIG_PARAM = "config";
	public static final String CONFIGURL_PARAM = "configURL";

	protected static final String[] hazelcastIntents = { "hazelcast" };

	public AbstractHazelcastContainerInstantiator() {
		super(Activator.HAZELCAST_MANAGER_NAME, Activator.HAZELCAST_MEMBER_NAME);
	}

	protected JMSID getJMSIDFromParameter(Map<String, ?> parameters, String key, String def) {
		Object p = getParameterValue(parameters, key, Object.class, def);
		if (p instanceof String) {
			return (JMSID) IDFactory.getDefault().createID(JMSNamespace.NAME, (String) p);
		} else if (p instanceof JMSID) {
			return (JMSID) p;
		} else if (def != null)
			return (JMSID) IDFactory.getDefault().createID(JMSNamespace.NAME, def);
		else
			return null;
	}

	public String[] getSupportedIntents(ContainerTypeDescription description) {
		List<String> results = new ArrayList<String>(Arrays.asList(super.getSupportedIntents(description)));
		results.addAll(Arrays.asList(hazelcastIntents));
		return (String[]) results.toArray(new String[results.size()]);
	}

	protected Config getConfigFromArg(Map<String, ?> parameters) throws Exception {
		Object o = getParameterValue(parameters, CONFIG_PARAM, Object.class, null);
		if (o instanceof Config)
			return (Config) o;
		else if (o instanceof InputStream)
			return new XmlConfigBuilder((InputStream) o).build();
		else if (o instanceof URL)
			return new UrlXmlConfig((URL) o);
		else if (o instanceof String)
			return new InMemoryXmlConfig((String) o);
		return null;
	}

	protected Config getURLConfigFromArg(Map<String, ?> parameters) throws Exception {
		Object o = getParameterValue(parameters, CONFIGURL_PARAM, Object.class, null);
		if (o instanceof URL)
			return new UrlXmlConfig((URL) o);
		else if (o instanceof String)
			return new UrlXmlConfig(new URL((String) o));
		return null;
	}

	public IContainer createInstance(ContainerTypeDescription description, Map<String, ?> parameters)
			throws ContainerCreateException {
		try {
			JMSID id = getJMSIDFromParameter(parameters, ID_PARAM,
					(description.getName().equals(Activator.HAZELCAST_MANAGER_NAME)) ? DEFAULT_SERVER_ID
							: UUID.randomUUID().toString());
			Integer keepAlive = getParameterValue(parameters, KEEPALIVE_PARAM, Integer.class,
					new Integer(HazelcastManagerContainer.DEFAULT_KEEPALIVE));
			Config config = getConfigFromArg(parameters);
			if (config == null)
				config = getURLConfigFromArg(parameters);
			return createHazelcastContainer(id, keepAlive, parameters, config);
		} catch (Exception e) {
			return throwCreateException("Could not create hazelcast container with name " + description.getName(), e);
		}
	}

	protected abstract IContainer createHazelcastContainer(JMSID id, Integer ka, Map<String, ?> parameters,
			Config config) throws Exception;
}
