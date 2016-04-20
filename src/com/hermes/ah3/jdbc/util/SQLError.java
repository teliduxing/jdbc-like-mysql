/*
 Copyright (c) 2002, 2013, Oracle and/or its affiliates. All rights reserved.
 

  The MySQL Connector/J is licensed under the terms of the GPLv2
  <http://www.gnu.org/licenses/old-licenses/gpl-2.0.html>, like most MySQL Connectors.
  There are special exceptions to the terms and conditions of the GPLv2 as it is applied to
  this software, see the FLOSS License Exception
  <http://www.mysql.com/about/legal/licensing/foss-exception.html>.

  This program is free software; you can redistribute it and/or modify it under the terms
  of the GNU General Public License as published by the Free Software Foundation; version 2
  of the License.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this
  program; if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth
  Floor, Boston, MA 02110-1301  USA



 */
package com.hermes.ah3.jdbc.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.BindException;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;


/**
 * SQLError is a utility class that maps MySQL error codes to X/Open error codes
 * as is required by the JDBC spec.
 * 
 * @author Mark Matthews <mmatthew_at_worldserver.com>
 * @version $Id: SQLError.java 5122 2006-04-03 15:37:11 +0000 (Mon, 03 Apr 2006)
 *          mmatthews $
 */
public class SQLError {
	
	public static final String SQL_STATE_CONNECTION_NOT_OPEN = "08003"; //$NON-NLS-1$
	public static final String SQL_STATE_ILLEGAL_ARGUMENT = "S1009"; //$NON-NLS-1$
	public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01"; //$NON-NLS-1$

	private static Method THROWABLE_INIT_CAUSE_METHOD;
	
	static {

		try {
			THROWABLE_INIT_CAUSE_METHOD = Throwable.class.getMethod("initCause", new Class[] {Throwable.class});
		} catch (Throwable t) {
			// we're not on a VM that has it
			THROWABLE_INIT_CAUSE_METHOD = null;
		}
	}
	/*
	 * SQL State Class SQLNonTransientException Subclass 08
	 * SQLNonTransientConnectionException 22 SQLDataException 23
	 * SQLIntegrityConstraintViolationException N/A
	 * SQLInvalidAuthorizationException 42 SQLSyntaxErrorException
	 * 
	 * SQL State Class SQLTransientException Subclass 08
	 * SQLTransientConnectionException 40 SQLTransactionRollbackException N/A
	 * SQLTimeoutException
	 */

	public static SQLException createSQLException(String message,
			String sqlState, ExceptionInterceptor interceptor) {
		return createSQLException(message, sqlState, 0, interceptor);
	}

	public static SQLException createSQLException(String message, ExceptionInterceptor interceptor) {
		return createSQLException(message, interceptor, null);
	}
	public static SQLException createSQLException(String message, ExceptionInterceptor interceptor, Connection conn) {
		SQLException sqlEx = new SQLException(message);
		
		if (interceptor != null) {
			SQLException interceptedEx = interceptor.interceptException(sqlEx, conn);
			
			if (interceptedEx != null) {
				return interceptedEx;
			}
		}
		
		return sqlEx;
	}

	public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor) {
		return createSQLException(message, sqlState, cause, interceptor, null);
	}
	public static SQLException createSQLException(String message, String sqlState, Throwable cause, ExceptionInterceptor interceptor,
			Connection conn) {
		if (THROWABLE_INIT_CAUSE_METHOD == null) {
			if (cause != null) {
				message = message + " due to " + cause.toString();
			}
		}
		
		SQLException sqlEx = createSQLException(message, sqlState, interceptor);
		
		if (cause != null && THROWABLE_INIT_CAUSE_METHOD != null) {
			try {
				THROWABLE_INIT_CAUSE_METHOD.invoke(sqlEx, new Object[] {cause});
			} catch (Throwable t) {
				// we're not going to muck with that here, since it's
				// an error condition anyway!
			}
		}
		
		if (interceptor != null) {
			SQLException interceptedEx = interceptor.interceptException(sqlEx, conn);
			
			if (interceptedEx != null) {
				return interceptedEx;
			}
		}
		
		return sqlEx;
	}
	
	public static SQLException createSQLException(String message,
			String sqlState, int vendorErrorCode, ExceptionInterceptor interceptor) {
		return createSQLException(message, sqlState, vendorErrorCode, false, interceptor);
	}
	
	/**
	 * 
	 * @param message
	 * @param sqlState
	 * @param vendorErrorCode
	 * @param isTransient
	 * @param interceptor
	 * @return
	 */
	public static SQLException createSQLException(String message,
			String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor) {
		return createSQLException(message, sqlState, vendorErrorCode, false, interceptor, null);
	}
	
	public static SQLException createSQLException(String message,
			String sqlState, int vendorErrorCode, boolean isTransient, ExceptionInterceptor interceptor, Connection conn) {

		SQLException sqlEx = null;
			
		if (sqlState != null) {
			sqlEx = new SQLException(message, sqlState, vendorErrorCode);
		}
			
		if (interceptor != null) {
			SQLException interceptedEx = interceptor.interceptException(sqlEx, conn);
			
			if (interceptedEx != null) {
				return interceptedEx;
			}
		}
			
		return sqlEx;

	}
	

}
