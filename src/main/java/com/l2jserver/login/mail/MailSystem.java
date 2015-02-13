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
package com.l2jserver.login.mail;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastMap;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.l2jserver.login.Config;

/**
 * @author mrTJO
 */
public class MailSystem
{
	private static final Logger _log = Logger.getLogger(MailSystem.class.getName());
	private final Map<String, MailContent> _mailData = new FastMap<>();
	
	public static MailSystem getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public MailSystem()
	{
		loadMails();
	}
	
	public void sendMail(String account, String messageId, String... args)
	{
		BaseMail mail = new BaseMail(account, messageId, args);
		mail.run();
	}
	
	private void loadMails()
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		File file = new File(Config.DATAPACK_ROOT, "data/mail/MailList.xml");
		Document doc = null;
		if (file.exists())
		{
			try
			{
				doc = factory.newDocumentBuilder().parse(file);
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not parse MailList.xml file: " + e.getMessage(), e);
				return;
			}
			
			Node n = doc.getFirstChild();
			File mailFile;
			for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
			{
				if (d.getNodeName().equals("mail"))
				{
					String mailId = d.getAttributes().getNamedItem("id").getNodeValue();
					String subject = d.getAttributes().getNamedItem("subject").getNodeValue();
					String maFile = d.getAttributes().getNamedItem("file").getNodeValue();
					
					mailFile = new File(Config.DATAPACK_ROOT, "data/mail/" + maFile);
					try (FileInputStream fis = new FileInputStream(mailFile);
						BufferedInputStream bis = new BufferedInputStream(fis))
					{
						int bytes = bis.available();
						byte[] raw = new byte[bytes];
						
						bis.read(raw);
						String html = new String(raw, "UTF-8");
						html = html.replaceAll(Config.EOL, "\n");
						html = html.replace("%servermail%", Config.EMAIL_SERVERINFO_ADDRESS);
						html = html.replace("%servername%", Config.EMAIL_SERVERINFO_NAME);
						
						_mailData.put(mailId, new MailContent(subject, html));
					}
					catch (IOException e)
					{
						_log.warning("IOException while reading " + maFile);
					}
				}
			}
			_log.info("eMail System Loaded");
		}
		else
		{
			_log.warning("Cannot load eMail System - Missing file MailList.xml");
		}
	}
	
	public class MailContent
	{
		private final String _subject;
		private final String _text;
		
		/**
		 * @param subject
		 * @param text
		 */
		public MailContent(String subject, String text)
		{
			_subject = subject;
			_text = text;
		}
		
		public String getSubject()
		{
			return _subject;
		}
		
		public String getText()
		{
			return _text;
		}
	}
	
	public MailContent getMailContent(String mailId)
	{
		return _mailData.get(mailId);
	}
	
	private static class SingletonHolder
	{
		protected static final MailSystem _instance = new MailSystem();
	}
}
