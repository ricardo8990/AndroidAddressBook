package g54mdp.sanchez.ricardo.addressbook;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.ArrayList;

/**
 * This class manages the database schema, if something is changed the database version in DBHelper
 * must be updated
 */
public class ContactEntryColumns implements BaseColumns
{
    //Table definition
    public static final String tableName = "Contact";
    public static final String column_name = "Name";
    public static final String column_phone = "Phone";
    public static final String column_email = "Email";
    public static final String column_url_picture = "Picture";
    //List of available columns to validate
    public static final ArrayList<String> availableColumns = new ArrayList<String>()
    {{
        add(_ID);
        add(column_name);
        add(column_phone);
        add(column_email);
        add(column_url_picture);
    }};

    //Sentences
    private static final String sql_create =
            "CREATE TABLE " + tableName + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    column_name + " TEXT NOT NULL, " +
                    column_phone + " TEXT NOT NULL, " +
                    column_email + " TEXT, " +
                    column_url_picture + " TEXT " +
            ")";

    private static final String sql_drop_table = "DROP TABLE IF EXISTS " + tableName;

    //Command
    public static void onCreate(SQLiteDatabase db)
    {
        db.execSQL(sql_create);
    }

    public static void onUpgrade(SQLiteDatabase db)
    {
        db.execSQL(sql_drop_table);
        onCreate(db);
    }
}
