/*
 * Copyright (c) 2004 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */

package org.epics.ca.client.impl.remote.tcp;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Set;

import org.epics.ca.impl.remote.Context;
import org.epics.ca.impl.remote.IntrospectionRegistry;
import org.epics.ca.impl.remote.ReferenceCountingTransport;
import org.epics.ca.impl.remote.ResponseHandler;
import org.epics.ca.impl.remote.TransportClient;
import org.epics.ca.impl.remote.TransportSendControl;
import org.epics.ca.impl.remote.TransportSender;
import org.epics.ca.impl.remote.tcp.BlockingTCPTransport;
import org.epics.pvData.misc.TimerFactory;
import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;

/**
 * Client TCP transport implementation.
 * @author <a href="mailto:matej.sekoranjaATcosylab.com">Matej Sekoranja</a>
 * @version $Id$
 */
public class BlockingClientTCPTransport extends BlockingTCPTransport implements ReferenceCountingTransport, TimerCallback, TransportSender {

	/**
	 * Owners (users) of the transport.
	 */
	private Set<TransportClient> owners;
	
	/**
	 * Connection timeout (no-traffic) flag.
	 */
	private long connectionTimeout;

	/**
	 * Unresponsive transport flag.
	 */
	private volatile boolean unresponsiveTransport = false;

	/**
	 * Timer task node.
	 */
	private TimerNode timerNode;

	/**
	 * Timestamp of last "live" event on this transport.
	 */
	private volatile long aliveTimestamp;
	
	/**
	 * Introspection registry.
	 */
	protected IntrospectionRegistry introspectionRegistry;
	
	/**
	 * Client TCP transport constructor.
	 * @param context context where transport lives in.
	 * @param channel used socker channel.
	 * @param responseHandler response handler used to process CA headers.
	 * @param receiveBufferSize receive buffer size.
	 * @param client transport client (owner, requestor).
	 * @param remoteTransportRevision remote transport revision.
	 * @param beaconInterval beacon interval in seconds.
	 * @param priority transport priority.
	 */
	public BlockingClientTCPTransport(Context context, SocketChannel channel,
					ResponseHandler responseHandler, int receiveBufferSize, 
					TransportClient client, short remoteTransportRevision,
					float beaconInterval, short priority) {
		super(context, channel, responseHandler, receiveBufferSize, priority);
		
		// create introspection registry
		introspectionRegistry = new IntrospectionRegistry(false);

		// initialize owners list, send queue
		owners = new HashSet<TransportClient>();
		acquire(client);
		
		// use immediate for clients
		setSendQueueFlushStrategy(SendQueueFlushStrategy.IMMEDIATE);
		
		// setup connection timeout timer (watchdog)
		connectionTimeout = (long)(beaconInterval * 1000);
		aliveTimestamp = System.currentTimeMillis();
		timerNode = TimerFactory.createNode(this);
		context.getTimer().schedulePeriodic(timerNode, beaconInterval, beaconInterval);
		
		start();
	}
	
	/**
	 * @see org.epics.ca.impl.remote.tcp.TCPTransport#internalClose(boolean)
	 */
	@Override
	protected void internalClose(boolean forced) {
		super.internalClose(forced);

		timerNode.cancel();

		closedNotifyClients();
	}

	final Object sendBufferFreed = new Object();

	/**
	 * @see org.epics.ca.impl.remote.Transport#getIntrospectionRegistry()
	 */
	public IntrospectionRegistry getIntrospectionRegistry() {
		return introspectionRegistry;
	}

	/**
	 * Notifies clients about disconnect.
	 */
	private void closedNotifyClients() {
		synchronized (owners)
		{
			// check if still acquired
			int refs = owners.size();
			if (refs > 0)
			{ 
				context.getLogger().fine("Transport to " + socketAddress + " still has " + refs + " client(s) active and closing...");
				TransportClient[] clients = new TransportClient[refs];
				owners.toArray(clients);
				for (int i = 0; i < clients.length; i++)
				{
					try
					{
						clients[i].transportClosed();
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}
			
			owners.clear();
		}
	}

	/** 
	 * Acquires transport.
	 * @param client client (channel) acquiring the transport
	 * @return <code>true</code> if transport was granted, <code>false</code> otherwise.
	 */
	public synchronized boolean acquire(TransportClient client) {

		if (closed)
			return false;
			
		context.getLogger().finer("Acquiring transport to " + socketAddress + ".");

		synchronized (owners)
		{
			if (closed)
				return false;
				
			owners.add(client);
		}
		
		return true;
	}

	/** 
	 * Releases transport.
	 * @param client client (channel) releasing the transport
	 */
	public synchronized void release(TransportClient client) {

		if (closed)
			return;
			
		context.getLogger().finer("Releasing transport to " + socketAddress + ".");

		synchronized (owners)
		{
			owners.remove(client);

			// not used anymore
			// TODO consider delayed destruction (can improve performance!!!)
			if (owners.size() == 0)
				close(false);
		}
	}

	/**
	 * Alive notification.
	 * This method needs to be called (by newly received data or beacon)
	 * at least once in this period, if not echo will be issued
	 * and if there is not reponse to it, transport will be considered as unresponsive.
	 */
	@Override
	public final void aliveNotification()
	{
		aliveTimestamp = System.currentTimeMillis();
		if (unresponsiveTransport)
			responsiveTransport();
	}

	/* (non-Javadoc)
	 * @see org.epics.pvData.misc.Timer.TimerCallback#timerStopped()
	 */
	@Override
	public void timerStopped() {
		// noop
	}

	/**
	 * Live-check timer.
	 * @see org.epics.pvData.misc.Timer.TimerCallback#callback()
	 */
	@Override
	public void callback()
	{
		final long diff = System.currentTimeMillis() - aliveTimestamp;
		if (diff > 2*connectionTimeout)
		{
			unresponsiveTransport();
		}
		else if (diff > connectionTimeout)
		{
			// send echo
			enqueueSendRequest(this);
		}
	}

	/**
	 * Responsive transport notify. 
	 */
	private void responsiveTransport()
	{
		if (unresponsiveTransport)
		{
		    unresponsiveTransport = false;
			synchronized (owners)
			{
				for (TransportClient client : owners)
				{
					try
					{
						client.transportResponsive(this);
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Unresponsive transport notify. 
	 */
	private void unresponsiveTransport()
	{
		if (!unresponsiveTransport)
		{
		    unresponsiveTransport = true;

			synchronized (owners)
			{
				for (TransportClient client : owners)
				{
					try
					{
						client.transportUnresponsive();
					}
					catch (Throwable th)
					{
						// TODO remove
						th.printStackTrace();
					}
				}
			}
		}
	}


	/**
	 * Changed transport (server restared) notify. 
	 */
	@Override
	public void changedTransport()
	{
		introspectionRegistry.reset();
		
		synchronized (owners)
		{
			for (TransportClient client : owners)
			{
				try
				{
					client.transportChanged();
				}
				catch (Throwable th)
				{
					// TODO remove
					th.printStackTrace();
				}
			}
		}
	}

	
	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#lock()
	 */
	@Override
	public void lock() {
		// noop
	}

	/* (non-Javadoc)
	 * @see org.epics.ca.impl.remote.TransportSender#unlock()
	 */
	@Override
	public void unlock() {
		// noop
	}

	// always called from the same thread, therefore no sync needed
	private boolean verifyOrEcho = true;
	
	/**
	 * CA connection validation response
	 */
	@Override
	public void send(ByteBuffer buffer, TransportSendControl control) {
		
		if (verifyOrEcho)
		{
			//
			// send verification response message
			//
			
			control.startMessage((byte)1, (2*Integer.SIZE+Short.SIZE)/Byte.SIZE);
	
			// receive buffer size
			buffer.putInt(getReceiveBufferSize());
	
			// socket receive buffer size
			buffer.putInt(getSocketReceiveBufferSize());
			
			// connection priority
			buffer.putShort(getPriority());
			
			// send immediately
			control.flush(true);
			
			verifyOrEcho = false;
		}
		else
		{
			control.startMessage((byte)2, 0);
			// send immediately
			control.flush(true);
		}
	}

}
