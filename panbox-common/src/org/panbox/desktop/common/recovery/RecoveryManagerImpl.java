package org.panbox.desktop.common.recovery;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.panbox.Settings;
import org.panbox.core.devicemgmt.DeviceType;
import org.panbox.desktop.common.devicemgmt.DeviceManagerException;
import org.panbox.desktop.common.gui.devices.PanboxDevice;

public class RecoveryManagerImpl implements IRecoveryManager {

	private static final Logger logger = Logger.getLogger("org.panbox");

	private final String RECOVERYDB = Settings.getInstance().getRecoveryDBPath();
	private final String RECOVERYDB_CONNECT_STRING = "jdbc:sqlite:" + RECOVERYDB;
	private static final String TABLE_RECOVERY = "recovery";

	private static final String CREATE_TABLES = "create table " + TABLE_RECOVERY
			+ " (id INTEGER PRIMARY KEY AUTOINCREMENT, filepath string)";
	private static final String DROP_TABLES = "drop table if exists " + TABLE_RECOVERY;
	private static final String QUERY_TABLES = "SELECT name FROM sqlite_master WHERE type='table' AND name='"
			+ TABLE_RECOVERY + "';";
	private static final String QUERY_FILEPATH = "SELECT * FROM "
			+ TABLE_RECOVERY + " WHERE filepath=?;";
	private static final String INSERT_FILEPATH = "insert into " + TABLE_RECOVERY
			+ " VALUES (NULL, (?))";

	private static IRecoveryManager instance = null;

	private Connection connection = null;

	public class RecoveryException extends Exception {
		private static final long serialVersionUID = 4536504133400851910L;

		public RecoveryException() {
			super();
			// TODO Auto-generated constructor stub
		}

		public RecoveryException(String arg0, Throwable arg1, boolean arg2, boolean arg3) {
			super(arg0, arg1, arg2, arg3);
			// TODO Auto-generated constructor stub
		}

		public RecoveryException(String arg0, Throwable arg1) {
			super(arg0, arg1);
			// TODO Auto-generated constructor stub
		}

		public RecoveryException(String arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

		public RecoveryException(Throwable arg0) {
			super(arg0);
			// TODO Auto-generated constructor stub
		}

	}

	private RecoveryManagerImpl() throws RecoveryException {

		// create a database connection
		try {
			connection = DriverManager.getConnection(RECOVERYDB_CONNECT_STRING);
		} catch (SQLException ex) {
			throw new RecoveryException("Could not get connection for SQL DB: " + RECOVERYDB_CONNECT_STRING, ex);
		}

		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery(QUERY_TABLES);
			if (!rs.next()) {
				logger.debug("RecoveryUtils database did not exist. Creating a new one now...");
				createTables(s);
			} else {
				logger.debug("RecoveryManager database exists. Will use that one...");
			}
		} catch (SQLException ex) {
			throw new RecoveryException("Failed to run SQL command init: ", ex);
		}

	}

	public static IRecoveryManager getInstance() throws RecoveryException {
		if (instance == null) {
			instance = new RecoveryManagerImpl();
		}
		return instance;
	}

	private void createTables(Statement statement) throws RecoveryException {
		logger.debug("RecoveryManager : createTables");
		try {
			statement.executeUpdate(DROP_TABLES);
			statement.executeUpdate(CREATE_TABLES);
		} catch (SQLException ex) {
			throw new RecoveryException("Failed to run SQL command during createTables: ", ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.panbox.desktop.common.recovery.IRecoveryManager#isFileAlreadyMarkedAsMissing(java.lang.String)
	 */
	@Override
	public boolean isFileAlreadyMarkedAsMissing(String filePath) throws RecoveryException {
		logger.debug("RecoveryManager : isFileAlreadyMarkedAsMissing(" + filePath + ")");

		try {
			PreparedStatement pStmt = connection
					.prepareStatement(QUERY_FILEPATH);
			pStmt.setQueryTimeout(30); // set timeout to 30 sec.
			pStmt.setString(1, filePath);
			ResultSet rs = pStmt.executeQuery();

			if (rs.next()) {
				return true;
			}
			pStmt.close();
		} catch (SQLException ex) {
			throw new RecoveryException("Failed to run isFileAlreadyMarkedAsMissing: ", ex);
		}
		
		logger.debug("RecoveryManager : isFileAlreadyMarkedAsMissing : File not listed.");
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.panbox.desktop.common.recovery.IRecoveryManager#markMissingIVForFile(java.lang.String)
	 */
	@Override
	public void markMissingIVForFile(String filePath) throws RecoveryException {
		if( !isFileAlreadyMarkedAsMissing(filePath) ) {
			PreparedStatement pStatement = null;
			try {
				pStatement = connection.prepareStatement(INSERT_FILEPATH);
				pStatement.setString(1, filePath);
				pStatement.execute();
			} catch (SQLException e) {
				throw new RecoveryException("Failed to run markMissingIVForFile: ", e);
			} finally {
				try {
					if( pStatement != null ) {
						pStatement.close();
					}
				} catch (SQLException e) {
					throw new RecoveryException("Failed to run markMissingIVForFile (close): ", e);
				}
			}
		}
	}
}
