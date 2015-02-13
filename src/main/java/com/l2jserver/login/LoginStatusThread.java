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
package com.l2jserver.login;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.l2jserver.login.GameServerTable.GameServerInfo;

public class LoginStatusThread extends Thread
{
	private static final Logger _log = Logger.getLogger(LoginStatusThread.class.getName());
	
	private final Socket _cSocket;
	
	private final PrintWriter _print;
	private final BufferedReader _read;
	
	private boolean _redirectLogger;
	
	private void telnetOutput(int type, String text)
	{
		if (type == 1)
		{
			System.out.println("TELNET | " + text);
		}
		else if (type == 2)
		{
			System.out.print("TELNET | " + text);
		}
		else if (type == 3)
		{
			System.out.print(text);
		}
		else if (type == 4)
		{
			System.out.println(text);
		}
		else
		{
			System.out.println("TELNET | " + text);
		}
	}
	
	private boolean isValidIP(Socket client)
	{
		boolean result = false;
		InetAddress ClientIP = client.getInetAddress();
		
		// convert IP to String, and compare with list
		String clientStringIP = ClientIP.getHostAddress();
		
		telnetOutput(1, "Connection from: " + clientStringIP);
		
		// read and loop thru list of IPs, compare with newIP
		if (Config.DEBUG)
		{
			telnetOutput(2, "");
		}
		
		final File file = new File(Config.TELNET_FILE);
		try (InputStream telnetIS = new FileInputStream(file))
		{
			Properties telnetSettings = new Properties();
			telnetSettings.load(telnetIS);
			
			String HostList = telnetSettings.getProperty("ListOfHosts", "127.0.0.1,localhost,::1");
			
			if (Config.DEBUG)
			{
				telnetOutput(3, "Comparing ip to list...");
			}
			
			// compare
			String ipToCompare = null;
			for (String ip : HostList.split(","))
			{
				if (!result)
				{
					ipToCompare = InetAddress.getByName(ip).getHostAddress();
					if (clientStringIP.equals(ipToCompare))
					{
						result = true;
					}
					if (Config.DEBUG)
					{
						telnetOutput(3, clientStringIP + " = " + ipToCompare + "(" + ip + ") = " + result);
					}
				}
			}
		}
		catch (IOException e)
		{
			if (Config.DEBUG)
			{
				telnetOutput(4, "");
			}
			telnetOutput(1, "Error: " + e);
		}
		
		if (Config.DEBUG)
		{
			telnetOutput(4, "Allow IP: " + result);
		}
		return result;
	}
	
	public LoginStatusThread(Socket client, int uptime, String StatusPW) throws IOException
	{
		_cSocket = client;
		
		_print = new PrintWriter(_cSocket.getOutputStream());
		_read = new BufferedReader(new InputStreamReader(_cSocket.getInputStream()));
		
		if (isValidIP(client))
		{
			telnetOutput(1, client.getInetAddress().getHostAddress() + " accepted.");
			_print.println("Welcome To The L2J Telnet Session.");
			_print.println("Please Insert Your Password!");
			_print.print("Password: ");
			_print.flush();
			String tmpLine = _read.readLine();
			if (tmpLine == null)
			{
				_print.println("Error.");
				_print.println("Disconnected...");
				_print.flush();
				_cSocket.close();
			}
			else
			{
				if (!tmpLine.equals(StatusPW))
				{
					_print.println("Incorrect Password!");
					_print.println("Disconnected...");
					_print.flush();
					_cSocket.close();
				}
				else
				{
					_print.println("Password Correct!");
					_print.println("[L2J Login Server]");
					_print.print("");
					_print.flush();
					start();
				}
			}
		}
		else
		{
			telnetOutput(5, "Connection attempt from " + client.getInetAddress().getHostAddress() + " rejected.");
			_cSocket.close();
		}
	}
	
	@Override
	public void run()
	{
		String _usrCommand = "";
		try
		{
			while ((_usrCommand.compareTo("quit") != 0) && (_usrCommand.compareTo("exit") != 0))
			{
				_usrCommand = _read.readLine();
				if (_usrCommand == null)
				{
					_cSocket.close();
					break;
				}
				if (_usrCommand.equals("help"))
				{
					_print.println("The following is a list of all available commands: ");
					_print.println("help                - shows this help.");
					_print.println("status              - displays basic server statistics.");
					_print.println("unblock <ip>        - removes <ip> from banlist.");
					_print.println("shutdown			- shuts down server.");
					_print.println("restart				- restarts the server.");
					_print.println("RedirectLogger		- Telnet will give you some info about server in real time.");
					_print.println("quit                - closes telnet session.");
					_print.println("");
				}
				else if (_usrCommand.equals("status"))
				{
					final Map<Integer, GameServerInfo> gslist = GameServerTable.getInstance().getRegisteredGameServers();
					
					if (gslist.isEmpty())
					{
						_print.println("Registered Servers: 0");
					}
					else
					{
						_print.println("=== Registered Servers ===");
						_print.println("ID\tName\tStatus\tPlayers online");
						
						gslist.forEach((id, gsinfo) ->
						{
							_print.print(id);
							_print.print("\t");
							_print.print(gsinfo.getName());
							_print.print("\t");
							_print.print(gsinfo.getStatusName());
							_print.print("\t");
							_print.print(gsinfo.getCurrentPlayerCount());
							_print.println();
						});
					}
				}
				else if (_usrCommand.startsWith("unblock"))
				{
					try
					{
						_usrCommand = _usrCommand.substring(8);
						if (LoginController.getInstance().removeBanForAddress(_usrCommand))
						{
							_log.warning("IP removed via TELNET by host: " + _cSocket.getInetAddress().getHostAddress());
							_print.println("The IP " + _usrCommand + " has been removed from the hack protection list!");
						}
						else
						{
							_print.println("IP not found in hack protection list...");
						}
					}
					catch (StringIndexOutOfBoundsException e)
					{
						_print.println("Please Enter the IP to Unblock!");
					}
				}
				else if (_usrCommand.startsWith("shutdown"))
				{
					L2LoginServer.getInstance().shutdown(false);
					_print.println("Bye Bye!");
					_print.flush();
					_cSocket.close();
				}
				else if (_usrCommand.startsWith("restart"))
				{
					L2LoginServer.getInstance().shutdown(true);
					_print.println("Bye Bye!");
					_print.flush();
					_cSocket.close();
				}
				else if (_usrCommand.equals("RedirectLogger"))
				{
					_redirectLogger = true;
				}
				else if (_usrCommand.equals("quit"))
				{ /* Do Nothing :p - Just here to save us from the "Command Not Understood" Text */
				}
				else if (_usrCommand.isEmpty())
				{ /* Do Nothing Again - Same reason as the quit part */
				}
				else
				{
					_print.println("Invalid Command");
				}
				_print.print("");
				_print.flush();
			}
			if (!_cSocket.isClosed())
			{
				_print.println("Bye Bye!");
				_print.flush();
				_cSocket.close();
			}
			telnetOutput(1, "Connection from " + _cSocket.getInetAddress().getHostAddress() + " was closed by client.");
		}
		catch (IOException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	public void printToTelnet(String msg)
	{
		synchronized (_print)
		{
			_print.println(msg);
			_print.flush();
		}
	}
	
	/**
	 * @return Returns the redirectLogger.
	 */
	public boolean isRedirectLogger()
	{
		return _redirectLogger;
	}
}
