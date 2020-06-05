/*
 * Copyright (C) 2014 joseph
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.jwebnet.nerdreportcard.i18n;

/**
 *
 * @author joseph
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.jwebnet.nerdreportcard.NerdReportCard;


public class I18n
{
	private static I18n instance;
	private static final String MESSAGES = "messages";
	private final transient Locale defaultLocale = Locale.getDefault();
	private transient Locale currentLocale = defaultLocale;
	private transient ResourceBundle customBundle;
	private transient ResourceBundle localeBundle;
	private final transient ResourceBundle defaultBundle;
	private final transient Map<String, MessageFormat> messageFormatCache = new HashMap<String, MessageFormat>();
        private final transient NerdReportCard nrc;
	private static final Pattern NODOUBLEMARK = Pattern.compile("''");

	public I18n(final NerdReportCard nrc)
	{
		this.nrc = nrc;
		customBundle = ResourceBundle.getBundle(MESSAGES, defaultLocale, new FileResClassLoader(I18n.class.getClassLoader(), nrc));
		localeBundle = ResourceBundle.getBundle(MESSAGES, defaultLocale);
		defaultBundle = ResourceBundle.getBundle(MESSAGES, Locale.ENGLISH);
	}

	public void onEnable()
	{
		instance = this;
	}

	public void onDisable()
	{
		instance = null;
	}

	public Locale getCurrentLocale()
	{
		return currentLocale;
	}

	private String translate(final String string)
	{
		try
		{
			try
			{
				return customBundle.getString(string);
			}
			catch (MissingResourceException ex)
			{
				return localeBundle.getString(string);
			}
		}
		catch (MissingResourceException ex)
		{
			Logger.getLogger("NerdReportCard").log(Level.WARNING, String.format("Missing translation key \"%s\" in translation file %s", ex.getKey(), localeBundle.getLocale().toString()), ex);
			return defaultBundle.getString(string);
		}
	}

	public static String tl(final String string, final Object... objects)
	{
		if (instance == null)
		{
			return "";
		}
		if (objects.length == 0)
		{
			return NODOUBLEMARK.matcher(instance.translate(string)).replaceAll("'");
		}
		else
		{
			return instance.format(string, objects);
		}
	}

	public String format(final String string, final Object... objects)
	{
		String format = translate(string);
		MessageFormat messageFormat = messageFormatCache.get(format);
		if (messageFormat == null)
		{
			try
			{
				messageFormat = new MessageFormat(format);
			}
			catch (IllegalArgumentException e)
			{
				nrc.getLogger().log(Level.SEVERE, "Invalid Translation key for '" + string + "': " + e.getMessage());
				format = format.replaceAll("\\{(\\D*?)\\}", "\\[$1\\]");
				messageFormat = new MessageFormat(format);
			}
			messageFormatCache.put(format, messageFormat);
		}
		return messageFormat.format(objects);
	}

	public void updateLocale(final String loc)
	{
		if (loc == null || loc.isEmpty())
		{
			return;
		}
		final String[] parts = loc.split("[_\\.]");
		if (parts.length == 1)
		{
			currentLocale = new Locale(parts[0]);
		}
		if (parts.length == 2)
		{
			currentLocale = new Locale(parts[0], parts[1]);
		}
		if (parts.length == 3)
		{
			currentLocale = new Locale(parts[0], parts[1], parts[2]);
		}
		ResourceBundle.clearCache();
		Logger.getLogger("NerdReportCard").log(Level.INFO, String.format("Using locale %s", currentLocale.toString()));
		customBundle = ResourceBundle.getBundle(MESSAGES, currentLocale, new FileResClassLoader(I18n.class.getClassLoader(), nrc));
		localeBundle = ResourceBundle.getBundle(MESSAGES, currentLocale);
	}

	public static String capitalCase(final String input)
	{
		return input == null || input.length() == 0
			   ? input
			   : input.toUpperCase(Locale.ENGLISH).charAt(0)
				 + input.toLowerCase(Locale.ENGLISH).substring(1);
	}


	private static class FileResClassLoader extends ClassLoader
	{
		private final transient File dataFolder;

		FileResClassLoader(final ClassLoader classLoader, final NerdReportCard nrc)
		{
			super(classLoader);
			this.dataFolder = nrc.getDataFolder();
		}

		@Override
		public URL getResource(final String string)
		{
			final File file = new File(dataFolder, string);
			if (file.exists())
			{
				try
				{
					return file.toURI().toURL();
				}
				catch (MalformedURLException ex)
				{
				}
			}
			return super.getResource(string);
		}

		@Override
		public InputStream getResourceAsStream(final String string)
		{
			final File file = new File(dataFolder, string);
			if (file.exists())
			{
				try
				{
					return new FileInputStream(file);
				}
				catch (FileNotFoundException ex)
				{
				}
			}
			return super.getResourceAsStream(string);
		}
	}
}