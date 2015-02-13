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

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.l2jserver.login.LoginController;
import com.l2jserver.util.db.L2DatabaseFactory;
import com.l2jserver.util.network.packets.BaseRecievePacket;

/**
 * @author mrTJO
 */
public class RequestTempBan extends BaseRecievePacket
{
	private static final Logger _log = Logger.getLogger(RequestTempBan.class.getName());
	
	private final String _accountName;
	@SuppressWarnings("unused")
	private String _banReason;
	private final String _ip;
	long _banTime;
	
	/**
	 * @param decrypt
	 */
	public RequestTempBan(byte[] decrypt)
	{
		super(decrypt);
		_accountName = readS();
		_ip = readS();
		_banTime = readQ();
		boolean haveReason = readC() == 0 ? false : true;
		if (haveReason)
		{
			_banReason = readS();
		}
		banUser();
	}
	
	private void banUser()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO account_data VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE value=?"))
		{
			ps.setString(1, _accountName);
			ps.setString(2, "ban_temp");
			ps.setString(3, Long.toString(_banTime));
			ps.setString(4, Long.toString(_banTime));
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		try
		{
			LoginController.getInstance().addBanForAddress(_ip, _banTime);
		}
		catch (UnknownHostException e)
		{
			
		}
	}
}
