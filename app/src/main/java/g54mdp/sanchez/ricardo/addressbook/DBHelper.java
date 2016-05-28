package g54mdp.sanchez.ricardo.addressbook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class manages the access to the Database, update the version if the schema is changed
 */
public class DBHelper extends SQLiteOpenHelper
{
    //If the database schema change, update the version
    public static final int dbVersion = 2;
    public static final String dbName = "Contact.db";

    public DBHelper(Context context)
    {
        super(context, dbName, null, dbVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        ContactEntryColumns.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        ContactEntryColumns.onUpgrade(db);
    }
}
