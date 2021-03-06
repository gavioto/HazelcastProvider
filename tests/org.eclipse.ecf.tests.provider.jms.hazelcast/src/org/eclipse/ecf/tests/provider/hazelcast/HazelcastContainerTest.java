package org.eclipse.ecf.tests.provider.hazelcast;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ecf.core.ContainerFactory;
import org.eclipse.ecf.core.IContainer;
import org.eclipse.ecf.core.identity.ID;
import org.eclipse.ecf.core.identity.IDFactory;
import org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase;

public class HazelcastContainerTest extends JMSContainerAbstractTestCase {

	@Override
	protected void setupBroker() throws Exception {
		// No broker
	}

	protected String getClientContainerName() {
		return Hazelcast.CLIENT_CONTAINER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#
	 * getServerContainerName()
	 */
	protected String getServerContainerName() {
		return Hazelcast.SERVER_CONTAINER_NAME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ecf.tests.provider.jms.JMSContainerAbstractTestCase#
	 * getServerIdentity()
	 */
	protected String getServerIdentity() {
		return Hazelcast.TARGET_NAME;
	}

	protected IContainer createServer() throws Exception {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("id", getServerIdentity());
		return ContainerFactory.getDefault().createContainer(getServerContainerName(),
				map);
	}

	public void testConnectClient() throws Exception {
		IContainer client = getClients()[0];
		ID targetID = IDFactory.getDefault().createID(client.getConnectNamespace(),
				getServerIdentity());
		Thread.sleep(3000);
		client.connect(targetID, null);
		Thread.sleep(3000);
	}

}
