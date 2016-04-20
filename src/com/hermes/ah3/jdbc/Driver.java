package com.hermes.ah3.jdbc;

import java.io.UnsupportedEncodingException;
import java.lang.ref.ReferenceQueue;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import com.hermes.ah3.jdbc.util.Messages;
import com.hermes.ah3.jdbc.util.StringUtils;


public class Driver implements java.sql.Driver {
	private static final String ALLOWED_QUOTES = "\"'";

	private static final String URL_PREFIX = "jdbc:hermes://";
	
	/**
	 * Key used to retreive the database value from the properties instance
	 * passed to the driver.
	 */
	public static final String DBNAME_PROPERTY_KEY = "DBNAME";

	/** Should the driver generate debugging output? */
	public static final boolean DEBUG = false;

	/** Index for hostname coming out of parseHostPortPair(). */
	public final static int HOST_NAME_INDEX = 0;

	/**
	 * Key used to retreive the hostname value from the properties instance
	 * passed to the driver.
	 */
	public static final String HOST_PROPERTY_KEY = "HOST";

	public static final String NUM_HOSTS_PROPERTY_KEY = "NUM_HOSTS";
	
	/**
	 * Key used to retreive the password value from the properties instance
	 * passed to the driver.
	 */
	public static final String PASSWORD_PROPERTY_KEY = "password";

	/** Index for port # coming out of parseHostPortPair(). */
	public final static int PORT_NUMBER_INDEX = 1;

	/**
	 * Key used to retreive the port number value from the properties instance
	 * passed to the driver.
	 */
	public static final String PORT_PROPERTY_KEY = "PORT";
	
	//涓嶇煡閬撲粈涔堝惈涔夛紝娌℃湁瀹炵幇
	//public static final String PROPERTIES_TRANSFORM_KEY = "propertiesTransform";

	/** Should the driver generate method-call traces? */
	public static final boolean TRACE = false;

	public static final String USE_CONFIG_PROPERTY_KEY = "useConfigs";

	/**
	 * Key used to retreive the username value from the properties instance
	 * passed to the driver.
	 */
	public static final String USER_PROPERTY_KEY = "user";

	public static final String PROTOCOL_PROPERTY_KEY = "PROTOCOL";

	public static final String PATH_PROPERTY_KEY = "PATH";
	
	
//	protected static final ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference> connectionPhantomRefs = new ConcurrentHashMap<ConnectionPhantomReference, ConnectionPhantomReference>();
	protected static final ReferenceQueue<ConnectionImpl> refQueue = new ReferenceQueue<ConnectionImpl>();

	static{

		// Register the Driver with DriverManager
		try {
			java.sql.DriverManager.registerDriver(new Driver());
		} catch (SQLException E) {
			throw new RuntimeException("Can't register driver!");
		}

	}



	public boolean acceptsURL(String url) throws SQLException {
		// TODO Auto-generated method stub
		return (parseURL(url, null) != null);
//		return false;
	}

	public Connection connect(String url, Properties info) throws SQLException {
		// TODO Auto-generated method stub

//		/*
		Properties props = null;

		if ((props = parseURL(url, info)) == null) {
			return null;
		}
		/*
		if (!"1".equals(props.getProperty(NUM_HOSTS_PROPERTY_KEY))) {
			return connectFailover(url, info);
		}
		*/
		
		try {
			Connection newConn = com.hermes.ah3.jdbc.ConnectionImpl.getInstance(
					host(props), port(props), props, database(props), url);
			
			return newConn;
		} catch (SQLException sqlEx) {
			// Don't wrap SQLExceptions, throw
			// them un-changed.
			throw sqlEx;
		} catch (Exception ex) {
			ex.printStackTrace();
			/*
			SQLException sqlEx = SQLError.createSQLException(Messages
					.getString("NonRegisteringDriver.17") //$NON-NLS-1$
					+ ex.toString()
					+ Messages.getString("NonRegisteringDriver.18"), //$NON-NLS-1$
					SQLError.SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE, null);
			
			sqlEx.initCause(ex);
			throw sqlEx;
			*/
		}
//		*/
		return null;
	}

	public int getMajorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getMinorVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
			throws SQLException {
		// TODO Auto-generated method stub
		if (info == null) {
			info = new Properties();
		}

		if ((url != null) && url.startsWith(URL_PREFIX)) { //$NON-NLS-1$
			info = parseURL(url, info);
		}

		DriverPropertyInfo hostProp = new DriverPropertyInfo(HOST_PROPERTY_KEY, //$NON-NLS-1$
				info.getProperty(HOST_PROPERTY_KEY)); //$NON-NLS-1$
		hostProp.required = true;
		hostProp.description = Messages.getString("NonRegisteringDriver.3"); //$NON-NLS-1$

		DriverPropertyInfo portProp = new DriverPropertyInfo(PORT_PROPERTY_KEY, //$NON-NLS-1$
				info.getProperty(PORT_PROPERTY_KEY, Ah3DriverConstant.DB_DEFAULT_PORT)); //$NON-NLS-1$ //$NON-NLS-2$
		portProp.required = false;
		portProp.description = Messages.getString("NonRegisteringDriver.7"); //$NON-NLS-1$

		DriverPropertyInfo dbProp = new DriverPropertyInfo(DBNAME_PROPERTY_KEY, //$NON-NLS-1$
				info.getProperty(DBNAME_PROPERTY_KEY)); //$NON-NLS-1$
		dbProp.required = false;
		dbProp.description = "Database name"; //$NON-NLS-1$

		DriverPropertyInfo userProp = new DriverPropertyInfo(USER_PROPERTY_KEY, //$NON-NLS-1$
				info.getProperty(USER_PROPERTY_KEY)); //$NON-NLS-1$
		userProp.required = true;
		userProp.description = Messages.getString("NonRegisteringDriver.13"); //$NON-NLS-1$

		DriverPropertyInfo passwordProp = new DriverPropertyInfo(
				PASSWORD_PROPERTY_KEY, //$NON-NLS-1$
				info.getProperty(PASSWORD_PROPERTY_KEY)); //$NON-NLS-1$
		passwordProp.required = true;
		passwordProp.description = Messages
				.getString("NonRegisteringDriver.16"); //$NON-NLS-1$

		DriverPropertyInfo[] dpi = new DriverPropertyInfo[5];

		dpi[0] = hostProp;
		dpi[1] = portProp;
		dpi[2] = dbProp;
		dpi[3] = userProp;
		dpi[4] = passwordProp;

		return dpi;
//		return null;
	}

	public boolean jdbcCompliant() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//jdbc:hermes://localhost:3306/?user=root&password=root
	public Properties parseURL(String url, Properties defaults)
			throws java.sql.SQLException {
		Properties urlProps = (defaults != null) ? new Properties(defaults)
				: new Properties();

		if (url == null) {
			return null;
		}

		if (!StringUtils.startsWithIgnoreCase(url, URL_PREFIX)) { //$NON-NLS-1$
			return null;
		}

		int beginningOfSlashes = url.indexOf("//");

		/*
		 * Parse parameters after the ? in the URL and remove them from the
		 * original URL.
		 */
		int index = url.indexOf("?"); //$NON-NLS-1$

		if (index != -1) {
			String paramString = url.substring(index + 1, url.length());
			url = url.substring(0, index);

			StringTokenizer queryParams = new StringTokenizer(paramString, "&"); //$NON-NLS-1$

			while (queryParams.hasMoreTokens()) {
				String parameterValuePair = queryParams.nextToken();

				int indexOfEquals = StringUtils.indexOfIgnoreCase(0,
						parameterValuePair, "=");

				String parameter = null;
				String value = null;

				if (indexOfEquals != -1) {
					parameter = parameterValuePair.substring(0, indexOfEquals);

					if (indexOfEquals + 1 < parameterValuePair.length()) {
						value = parameterValuePair.substring(indexOfEquals + 1);
					}
				}

				if ((value != null && value.length() > 0)
						&& (parameter != null && parameter.length() > 0)) {
					try {
						urlProps.put(parameter, URLDecoder.decode(value,
								"UTF-8"));
					} catch (UnsupportedEncodingException badEncoding) {
						// punt
						urlProps.put(parameter, URLDecoder.decode(value));
					} catch (NoSuchMethodError nsme) {
						// punt again
						urlProps.put(parameter, URLDecoder.decode(value));
					}
				}
			}
		}

		url = url.substring(beginningOfSlashes + 2);

		String hostStuff = null;

		int slashIndex = StringUtils.indexOfIgnoreCaseRespectMarker(0, url,
				"/", ALLOWED_QUOTES, ALLOWED_QUOTES, true); //$NON-NLS-1$

		if (slashIndex != -1) {
			hostStuff = url.substring(0, slashIndex);

			if ((slashIndex + 1) < url.length()) {
				urlProps.put(DBNAME_PROPERTY_KEY, //$NON-NLS-1$
						url.substring((slashIndex + 1), url.length()));
			}
		} else {
			hostStuff = url;
		}

		int numHosts = 0;

		if ((hostStuff != null) && (hostStuff.trim().length() > 0)) {
			List<String> hosts = StringUtils.split(hostStuff, ",",
					ALLOWED_QUOTES, ALLOWED_QUOTES, false);

			for (String hostAndPort : hosts) {
				numHosts++;

				String[] hostPortPair = parseHostPortPair(hostAndPort);

				if (hostPortPair[HOST_NAME_INDEX] != null
						&& hostPortPair[HOST_NAME_INDEX].trim().length() > 0) {
					urlProps.setProperty(HOST_PROPERTY_KEY + "." + numHosts,
							hostPortPair[HOST_NAME_INDEX]);
				} else {
					urlProps.setProperty(HOST_PROPERTY_KEY + "." + numHosts,
							Ah3DriverConstant.DB_DEFAULT_HOST);
				}

				if (hostPortPair[PORT_NUMBER_INDEX] != null) {
					urlProps.setProperty(PORT_PROPERTY_KEY + "." + numHosts,
							hostPortPair[PORT_NUMBER_INDEX]);
				} else {
					urlProps.setProperty(PORT_PROPERTY_KEY + "." + numHosts,
							Ah3DriverConstant.DB_DEFAULT_PORT);
				}
			}
		} else {
			numHosts = 1;
			urlProps.setProperty(HOST_PROPERTY_KEY + ".1", Ah3DriverConstant.DB_DEFAULT_HOST);
			urlProps.setProperty(PORT_PROPERTY_KEY + ".1", Ah3DriverConstant.DB_DEFAULT_PORT);
		}

		urlProps.setProperty(NUM_HOSTS_PROPERTY_KEY, String.valueOf(numHosts));
		urlProps.setProperty(HOST_PROPERTY_KEY, urlProps
				.getProperty(HOST_PROPERTY_KEY + ".1"));
		urlProps.setProperty(PORT_PROPERTY_KEY, urlProps
				.getProperty(PORT_PROPERTY_KEY + ".1"));

		
		//涓嶆敮鎸乧oldFusion
		/*
		if (Util.isColdFusion()
				&& urlProps.getProperty("autoConfigureForColdFusion", "true")
						.equalsIgnoreCase("true")) {
			String configs = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);

			StringBuffer newConfigs = new StringBuffer();

			if (configs != null) {
				newConfigs.append(configs);
				newConfigs.append(",");
			}

			newConfigs.append("coldFusion");

			urlProps
					.setProperty(USE_CONFIG_PROPERTY_KEY, newConfigs.toString());
		}
		*/
		// If we use a config, it actually should get overridden by anything in
		// the URL or passed-in properties

		//鏀寔閰嶇疆鏂囦欢涓斿涓�鏆備笉鏀寔锛�
		/*
		String configNames = null;

		if (defaults != null) {
			configNames = defaults.getProperty(USE_CONFIG_PROPERTY_KEY);
		}

		if (configNames == null) {
			configNames = urlProps.getProperty(USE_CONFIG_PROPERTY_KEY);
		}
		if (configNames != null) {
			List<String> splitNames = StringUtils.split(configNames, ",", true);

			Properties configProps = new Properties();

			Iterator<String> namesIter = splitNames.iterator();

			while (namesIter.hasNext()) {
				String configName = namesIter.next();

				try {
					InputStream configAsStream = getClass()
							.getResourceAsStream(
									"configs/" + configName + ".properties");
					//鎵句笉鍒伴厤缃枃浠�
					if (configAsStream == null) {
						throw SQLError
								.createSQLException(
										"Can't find configuration template named '"
												+ configName + "'",
										SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE,
										null);
					}
					configProps.load(configAsStream);
				} catch (IOException ioEx) {
					SQLException sqlEx = SQLError.createSQLException(
							"Unable to load configuration template '"
									+ configName
									+ "' due to underlying IOException: "
									+ ioEx,
							SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE,
							null);
					sqlEx.initCause(ioEx);

					throw sqlEx;
				}
			}

			Iterator<Object> propsIter = urlProps.keySet().iterator();

			while (propsIter.hasNext()) {
				String key = propsIter.next().toString();
				String property = urlProps.getProperty(key);
				configProps.setProperty(key, property);
			}

			urlProps = configProps;
		}
		*/
		
		// Properties passed in should override ones in URL

		if (defaults != null) {
			Iterator<Object> propsIter = defaults.keySet().iterator();

			while (propsIter.hasNext()) {
				String key = propsIter.next().toString();
				if (!key.equals(NUM_HOSTS_PROPERTY_KEY)) {
					String property = defaults.getProperty(key);
					urlProps.setProperty(key, property);
				}
			}
		}

		return urlProps;
	}

	/**
	 * Parses hostPortPair in the form of [host][:port] into an array, with the
	 * element of index HOST_NAME_INDEX being the host (or null if not
	 * specified), and the element of index PORT_NUMBER_INDEX being the port (or
	 * null if not specified).
	 * 
	 * @param hostPortPair
	 *            host and port in form of of [host][:port]
	 * 
	 * @return array containing host and port as Strings
	 * 
	 * @throws SQLException
	 *             if a parse error occurs
	 */
	protected static String[] parseHostPortPair(String hostPortPair)
			throws SQLException {
		

		String[] splitValues = new String[2];

		if (StringUtils.startsWithIgnoreCaseAndWs(hostPortPair, "address")) {
			splitValues[HOST_NAME_INDEX] = hostPortPair.trim();
			splitValues[PORT_NUMBER_INDEX] = null;
			
			return splitValues;
		}
		
		int portIndex = hostPortPair.indexOf(":"); //$NON-NLS-1$
		
		String hostname = null;

		if (portIndex != -1) {
			if ((portIndex + 1) < hostPortPair.length()) {
				String portAsString = hostPortPair.substring(portIndex + 1);
				hostname = hostPortPair.substring(0, portIndex);

				splitValues[HOST_NAME_INDEX] = hostname;

				splitValues[PORT_NUMBER_INDEX] = portAsString;
			} else {
				try {
					throw new Exception("娌℃湁瀹氫箟绔彛");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*
				throw SQLError.createSQLException(Messages
						.getString("NonRegisteringDriver.37"), //$NON-NLS-1$
						SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, null);
				*/
			}
		} else {
			splitValues[HOST_NAME_INDEX] = hostPortPair;
			splitValues[PORT_NUMBER_INDEX] = null;
		}

		return splitValues;
	}
	/**
	 * Returns the hostname property
	 * 
	 * @param props
	 *            the java.util.Properties instance to retrieve the hostname
	 *            from.
	 * 
	 * @return the hostname
	 */
	public String host(Properties props) {
		return props.getProperty(HOST_PROPERTY_KEY, Ah3DriverConstant.DB_DEFAULT_HOST); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the port number property
	 * 
	 * @param props
	 *            the properties to get the port number from
	 * 
	 * @return the port number
	 */
	public int port(Properties props) {
		return Integer.parseInt(props.getProperty(PORT_PROPERTY_KEY, Ah3DriverConstant.DB_DEFAULT_PORT)); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns the database property from <code>props</code>
	 * 
	 * @param props
	 *            the Properties to look for the database property.
	 * 
	 * @return the database name.
	 */
	public String database(Properties props) {
		return props.getProperty(DBNAME_PROPERTY_KEY); //$NON-NLS-1$
	}
	
	public static boolean isHostPropertiesList(String host) {
		return host != null && StringUtils.startsWithIgnoreCase(host, "address=");
	}
	
	/**
	 * Expands hosts of the form address=(protocol=tcp)(host=localhost)(port=3306)
	 * into a java.util.Properties. Special characters (in this case () and =) must be quoted.
	 * Any values that are string-quoted ("" or '') are also stripped of quotes.
	 */
	public static Properties expandHostKeyValues(String host) {
		Properties hostProps = new Properties();
		
		if (isHostPropertiesList(host)) {
			host = host.substring("address=".length() + 1);
			List<String> hostPropsList = StringUtils.split(host, ")", "'\"", "'\"", true);
			
			for (String propDef : hostPropsList) {
				if (propDef.startsWith("(")) {
					propDef = propDef.substring(1);
				}
				
				List<String> kvp = StringUtils.split(propDef, "=", "'\"", "'\"", true);
				
				String key = kvp.get(0);
				String value = kvp.size() > 1 ? kvp.get(1) : null;
				
				if (value != null && ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'")))) {
					value = value.substring(1, value.length() - 1);
				}
				
				if (value != null) {
					if (HOST_PROPERTY_KEY.equalsIgnoreCase(key) ||
							DBNAME_PROPERTY_KEY.equalsIgnoreCase(key) ||
							PORT_PROPERTY_KEY.equalsIgnoreCase(key) ||
							PROTOCOL_PROPERTY_KEY.equalsIgnoreCase(key) ||
							PATH_PROPERTY_KEY.equalsIgnoreCase(key)) {
						key = key.toUpperCase(Locale.ENGLISH);
					} else if (USER_PROPERTY_KEY.equalsIgnoreCase(key) ||
							PASSWORD_PROPERTY_KEY.equalsIgnoreCase(key)) {
						key = key.toLowerCase(Locale.ENGLISH);
					}
					
					hostProps.setProperty(key, value);
				}
			}
		}
		
		return hostProps;
	}
	protected static void trackConnection(Connection newConn) {
		
//		ConnectionPhantomReference phantomRef = new ConnectionPhantomReference((ConnectionImpl) newConn, refQueue);
//		connectionPhantomRefs.put(phantomRef, phantomRef);
	}
	
//	static class ConnectionPhantomReference extends PhantomReference<ConnectionImpl> {
//		private NetworkResources io;
//		
//		ConnectionPhantomReference(ConnectionImpl connectionImpl, ReferenceQueue<ConnectionImpl> q) {
//			super(connectionImpl, q);
//			
//			try {
//				io = connectionImpl.getIO().getNetworkResources();
//			} catch (SQLException e) {
//				// if we somehow got here and there's really no i/o, we deal with it later
//			}
//		}
//		
//		void cleanup() {
//			if (io != null) {
//				try {
//					io.forceClose();
//				} finally {
//					io = null;
//				}
//			}
//		}
//	}

}
