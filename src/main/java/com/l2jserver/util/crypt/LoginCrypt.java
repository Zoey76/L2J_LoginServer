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
package com.l2jserver.util.crypt;

import java.io.IOException;

import com.l2jserver.util.Rnd;

/**
 * @author KenM
 */
public class LoginCrypt
{
	private static final byte[] STATIC_BLOWFISH_KEY =
	{
		(byte) 0x6b,
		(byte) 0x60,
		(byte) 0xcb,
		(byte) 0x5b,
		(byte) 0x82,
		(byte) 0xce,
		(byte) 0x90,
		(byte) 0xb1,
		(byte) 0xcc,
		(byte) 0x2b,
		(byte) 0x6c,
		(byte) 0x55,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c,
		(byte) 0x6c
	};
	
	private static final NewCrypt _STATIC_CRYPT = new NewCrypt(STATIC_BLOWFISH_KEY);
	private NewCrypt _crypt = null;
	private boolean _static = true;
	
	/**
	 * Method to initialize the the blowfish cipher with dynamic key.
	 * @param key the blowfish key to initialize the dynamic blowfish cipher with
	 */
	public void setKey(byte[] key)
	{
		_crypt = new NewCrypt(key);
	}
	
	/**
	 * Method to decrypt an incoming login client packet.
	 * @param raw array with encrypted data
	 * @param offset offset where the encrypted data is located
	 * @param size number of bytes of encrypted data
	 * @return true when checksum could be verified, false otherwise
	 * @throws IOException the size is not multiple of blowfishs block size or the raw array can't hold size bytes starting at offset due to it's size
	 */
	public boolean decrypt(byte[] raw, final int offset, final int size) throws IOException
	{
		if ((size % 8) != 0)
		{
			throw new IOException("size have to be multiple of 8");
		}
		if ((offset + size) > raw.length)
		{
			throw new IOException("raw array too short for size starting from offset");
		}
		
		_crypt.decrypt(raw, offset, size);
		return NewCrypt.verifyChecksum(raw, offset, size);
	}
	
	/**
	 * Method to encrypt an outgoing packet to login client.<br>
	 * Performs padding and resizing of data array.
	 * @param raw array with plain data
	 * @param offset offset where the plain data is located
	 * @param size number of bytes of plain data
	 * @return the new array size
	 * @throws IOException packet is too long to make padding and add verification data
	 */
	public int encrypt(byte[] raw, final int offset, int size) throws IOException
	{
		// reserve checksum
		size += 4;
		
		if (_static)
		{
			// reserve for XOR "key"
			size += 4;
			
			// padding
			size += 8 - (size % 8);
			if ((offset + size) > raw.length)
			{
				throw new IOException("packet too long");
			}
			NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt());
			_STATIC_CRYPT.crypt(raw, offset, size);
			_static = false;
		}
		else
		{
			// padding
			size += 8 - (size % 8);
			if ((offset + size) > raw.length)
			{
				throw new IOException("packet too long");
			}
			NewCrypt.appendChecksum(raw, offset, size);
			_crypt.crypt(raw, offset, size);
		}
		return size;
	}
}
