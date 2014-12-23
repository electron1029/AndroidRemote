package sr.design.tvremote;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class cmdDataBase
{
	//Commands. Not sure if we need globals like these, or just
	//type out strings every time:
	
	public static final String POWER_ON = "power on";
	public static final String POWER_OFF = "power off";
	public static final String VOLUME_UP = "volume up";
	public static final String VOLUME_DOWN = "volume down";
	
	
	public static final String KEY_ROWID = "rowid";
	public static final String KEY_CMDNAME = "cmdname";
	public static final String KEY_IRCODE = "ir";
	public static final String KEY_REMOTE_NAME = "remote_name";
	
	static final String DATABASE_NAME = "cmdStorage";
	static final String DATABASE_TABLE = "commandsTable";
	
	static final String[] columns = new String[] { KEY_ROWID, KEY_REMOTE_NAME, KEY_CMDNAME, KEY_IRCODE};
	private static final int DATABASE_VERSION = 1;

	private SQLHandler sqlHandler;
	private final Context _context;
	private SQLiteDatabase _database;
	
	
	private static class SQLHandler extends SQLiteOpenHelper
	{
		
		public SQLHandler(Context context) 
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**Called when database is first created: **/
		@Override
		public void onCreate(SQLiteDatabase db) 
		{
			db.execSQL("CREATE VIRTUAL TABLE " + DATABASE_TABLE + " USING fts3(" + 
					KEY_REMOTE_NAME + " TEXT NOT NULL, " +
					KEY_CMDNAME + " TEXT NOT NULL, " 
					+ KEY_IRCODE + " TEXT NOT NULL);");
		}

		/** If database exists, this method will be called: **/
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
		{
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
		
	}
	
	/** Database constructor **/
	public cmdDataBase(Context c)
	{
		_context = c;
	}
	
	/** Adds a new command to the database.
	 * If the command was already in database, updates
	 * the encoding
	 */
	public long addCommand(String remote_name, String name, String encoding)
	{
		ContentValues cv = new ContentValues();
		cv.put(KEY_REMOTE_NAME, remote_name);
		cv.put(KEY_CMDNAME, name);
		cv.put(KEY_IRCODE, encoding);
		
		Cursor c = findCommand(remote_name, name);
		c.moveToFirst();
		if (!c.isAfterLast())
			return _database.update(DATABASE_TABLE, cv, KEY_CMDNAME + " MATCH '" + name + "'", null);
		else
			return _database.insert(DATABASE_TABLE, null, cv);
	}
	
	/** A test method for printing inputed data.
	 * Faster than the saving to file method.
	 */
	public void printData()
	{
		Cursor c = _database.query(DATABASE_TABLE, columns, null, null, null, null, KEY_CMDNAME + " ASC");

		int iRemoteName = c.getColumnIndex(KEY_REMOTE_NAME);
		int iName = c.getColumnIndex(KEY_CMDNAME);
		int iCode = c.getColumnIndex(KEY_IRCODE);
		for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext())
		{
			Log.d("Database Data", 
					"Remote Name: " + c.getString(iRemoteName) +
					" Name: " + c.getString(iName) + 
					" code: " + c.getString(iCode));
		}
	}
	
	/** Returns the encoding of the specified command **/
	public String getEncoding(String remote_name, String cmdName)
	{
		Cursor c = findCommand(remote_name, cmdName);
		int ir = c.getColumnIndex(KEY_IRCODE);
		c.moveToFirst();
		
		if(!c.isAfterLast()) return c.getString(ir);
		else return null;
	}
	
	/**
	 * This function searches for the row with the IR code in the database.
	 * Needs to use LIKE instead of MATCH for some reason (don't know why, just does)
	 * @param remote_name
	 * @param commandName
	 * @return
	 */
	private Cursor findCommand(String remote_name, String commandName)
	{
		return _database.query(DATABASE_TABLE, columns, KEY_CMDNAME + " LIKE '" + commandName + "' and " + 
				KEY_REMOTE_NAME + " LIKE '" + remote_name + "'", 
				null, null, null, null);
	}
	
	/** Deletes all entries in the database **/
	public void clearDatabase()
	{
		_database.delete(DATABASE_TABLE, null, null);
	}
	
	/**Opens the database and returns it to caller **/
	public cmdDataBase open() throws SQLiteException
	{
		sqlHandler = new SQLHandler(_context);
		_database = sqlHandler.getWritableDatabase();
		return this;
	}
	
	/**Close the database **/
	public void close()
	{
		sqlHandler.close();
	}
	
	public void dropTable()
	{
		sqlHandler = new SQLHandler(_context);
		_database = sqlHandler.getWritableDatabase();
		_database.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		_database.execSQL("VACUUM");
	}
}
