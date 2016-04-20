package com.hermes.ah3.jdbc;

import java.lang.reflect.InvocationHandler;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import com.hermes.ah3.jdbc.util.SQLError;
import com.hermes.ah3.jdbc.util.ExceptionInterceptor;
import com.hermes.ah3.jdbc.util.Extension;

import com.hermes.ah3.jdbc.communication.Ah3IO;
import com.hermes.ah3.jdbc.communication.Ah3IOImplByC;
import com.hermes.ah3.jdbc.communication.Iah3IO;



public class ConnectionImpl implements Connection {
	/** Has this connection been closed? */
	private boolean isClosed = true;
	
	/** Why was this connection implicitly closed, if known? (for diagnostics) */
	private Throwable forceClosedReason;

	private static final int DEFAULT_RESULT_SET_TYPE = ResultSet.TYPE_FORWARD_ONLY; //TYPE_SCROLL_INSENSITIVE  TYPE_SCROLL_SENSITIVE
	
	private static final int DEFAULT_RESULT_SET_CONCURRENCY = ResultSet.CONCUR_READ_ONLY;//CONCUR_UPDATABLE

	private Iah3IO ah3Io;
	
	/** The point in time when this connection was created */
	private long connectionCreationTimeMillis = 0;

	private String origHostToConnectTo;

	// we don't want to be able to publicly clone this...
	
	private int origPortToConnectTo;

	private String origDatabaseToConnectTo;

	private Calendar sessionCalendar;
	
	private Calendar utcCalendar;
	private TimeZone defaultTimeZone;
	
	private boolean isClientTzUTC = false;

	/** A map of currently open statements */
	private Map<Statement, Statement> openStatements;
	
	/** The hostname we're connected to */
	private String host = null;
	
	private String hostPortPair;

	/** The port number we're connected to (defaults to DEFAULT) */
	private int port = Integer.parseInt(Ah3DriverConstant.DB_DEFAULT_PORT);
	/** The database we're currently using (called Catalog in JDBC terms). */
	private String database = null;
	private boolean autoCommit = true;

	/** The user we're connected as */
	private String user = null;
	
	/** The password we used */
	private String password = null;

	/** The JDBC URL we're using */
	private String myURL = null;

	/** Properties for this connection specified by user */
	protected Properties props = null;
	
	/** Internal DBMD to use for various database-version specific features */
	private DatabaseMetaData dbmd = null;

	private InvocationHandler realProxy = null;

	private ConnectionImpl proxy = null;

	private int maxAllowedPacket = 1024*1024;
	
	private int defaultFetchSize = 100;
	
	private boolean useUnicode = true;
	
	private String characterEncoding = "";
	
	private ExceptionInterceptor exceptionInterceptor;
	private List<Extension> connectionLifecycleInterceptors;
	
	public ExceptionInterceptor getExceptionInterceptor() {
		return this.exceptionInterceptor;
	}
	
	
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	public boolean getUseUnicode() {
		return useUnicode;
	}

	public void setUseUnicode(boolean useUnicode) {
		this.useUnicode = useUnicode;
	}

	public int getDefaultFetchSize() {
		return defaultFetchSize;
	}

	public void setDefaultFetchSize(int defaultFetchSize) {
		this.defaultFetchSize = defaultFetchSize;
	}

	public int getMaxAllowedPacket() {
		return maxAllowedPacket;
	}

	public void setMaxAllowedPacket(int maxAllowedPacket) {
		this.maxAllowedPacket = maxAllowedPacket;
	}

	public void setRealProxy(InvocationHandler proxy) {
		this.realProxy = proxy;
	}

	public Object getConnectionMutex() {
		return (this.realProxy != null) ? this.realProxy : this;
	}

	public boolean isProxySet(){
		return this.proxy != null;
	}

	public void setProxy(ConnectionImpl proxy) {
		this.proxy = proxy;
	}

	// We have to proxy ourselves when we're load balanced so that
	// statements get routed to the right physical connection
	// (when load balanced, we're a "logical" connection)
	private Connection getProxy() {
		return (proxy != null) ? proxy : (Connection) this;
	}
	
	/**
	 * Creates a connection to a MySQL Server.
	 * 
	 * @param hostToConnectTo
	 *            the hostname of the database server
	 * @param portToConnectTo
	 *            the port number the server is listening on
	 * @param info
	 *            a Properties[] list holding the user and password
	 * @param databaseToConnectTo
	 *            the database to connect to
	 * @param url
	 *            the URL of the connection
	 * @param d
	 *            the Driver instantation of the connection
	 * @exception SQLException
	 *                if a database access error occurs
	 */
	protected ConnectionImpl(String hostToConnectTo, int portToConnectTo, Properties info,
			String databaseToConnectTo, String url)
			throws SQLException {
	
		this.connectionCreationTimeMillis = System.currentTimeMillis();
		
		if (databaseToConnectTo == null) {
			databaseToConnectTo = "";
		}

		// Stash away for later, used to clone this connection for Statement.cancel
		// and Statement.setQueryTimeout().
		//
		
		this.origHostToConnectTo = hostToConnectTo;
		this.origPortToConnectTo = portToConnectTo;
		this.origDatabaseToConnectTo = databaseToConnectTo;

		
		this.sessionCalendar = new GregorianCalendar();
		this.utcCalendar = new GregorianCalendar();
		this.utcCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// We store this per-connection, due to static synchronization
		// issues in Java's built-in TimeZone class...
//		this.defaultTimeZone = Util.getDefaultTimeZone();
//		
//		if ("GMT".equalsIgnoreCase(this.defaultTimeZone.getID())) {
//			this.isClientTzUTC = true;
//		} else {
//			this.isClientTzUTC = false;
//		}

		this.openStatements = new HashMap<Statement, Statement>();
		
		if (Driver.isHostPropertiesList(hostToConnectTo)) {
			Properties hostSpecificProps = Driver.expandHostKeyValues(hostToConnectTo);
			
			Enumeration<?> propertyNames = hostSpecificProps.propertyNames();
			
			while (propertyNames.hasMoreElements()) {
				String propertyName = propertyNames.nextElement().toString();
				String propertyValue = hostSpecificProps.getProperty(propertyName);
				
				info.setProperty(propertyName, propertyValue);
			}
		} else {
		
			if (hostToConnectTo == null) {
				this.host = Ah3DriverConstant.DB_DEFAULT_HOST;
				this.hostPortPair = this.host + ":" + portToConnectTo;
			} else {
				this.host = hostToConnectTo;
				
				if (hostToConnectTo.indexOf(":") == -1) {
					this.hostPortPair = this.host + ":" + portToConnectTo;
				} else {
					this.hostPortPair = this.host;
				}
			}
		}

		this.port = portToConnectTo;

		this.database = databaseToConnectTo;
		this.myURL = url;
		this.user = info.getProperty(Driver.USER_PROPERTY_KEY);
		this.password = info
				.getProperty(Driver.PASSWORD_PROPERTY_KEY);

		if ((this.user == null) || this.user.equals("")) {
			this.user = "";
		}

		if (this.password == null) {
			this.password = "";
		}

		this.props = info;
		
		
//		鍒濆鍖栭厤缃枃浠讹紝闇�鐮旂┒涓�笅
//		initializeDriverProperties(info);

		
		try {
//			this.dbmd = getMetaData(false, false);
//			initializeSafeStatementInterceptors();
			//閾炬帴鏁版嵁搴�
			ah3Io = new Ah3IO(hostToConnectTo,portToConnectTo,user,password,info);
//			ah3Io = new Ah3IOImplByC(hostToConnectTo,portToConnectTo,user,password,info);
			this.isClosed = false;
//			createNewIO(false);
//			unSafeStatementInterceptors();
		} catch (SQLException ex) {
			cleanup(ex);
			// don't clobber SQL exceptions
			throw ex;
		} catch (Exception ex) {
			cleanup(ex);
			StringBuffer mesg = new StringBuffer(128);
			mesg.append("Unable to connect to AH3 server:"+this.host + ":" + this.port+ ".");
			SQLException sqlEx = SQLError.createSQLException(mesg.toString(),
					SQLError.SQL_STATE_COMMUNICATION_LINK_FAILURE, getExceptionInterceptor());

//			SQLException sqlEx = new SQLException("Unable to connect to AH3 server:"+this.host + ":" + this.port+ ".","connect failure");
			sqlEx.initCause(ex);
			throw sqlEx;
			
		}
		
		this.proxy = this;
		
		Driver.trackConnection(this);
	}
	
	/**
	 * Creates a connection instance -- We need to provide factory-style methods
	 * so we can support both JDBC3 (and older) and JDBC4 runtimes, otherwise
	 * the class verifier complains when it tries to load JDBC4-only interface
	 * classes that are present in JDBC4 method signatures.
	 */

	protected static Connection getInstance(String hostToConnectTo,
			int portToConnectTo, Properties info, String databaseToConnectTo,
			String url) throws SQLException {
		
			return new ConnectionImpl(hostToConnectTo, portToConnectTo, info,
					databaseToConnectTo, url);

	}

	/**
	 * Register a Statement instance as open.
	 * 
	 * @param stmt
	 *            the Statement instance to remove
	 */
	public void registerStatement(Statement stmt) {
		synchronized (this.openStatements) {
			this.openStatements.put(stmt, stmt);
		}
	}
	
	public void clearWarnings() throws SQLException {
		// TODO Auto-generated method stub

	}

	public void close() throws SQLException {
		// TODO Auto-generated method stub
		synchronized (getConnectionMutex()) {
			realClose(true, true, false, null);
		}

	}

	/**
	 * Closes connection and frees resources.
	 * 
	 * @param calledExplicitly
	 *            is this being called from close()
	 * @param issueRollback
	 *            should a rollback() be issued?
	 * @throws SQLException
	 *             if an error occurs
	 */
	public void realClose(boolean calledExplicitly, boolean issueRollback,
			boolean skipLocalTeardown, Throwable reason) throws SQLException {
		SQLException sqlEx = null;

		if (this.isClosed()) {
			return;
		}
		
		this.forceClosedReason = reason;
		
		try {
			if (!skipLocalTeardown) {
				if (!getAutoCommit() && issueRollback) {
					try {
						rollback();
					} catch (SQLException ex) {
						sqlEx = ex;
					}
				}

//				reportMetrics();

//				if (getUseUsageAdvisor()) {
//					if (!calledExplicitly) {
//						String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
//
//						this.eventSink.consumeEvent(new ProfilerEvent(
//								ProfilerEvent.TYPE_WARN, "", //$NON-NLS-1$
//								this.getCatalog(), this.getId(), -1, -1, System
//										.currentTimeMillis(), 0, Constants.MILLIS_I18N,
//										null,
//								this.pointOfOrigin, message));
//					}
//
//					long connectionLifeTime = System.currentTimeMillis()
//							- this.connectionCreationTimeMillis;
//
//					if (connectionLifeTime < 500) {
//						String message = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
//
//						this.eventSink.consumeEvent(new ProfilerEvent(
//								ProfilerEvent.TYPE_WARN, "", //$NON-NLS-1$
//								this.getCatalog(), this.getId(), -1, -1, System
//										.currentTimeMillis(), 0, Constants.MILLIS_I18N,
//										null,
//								this.pointOfOrigin, message));
//					}
//				}

				try {
					closeAllOpenStatements();
				} catch (SQLException ex) {
					sqlEx = ex;
				}

				if (this.ah3Io != null) {
					try {
						this.ah3Io.quit();
					} catch (Exception e) {
						;
					}

				}
			} else {			
				this.ah3Io.forceClose();
			}
			
			// stament鍜宔xception鐨処nterceptor 鍚庣画鑰冭檻
			/*
	    	if (this.statementInterceptors != null) {
	    		for (int i = 0; i < this.statementInterceptors.size(); i++) {
	    			this.statementInterceptors.get(i).destroy();
	    		}
	    	}
	    	
	    	if (this.exceptionInterceptor != null) {
	    		this.exceptionInterceptor.destroy();
	    	}
	    	*/
		} finally {
			this.openStatements = null;
			this.ah3Io = null;
			/*
			this.statementInterceptors = null;
			this.exceptionInterceptor = null;
			ProfilerEventHandlerFactory.removeInstance(this);
			synchronized (getConnectionMutex()) {
				if (this.cancelTimer != null) {
					this.cancelTimer.cancel();
				}
			}
			*/			
			this.isClosed = true;
		}

		if (sqlEx != null) {
			throw sqlEx;
		}

	}
	
	public void commit() throws SQLException {
		// TODO Auto-generated method stub

	}

	public Statement createStatement() throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		return createStatement(DEFAULT_RESULT_SET_TYPE,
					DEFAULT_RESULT_SET_CONCURRENCY);
	}

	public Statement createStatement(int resultSetType, int resultSetConcurrency)
			throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		checkClosed();

		StatementImpl stmt = new StatementImpl(this.proxy, this.database);
//		stmt.setResultSetType(resultSetType);
//		stmt.setResultSetConcurrency(DEFAULT_RESULT_SET_CONCURRENCY);

		return stmt;
		
//		return null;
		
	}

	public Statement createStatement(int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		//涓嶆敮鎸丷esultSet.HOLD_CURSORS_OVER_COMMIT(涓嶅叧闂� ,鍙湁ResultSet.CLOSE_CURSORS_AT_COMMIT
		return createStatement(resultSetType, resultSetConcurrency);
	}

	public boolean getAutoCommit() throws SQLException {
		// TODO Auto-generated method stub
		return false;
	}

	public String getCatalog() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getHoldability() throws SQLException {
		// TODO Auto-generated method stub
		return java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
//		return 0;
	}
	//寰呭鍔�
	public DatabaseMetaData getMetaData() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	// 涓嶆敮鎸佷簨鍔�
	public int getTransactionIsolation() throws SQLException {
		// TODO Auto-generated method stub
		return java.sql.Connection.TRANSACTION_NONE;
//		return 0;
	}

	public Map<String, Class<?>> getTypeMap() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public SQLWarning getWarnings() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isClosed() throws SQLException {
		// TODO Auto-generated method stub
		return this.isClosed;
//		return false;
	}

	public boolean isReadOnly() throws SQLException {
		// TODO Auto-generated method stub
		return true;
	}

	public String nativeSQL(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return sql;
	}
	//瀛樺偍杩囩▼ 涓嶆敮鎸�
	public CallableStatement prepareCall(String sql) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		// TODO Auto-generated method stub
		//瀛樺偍杩囩▼ 涓嶆敮鎸�
		return null;
	}

	public CallableStatement prepareCall(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
		//瀛樺偍杩囩▼ 涓嶆敮鎸�
		return null;
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		return prepareStatement(sql, DEFAULT_RESULT_SET_TYPE,
				DEFAULT_RESULT_SET_CONCURRENCY);
	}

	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
			throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		java.sql.PreparedStatement pStmt = prepareStatement(sql);
		//涓嶆敮鎸佽嚜鍔ㄧ敓鎴愰敭
		return pStmt;
	}

	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
			throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		java.sql.PreparedStatement pStmt = prepareStatement(sql);
		//涓嶆敮鎸佽嚜鍔ㄧ敓鎴愰敭
		return pStmt;
	}

	public PreparedStatement prepareStatement(String sql, String[] columnNames)
			throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		java.sql.PreparedStatement pStmt = prepareStatement(sql);
		//涓嶆敮鎸佽嚜鍔ㄧ敓鎴愰敭
		return pStmt;
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency) throws SQLException {
		// TODO Auto-generated method stub
//		return null;
		synchronized (getConnectionMutex()) {
			checkClosed();
	
			//
			// FIXME: Create warnings if can't create results of the given
			// type or concurrency
			//
			PreparedStatement pStmt = null;
			
			String nativeSql = nativeSQL(sql);
			
			pStmt = (PreparedStatement) clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
			
			return pStmt;
		}
	}

	public PreparedStatement prepareStatement(String sql, int resultSetType,
			int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸丷esultSet.HOLD_CURSORS_OVER_COMMIT(涓嶅叧闂� ,鍙湁ResultSet.CLOSE_CURSORS_AT_COMMIT
		return prepareStatement(sql, resultSetType, resultSetConcurrency);
//		return null;
	}
	
	public java.sql.PreparedStatement clientPrepareStatement(String sql,
			int resultSetType, int resultSetConcurrency, 
			boolean processEscapeCodesIfNeeded) throws SQLException {
		checkClosed();

		String nativeSql = nativeSQL(sql);
		
		PreparedStatement pStmt = null;

//		pStmt = StatementImpl.getInstance(getProxy(), nativeSql,
//					this.database);
//
//		pStmt.setResultSetType(resultSetType);
//		pStmt.setResultSetConcurrency(DEFAULT_RESULT_SET_CONCURRENCY);

		return pStmt;
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
	}

	public void rollback() throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸佸洖婊�
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸佸洖婊�
	}

	public void setAutoCommit(boolean autoCommit) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸佽缃�
		this.autoCommit = true;
	}

	public void setCatalog(String catalog) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
	}

	public void setHoldability(int holdability) throws SQLException {
		// TODO Auto-generated method stub
		// 涓嶆敮鎸�return java.sql.ResultSet.CLOSE_CURSORS_AT_COMMIT;
	}

	public void setReadOnly(boolean readOnly) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
	}

	public Savepoint setSavepoint() throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
		return null;
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
		return null;
	}

	public void setTransactionIsolation(int level) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸佷簨鍔�
	}

	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
		// TODO Auto-generated method stub
		//涓嶆敮鎸�
	}
	
	public void checkClosed() throws SQLException {
		if (this.isClosed || this.ah3Io.isClosed()) {
			throwConnectionClosedException();
		}
	}
	
	public void throwConnectionClosedException() throws SQLException {
		StringBuffer messageBuf = new StringBuffer(
				"No operations allowed after connection closed.");
		
		SQLException ex = SQLError.createSQLException(messageBuf.toString(),
				SQLError.SQL_STATE_CONNECTION_NOT_OPEN, getExceptionInterceptor());
		
		if (this.forceClosedReason != null) {
			ex.initCause(this.forceClosedReason);
		}
		throw ex;		
	}
	
	/**
	 * Returns the locally mapped instance of a charset converter (to avoid
	 * overhead of static synchronization).
	 * 
	 * @param javaEncodingName
	 *            the encoding name to retrieve
	 * @return a character converter, or null if one couldn't be mapped.
	 */
	//缂栫爜杞崲========鏄惁鏈夊繀瑕�
	/*
	public SingleByteCharsetConverter getCharsetConverter(
			String javaEncodingName) throws SQLException {
		if (javaEncodingName == null) {
			return null;
		}

		if (this.usePlatformCharsetConverters) {
			return null; // we'll use Java's built-in routines for this
			             // they're finally fast enough
		}
		
		SingleByteCharsetConverter converter = null;
		
		synchronized (this.charsetConverterMap) {
			Object asObject = this.charsetConverterMap
			.get(javaEncodingName);

			if (asObject == CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
				return null;
			}
			
			converter = (SingleByteCharsetConverter)asObject;
			
			if (converter == null) {
				try {
					converter = SingleByteCharsetConverter.getInstance(
							javaEncodingName, this);

					if (converter == null) {
						this.charsetConverterMap.put(javaEncodingName,
								CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
					} else {
						this.charsetConverterMap.put(javaEncodingName, converter);
					}
				} catch (UnsupportedEncodingException unsupEncEx) {
					this.charsetConverterMap.put(javaEncodingName,
							CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);

					converter = null;
				}
			}
		}

		return converter;
	}
	*/
	/**
	 * Initializes driver properties that come from URL or properties passed to
	 * the driver manager.
	 * 
	 * @param info
	 *            DOCUMENT ME!
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	private void initializeDriverProperties(Properties info)
			throws SQLException {
		initializeProperties(info);
		/*
		String exceptionInterceptorClasses = getExceptionInterceptors();
		
		if (exceptionInterceptorClasses != null && !"".equals(exceptionInterceptorClasses)) {
			this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses);
			this.exceptionInterceptor.init(this, info);
		}
		
		this.usePlatformCharsetConverters = getUseJvmCharsetConverters();

		this.log = LogFactory.getLogger(getLogger(), LOGGER_INSTANCE_NAME, getExceptionInterceptor());

		if (getProfileSql() || getUseUsageAdvisor()) {
			this.eventSink = ProfilerEventHandlerFactory.getInstance(getLoadBalanceSafeProxy());
		}

		if (getCachePreparedStatements()) {
			createPreparedStatementCaches();		
		}

		if (getNoDatetimeStringSync() && getUseTimezone()) {
			throw SQLError.createSQLException(
					"Can't enable noDatetimeStringSync and useTimezone configuration "
							+ "properties at the same time",
					SQLError.SQL_STATE_INVALID_CONNECTION_ATTRIBUTE, getExceptionInterceptor());
		}
		
		if (getCacheCallableStatements()) {
			this.parsedCallableStatementCache = new LRUCache(
					getCallableStatementCacheSize());
		}
		
		if (getAllowMultiQueries()) {
			setCacheResultSetMetadata(false); // we don't handle this yet
		}
		
		if (getCacheResultSetMetadata()) {
			this.resultSetMetadataCache = new LRUCache(
					getMetadataCacheSize());
		}
		*/
	}
	/**
	 * Initializes driver properties that come from URL or properties passed to
	 * the driver manager.
	 * 
	 * @param info
	 *            DOCUMENT ME!
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	protected void initializeProperties(Properties info) throws SQLException {
		if (info != null) {
			// For backwards-compatibility
			String profileSqlLc = info.getProperty("profileSql"); //$NON-NLS-1$

			if (profileSqlLc != null) {
				info.put("profileSQL", profileSqlLc); //$NON-NLS-1$
			}

			Properties infoCopy = (Properties) info.clone();

			infoCopy.remove(Driver.HOST_PROPERTY_KEY);
			infoCopy.remove(Driver.USER_PROPERTY_KEY);
			infoCopy.remove(Driver.PASSWORD_PROPERTY_KEY);
			infoCopy.remove(Driver.DBNAME_PROPERTY_KEY);
			infoCopy.remove(Driver.PORT_PROPERTY_KEY);
			infoCopy.remove("profileSql"); //$NON-NLS-1$
			
			/*
			int numPropertiesToSet = PROPERTY_LIST.size();

			for (int i = 0; i < numPropertiesToSet; i++) {
				java.lang.reflect.Field propertyField = PROPERTY_LIST
						.get(i);

				try {
					ConnectionProperty propToSet = (ConnectionProperty) propertyField
							.get(this);

					propToSet.initializeFrom(infoCopy);
				} catch (IllegalAccessException iae) {
					throw SQLError.createSQLException(
							Messages.getString("ConnectionProperties.unableToInitDriverProperties") //$NON-NLS-1$
									+ iae.toString(),
							SQLError.SQL_STATE_GENERAL_ERROR, getExceptionInterceptor());
				}
			}
			*/
//			postInitialization();
		}
	}

	/*
	protected void postInitialization() throws SQLException {
	
		// Support 'old' profileSql capitalization
		if (this.profileSql.getValueAsObject() != null) {
			this.profileSQL.initializeFrom(this.profileSql.getValueAsObject()
					.toString());
		}

		this.reconnectTxAtEndAsBoolean = ((Boolean) this.reconnectAtTxEnd
				.getValueAsObject()).booleanValue();

		// Adjust max rows
		if (this.getMaxRows() == 0) {
			// adjust so that it will become MysqlDefs.MAX_ROWS
			// in execSQL()
			this.maxRows.setValueAsObject(Integer.valueOf(-1));
		}

		//
		// Check character encoding
		//
		String testEncoding = this.getEncoding();

		if (testEncoding != null) {
			// Attempt to use the encoding, and bail out if it
			// can't be used
			try {
				String testString = "abc"; //$NON-NLS-1$
				StringUtils.getBytes(testString, testEncoding);
			} catch (UnsupportedEncodingException UE) {
				throw SQLError.createSQLException(Messages.getString(
						"ConnectionProperties.unsupportedCharacterEncoding", 
						new Object[] {testEncoding}), "0S100", getExceptionInterceptor()); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// Metadata caching is only supported on JDK-1.4 and newer
		// because it relies on LinkedHashMap being present.
		// Check (and disable) if not supported
		if (((Boolean) this.cacheResultSetMetadata.getValueAsObject())
				.booleanValue()) {
			try {
				Class.forName("java.util.LinkedHashMap"); //$NON-NLS-1$
			} catch (ClassNotFoundException cnfe) {
				this.cacheResultSetMetadata.setValue(false);
			}
		}

		this.cacheResultSetMetaDataAsBoolean = this.cacheResultSetMetadata
				.getValueAsBoolean();
		this.useUnicodeAsBoolean = this.useUnicode.getValueAsBoolean();
		this.characterEncodingAsString = ((String) this.characterEncoding
				.getValueAsObject());
		this.characterEncodingIsAliasForSjis = CharsetMapping.isAliasForSjis(this.characterEncodingAsString);
		this.highAvailabilityAsBoolean = this.autoReconnect.getValueAsBoolean();
		this.autoReconnectForPoolsAsBoolean = this.autoReconnectForPools
				.getValueAsBoolean();
		this.maxRowsAsInt = ((Integer) this.maxRows.getValueAsObject())
				.intValue();
		this.profileSQLAsBoolean = this.profileSQL.getValueAsBoolean();
		this.useUsageAdvisorAsBoolean = this.useUsageAdvisor
				.getValueAsBoolean();
		this.useOldUTF8BehaviorAsBoolean = this.useOldUTF8Behavior
				.getValueAsBoolean();
		this.autoGenerateTestcaseScriptAsBoolean = this.autoGenerateTestcaseScript
				.getValueAsBoolean();
		this.maintainTimeStatsAsBoolean = this.maintainTimeStats
				.getValueAsBoolean();
		this.jdbcCompliantTruncationForReads = getJdbcCompliantTruncation();
		
		if (getUseCursorFetch()) {
			// assume they want to use server-side prepared statements
			// because they're required for this functionality
			setDetectServerPreparedStmts(true);
		}
	}
	*/
	/**
	 * Closes all currently open statements.
	 * 
	 * @throws SQLException
	 *             DOCUMENT ME!
	 */
	private void closeAllOpenStatements() throws SQLException {
		SQLException postponedException = null;

		if (this.openStatements != null) {
			List<Statement> currentlyOpenStatements = new ArrayList<Statement>(); // we need this to
			// avoid
			// ConcurrentModificationEx

			for (Iterator<Statement> iter = this.openStatements.keySet().iterator(); iter.hasNext();) {
				currentlyOpenStatements.add(iter.next());
			}

			int numStmts = currentlyOpenStatements.size();

			for (int i = 0; i < numStmts; i++) {
				StatementImpl stmt = (StatementImpl) currentlyOpenStatements.get(i);

				try {
					stmt.realClose(false, true);
				} catch (SQLException sqlEx) {
					postponedException = sqlEx; // throw it later, cleanup all
					// statements first
				}
			}

			if (postponedException != null) {
				throw postponedException;
			}
		}
	}

	private void cleanup(Throwable whyCleanedUp) {
		try {
			if ((this.ah3Io != null) && !isClosed()) {
				realClose(false, false, false, whyCleanedUp);
			} else if (this.ah3Io != null) {
				this.ah3Io.forceClose();
			}
		} catch (SQLException sqlEx) {
			// ignore, we're going away.
			;
		}

		this.isClosed = true;
	}
	
	private void closeStatement(java.sql.Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException sqlEx) {
				; // ignore
			}

			stmt = null;
		}
	}
	
	@Override
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public Blob createBlob() throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public Clob createClob() throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public NClob createNClob() throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public SQLXML createSQLXML() throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public Properties getClientInfo() throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public String getClientInfo(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public boolean isValid(int arg0) throws SQLException {
		// TODO Auto-generated method stub
		throw new SQLException("not implemented yet");
	}

	@Override
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		// TODO Auto-generated method stub
		throw new SQLClientInfoException("not implemented yet",null);
	}

	@Override
	public void setClientInfo(String arg0, String arg1)
			throws SQLClientInfoException {
		// TODO Auto-generated method stub
		//throw new SQLClientInfoException("not implemented yet");
	}
	
	protected void executeSql(String sql) throws SQLException {
		try {
			if (this.ah3Io.checkAndReConnect()) {			
				this.ah3Io.sendExecuteSQLPackToServer(sql);
			}
		} catch (SQLException se) { 
			throw se;
		} catch (Exception e) {
			throw new SQLException(e);
		}
	}
	
	protected Iah3IO getConnectionIO() {
		return this.ah3Io;
	}
}
