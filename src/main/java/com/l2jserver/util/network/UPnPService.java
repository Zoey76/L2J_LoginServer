/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.util.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.xml.sax.SAXException;

import com.l2jserver.login.Config;

/**
 * @author UnAfraid
 */
public class UPnPService
{
	private static final Logger _log = Logger.getLogger(UPnPService.class.getName());
	private static final String PROTOCOL = "TCP";
	
	private final GatewayDiscover _gatewayDiscover = new GatewayDiscover();
	private GatewayDevice _activeGW;
	
	protected UPnPService()
	{
		try
		{
			load();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, getClass().getSimpleName() + ": error while initializing: ", e);
		}
	}
	
	private void load() throws Exception
	{
		if (!Config.ENABLE_UPNP)
		{
			_log.log(Level.WARNING, "UPnP Service is disabled.");
			return;
		}
		
		_log.log(Level.INFO, "Looking for UPnP Gateway Devices...");
		
		final Map<InetAddress, GatewayDevice> gateways = _gatewayDiscover.discover();
		if (gateways.isEmpty())
		{
			_log.log(Level.INFO, "No UPnP gateways found");
			return;
		}
		
		// choose the first active gateway for the tests
		_activeGW = _gatewayDiscover.getValidGateway();
		if (_activeGW != null)
		{
			_log.log(Level.INFO, "Using UPnP gateway: " + _activeGW.getFriendlyName());
		}
		else
		{
			_log.log(Level.INFO, "No active UPnP gateway found");
			return;
		}
		
		_log.log(Level.INFO, "Using local address: " + _activeGW.getLocalAddress().getHostAddress() + " External address: " + _activeGW.getExternalIPAddress());
		
		addPortMapping(Config.PORT_LOGIN, "L2J Login Server");
	}
	
	public void removeAllPorts() throws Exception
	{
		if (_activeGW != null)
		{
			deletePortMapping(Config.PORT_LOGIN);
		}
	}
	
	private void addPortMapping(int port, String description) throws IOException, SAXException
	{
		final PortMappingEntry portMapping = new PortMappingEntry();
		final InetAddress localAddress = _activeGW.getLocalAddress();
		
		// Attempt to re-map
		if (_activeGW.getSpecificPortMappingEntry(port, PROTOCOL, portMapping))
		{
			_activeGW.deletePortMapping(port, PROTOCOL);
		}
		
		if (_activeGW.addPortMapping(port, port, localAddress.getHostAddress(), PROTOCOL, description))
		{
			_log.log(Level.INFO, "Mapping successfull on [" + localAddress.getHostAddress() + ":" + port + "]");
		}
		else
		{
			_log.log(Level.INFO, "Mapping failed on [" + localAddress.getHostAddress() + ":" + port + "] - Already mapped?");
		}
	}
	
	private void deletePortMapping(int port) throws IOException, SAXException
	{
		if (_activeGW.deletePortMapping(port, PROTOCOL))
		{
			_log.log(Level.INFO, "Mapping was deleted from [" + _activeGW.getLocalAddress().getHostAddress() + ":" + port + "]");
		}
	}
	
	public static UPnPService getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final UPnPService _instance = new UPnPService();
	}
}
