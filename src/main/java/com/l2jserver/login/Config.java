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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.util.PropertiesParser;
import com.l2jserver.util.network.StringUtil;

/**
 * This class loads all the game server related configurations from files.<br>
 * The files are usually located in config folder in server root folder.<br>
 * Each configuration has a default value (that should reflect retail behavior).
 */
public final class Config
{
	private static final Logger LOGGER = Logger.getLogger(Config.class.getName());
	
	public static final String EOL = System.lineSeparator();
	
	private static final String HEXID_FILE = "./config/hexid.txt";
	private static final String LOGIN_CONFIGURATION_FILE = "./config/LoginServer.properties";
	public static final String TELNET_FILE = "./config/Telnet.properties";
	private static final String MMO_CONFIG_FILE = "./config/MMO.properties";
	private static final String EMAIL_CONFIG_FILE = "./config/Email.properties";
	private static final String PROTOCOL_CONFIG_FILE = "./config/Protocol.properties";
	
	// --------------------------------------------------
	// General Settings
	// --------------------------------------------------
	public static boolean DEBUG;
	public static long CONNECTION_CLOSE_TIME;
	// --------------------------------------------------
	// Server Settings
	// --------------------------------------------------
	public static boolean ENABLE_UPNP;
	public static int PORT_LOGIN;
	public static String LOGIN_BIND_ADDRESS;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static String DATABASE_DRIVER;
	public static String DATABASE_URL;
	public static String DATABASE_LOGIN;
	public static String DATABASE_PASSWORD;
	public static int DATABASE_MAX_CONNECTIONS;
	public static int DATABASE_MAX_IDLE_TIME;
	public static File DATAPACK_ROOT;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	// --------------------------------------------------
	// Protocol Settings
	// --------------------------------------------------
	public static final Map<Integer, String> ALLOWED_PROTOCOLS = new HashMap<>();
	// --------------------------------------------------
	// MMO Settings
	// --------------------------------------------------
	public static int MMO_SELECTOR_SLEEP_TIME;
	public static int MMO_MAX_SEND_PER_PASS;
	public static int MMO_MAX_READ_PER_PASS;
	public static int MMO_HELPER_BUFFER_COUNT;
	public static boolean MMO_TCP_NODELAY;
	// --------------------------------------------------
	// No classification assigned to the following yet
	// --------------------------------------------------
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static boolean IS_TELNET_ENABLED;
	public static boolean SHOW_LICENCE;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	// --------------------------------------------------
	// Email
	// --------------------------------------------------
	public static String EMAIL_SERVERINFO_NAME;
	public static String EMAIL_SERVERINFO_ADDRESS;
	public static boolean EMAIL_SYS_ENABLED;
	public static String EMAIL_SYS_HOST;
	public static int EMAIL_SYS_PORT;
	public static boolean EMAIL_SYS_SMTP_AUTH;
	public static String EMAIL_SYS_FACTORY;
	public static boolean EMAIL_SYS_FACTORY_CALLBACK;
	public static String EMAIL_SYS_USERNAME;
	public static String EMAIL_SYS_PASSWORD;
	public static String EMAIL_SYS_ADDRESS;
	public static String EMAIL_SYS_SELECTQUERY;
	public static String EMAIL_SYS_DBFIELD;
	
	public static void load()
	{
		final PropertiesParser ServerSettings = new PropertiesParser(LOGIN_CONFIGURATION_FILE);
		
		ENABLE_UPNP = ServerSettings.getBoolean("EnableUPnP", true);
		GAME_SERVER_LOGIN_HOST = ServerSettings.getString("LoginHostname", "127.0.0.1");
		GAME_SERVER_LOGIN_PORT = ServerSettings.getInt("LoginPort", 9013);
		
		LOGIN_BIND_ADDRESS = ServerSettings.getString("LoginserverHostname", "*");
		PORT_LOGIN = ServerSettings.getInt("LoginserverPort", 2106);
		
		try
		{
			DATAPACK_ROOT = new File(ServerSettings.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
		}
		catch (IOException e)
		{
			LOGGER.log(Level.WARNING, "Error setting datapack root!", e);
			DATAPACK_ROOT = new File(".");
		}
		
		DEBUG = ServerSettings.getBoolean("Debug", false);
		
		ACCEPT_NEW_GAMESERVER = ServerSettings.getBoolean("AcceptNewGameServer", true);
		
		LOGIN_TRY_BEFORE_BAN = ServerSettings.getInt("LoginTryBeforeBan", 5);
		LOGIN_BLOCK_AFTER_BAN = ServerSettings.getInt("LoginBlockAfterBan", 900);
		
		LOGIN_SERVER_SCHEDULE_RESTART = ServerSettings.getBoolean("LoginRestartSchedule", false);
		LOGIN_SERVER_SCHEDULE_RESTART_TIME = ServerSettings.getLong("LoginRestartTime", 24);
		
		DATABASE_DRIVER = ServerSettings.getString("Driver", "com.mysql.jdbc.Driver");
		DATABASE_URL = ServerSettings.getString("URL", "jdbc:mysql://localhost/l2jls");
		DATABASE_LOGIN = ServerSettings.getString("Login", "root");
		DATABASE_PASSWORD = ServerSettings.getString("Password", "");
		DATABASE_MAX_CONNECTIONS = ServerSettings.getInt("MaximumDbConnections", 10);
		DATABASE_MAX_IDLE_TIME = ServerSettings.getInt("MaximumDbIdleTime", 0);
		CONNECTION_CLOSE_TIME = ServerSettings.getLong("ConnectionCloseTime", 60000);
		
		SHOW_LICENCE = ServerSettings.getBoolean("ShowLicence", true);
		
		AUTO_CREATE_ACCOUNTS = ServerSettings.getBoolean("AutoCreateAccounts", true);
		
		FLOOD_PROTECTION = ServerSettings.getBoolean("EnableFloodProtection", true);
		FAST_CONNECTION_LIMIT = ServerSettings.getInt("FastConnectionLimit", 15);
		NORMAL_CONNECTION_TIME = ServerSettings.getInt("NormalConnectionTime", 700);
		FAST_CONNECTION_TIME = ServerSettings.getInt("FastConnectionTime", 350);
		MAX_CONNECTION_PER_IP = ServerSettings.getInt("MaxConnectionPerIP", 50);
		
		// MMO
		final PropertiesParser mmoSettings = new PropertiesParser(MMO_CONFIG_FILE);
		
		MMO_SELECTOR_SLEEP_TIME = mmoSettings.getInt("SleepTime", 20);
		MMO_MAX_SEND_PER_PASS = mmoSettings.getInt("MaxSendPerPass", 12);
		MMO_MAX_READ_PER_PASS = mmoSettings.getInt("MaxReadPerPass", 12);
		MMO_HELPER_BUFFER_COUNT = mmoSettings.getInt("HelperBufferCount", 20);
		MMO_TCP_NODELAY = mmoSettings.getBoolean("TcpNoDelay", false);
		
		// Load Telnet L2Properties file (if exists)
		final PropertiesParser telnetSettings = new PropertiesParser(TELNET_FILE);
		
		IS_TELNET_ENABLED = telnetSettings.getBoolean("EnableTelnet", false);
		
		// Email
		final PropertiesParser emailSettings = new PropertiesParser(EMAIL_CONFIG_FILE);
		
		EMAIL_SERVERINFO_NAME = emailSettings.getString("ServerInfoName", "Unconfigured L2J Server");
		EMAIL_SERVERINFO_ADDRESS = emailSettings.getString("ServerInfoAddress", "info@myl2jserver.com");
		
		EMAIL_SYS_ENABLED = emailSettings.getBoolean("EmailSystemEnabled", false);
		EMAIL_SYS_HOST = emailSettings.getString("SmtpServerHost", "smtp.gmail.com");
		EMAIL_SYS_PORT = emailSettings.getInt("SmtpServerPort", 465);
		EMAIL_SYS_SMTP_AUTH = emailSettings.getBoolean("SmtpAuthRequired", true);
		EMAIL_SYS_FACTORY = emailSettings.getString("SmtpFactory", "javax.net.ssl.SSLSocketFactory");
		EMAIL_SYS_FACTORY_CALLBACK = emailSettings.getBoolean("SmtpFactoryCallback", false);
		EMAIL_SYS_USERNAME = emailSettings.getString("SmtpUsername", "user@gmail.com");
		EMAIL_SYS_PASSWORD = emailSettings.getString("SmtpPassword", "password");
		EMAIL_SYS_ADDRESS = emailSettings.getString("EmailSystemAddress", "noreply@myl2jserver.com");
		EMAIL_SYS_SELECTQUERY = emailSettings.getString("EmailDBSelectQuery", "SELECT value FROM account_data WHERE account_name=? AND var='email_addr'");
		EMAIL_SYS_DBFIELD = emailSettings.getString("EmailDBField", "value");
		
		// Protocol
		final PropertiesParser protocolSettings = new PropertiesParser(PROTOCOL_CONFIG_FILE);
		final String allProtocols = "Prelude, C1, C2, C3, C4, C5, Interlude, Kamael, Hellbound, Gracia1, Gracia2, GraciaFinal, Epilogue, Freya, HighFive, Awakening, Harmony, Tauti, GloryDays, Livindor, Valiance, Ertheia";
		String allowedProtocolList = protocolSettings.getString("AllowedProtocols", allProtocols);
		if (allowedProtocolList.equals("All"))
		{
			allowedProtocolList = allProtocols;
		}
		final String[] protocols = allowedProtocolList.replace(" ", "").split(",");
		for (String protocolName : protocols)
		{
			final int protocol = Integer.decode(protocolSettings.getString(protocolName, "-1"));
			if (protocol >= 0)
			{
				ALLOWED_PROTOCOLS.put(protocol, protocolName);
			}
		}
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.<br>
	 * Check {@link #HEXID_FILE}.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 */
	public static void saveHexid(int serverId, String hexId)
	{
		Config.saveHexid(serverId, hexId, HEXID_FILE);
	}
	
	/**
	 * Save hexadecimal ID of the server in the L2Properties file.
	 * @param serverId the ID of the server whose hexId to save
	 * @param hexId the hexadecimal ID to store
	 * @param fileName name of the L2Properties file
	 */
	public static void saveHexid(int serverId, String hexId, String fileName)
	{
		try
		{
			Properties hexSetting = new Properties();
			File file = new File(fileName);
			// Create a new empty file only if it doesn't exist
			file.createNewFile();
			try (OutputStream out = new FileOutputStream(file))
			{
				hexSetting.setProperty("ServerID", String.valueOf(serverId));
				hexSetting.setProperty("HexID", hexId);
				hexSetting.store(out, "the hexID to auth into login");
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(StringUtil.concat("Failed to save hex id to ", fileName, " File."));
			LOGGER.warning("Config: " + e.getMessage());
		}
	}
	
	public static int getServerTypeId(String[] serverTypes)
	{
		int serverType = 0;
		for (String cType : serverTypes)
		{
			switch (cType.trim().toLowerCase())
			{
				case "normal":
					serverType |= 0x01;
					break;
				case "relax":
					serverType |= 0x02;
					break;
				case "test":
					serverType |= 0x04;
					break;
				case "broad":
					serverType |= 0x08;
					break;
				case "restricted":
					serverType |= 0x10;
					break;
				case "event":
					serverType |= 0x20;
					break;
				case "free":
					serverType |= 0x40;
					break;
				case "world":
					serverType |= 0x100;
					break;
				case "new":
					serverType |= 0x200;
					break;
				case "classic":
					serverType |= 0x400;
					break;
			}
		}
		return serverType;
	}
}
