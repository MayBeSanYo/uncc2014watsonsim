package uncc2014watsonsim;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Database {
	private Connection conn;
	
	public Database() {
		try {
			Class.forName("org.sqlite.JDBC");
		    Properties props = new Properties();
		    props.put("busy_timeout", "30000");
			conn = DriverManager.getConnection("jdbc:sqlite:data/watsonsim.db", props);
			conn.createStatement().execute("PRAGMA journal_mode = WAL;");
			conn.createStatement().execute("PRAGMA synchronous = OFF;");
			// JDBC's SQLite uses autocommit (So commit() is redundant)
			// Furthermore, close() is a no-op as long as the results are commit()'d

			if (!sanityCheck()) {
				System.out.println(String.format("Warning: Database missing or malformed."));
			}
		} catch (SQLException | ClassNotFoundException e2) {
			e2.printStackTrace();
			throw new RuntimeException("Can't run without a database.");
		}
	}
	
	/** Simple wrapper for creating an SQL statement */
	public PreparedStatement prep(String sql) {
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ps.setFetchSize(100);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Can't prepare an SQL statement \"" + sql + "\"");
		}
		return ps;
	}
	
	/** Check that the SQLite DB we opened contains the right tables
	 * You would do this rather than check if the file exists because SQLite
	 * creates the file implicitly and it simply has no contents. 
	 * */
	public boolean sanityCheck() {
		Set<String> existent_tables = new HashSet<String>();
		try {
			ResultSet sql = prep("select tbl_name from sqlite_master;").executeQuery();
			while (sql.next()) {
				existent_tables.add(sql.getString("tbl_name"));
			}
		} catch (SQLException e) {
			// There was a problem executing the query
			return false;
		}

		return existent_tables.containsAll(Arrays.asList(new String[]{
				"meta", "content", "redirects", "questions", "results", "cache"
		}));
	}

}