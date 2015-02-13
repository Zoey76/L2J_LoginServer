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
package com.l2jserver.login.network.gameserverpackets;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Base64;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.login.GameServerTable;
import com.l2jserver.login.GameServerTable.GameServerInfo;
import com.l2jserver.login.GameServerThread;
import com.l2jserver.util.db.L2DatabaseFactory;
import com.l2jserver.util.network.packets.BaseRecievePacket;

/**
 * @author Nik
 */
public class ChangePassword extends BaseRecievePacket
{
	protected static Logger _log = Logger.getLogger(ChangePassword.class.getName());
	private static GameServerThread gst = null;
	
	public ChangePassword(byte[] decrypt)
	{
		super(decrypt);
		
		String accountName = readS();
		String characterName = readS();
		String curpass = readS();
		String newpass = readS();
		
		// get the GameServerThread
		Collection<GameServerInfo> serverList = GameServerTable.getInstance().getRegisteredGameServers().values();
		for (GameServerInfo gsi : serverList)
		{
			if ((gsi.getGameServerThread() != null) && gsi.getGameServerThread().hasAccountOnGameServer(accountName))
			{
				gst = gsi.getGameServerThread();
			}
		}
		
		if (gst == null)
		{
			return;
		}
		
		if ((curpass == null) || (newpass == null))
		{
			gst.ChangePasswordResponse((byte) 0, characterName, "Invalid password data! Try again.");
		}
		else
		{
			try
			{
				MessageDigest md = MessageDigest.getInstance("SHA");
				
				byte[] raw = curpass.getBytes("UTF-8");
				raw = md.digest(raw);
				String curpassEnc = Base64.getEncoder().encodeToString(raw);
				String pass = null;
				int passUpdated = 0;
				
				// SQL connection
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();
					PreparedStatement ps = con.prepareStatement("SELECT password FROM accounts WHERE login=?"))
				{
					ps.setString(1, accountName);
					try (ResultSet rs = ps.executeQuery())
					{
						if (rs.next())
						{
							pass = rs.getString("password");
						}
					}
				}
				
				if (curpassEnc.equals(pass))
				{
					byte[] password = newpass.getBytes("UTF-8");
					password = md.digest(password);
					
					// SQL connection
					try (Connection con = L2DatabaseFactory.getInstance().getConnection();
						PreparedStatement ps = con.prepareStatement("UPDATE accounts SET password=? WHERE login=?"))
					{
						ps.setString(1, Base64.getEncoder().encodeToString(password));
						ps.setString(2, accountName);
						passUpdated = ps.executeUpdate();
					}
					
					_log.log(Level.INFO, "The password for account " + accountName + " has been changed from " + curpassEnc + " to " + Base64.getEncoder().encodeToString(password));
					if (passUpdated > 0)
					{
						gst.ChangePasswordResponse((byte) 1, characterName, "You have successfully changed your password!");
					}
					else
					{
						gst.ChangePasswordResponse((byte) 0, characterName, "The password change was unsuccessful!");
					}
				}
				else
				{
					gst.ChangePasswordResponse((byte) 0, characterName, "The typed current password doesn't match with your current one.");
				}
			}
			catch (Exception e)
			{
				_log.warning("Error while changing password for account " + accountName + " requested by player " + characterName + "! " + e);
			}
		}
	}
}