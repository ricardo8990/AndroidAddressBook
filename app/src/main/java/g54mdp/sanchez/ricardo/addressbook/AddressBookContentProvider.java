package g54mdp.sanchez.ricardo.addressbook;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * ContentProvider created, contract defined in g54mdp.sanchez.ricardo.addressbook.AddressBookCPContract
 */
public class AddressBookContentProvider extends ContentProvider
{
    DBHelper database;
    private static final UriMatcher uriMatcher;
    protected static String contactBasePath = "CONTACT";
    private static final int contactOperationList = 1;
    private static final int contactOperationId = 2;

    static
    {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AddressBookCPContract.AUTHORITY, contactBasePath, contactOperationList);
        uriMatcher.addURI(AddressBookCPContract.AUTHORITY, contactBasePath + "/#", contactOperationId);
    }

    public AddressBookContentProvider()    {    }

    @Override
    public String getType(Uri uri)
    {
        switch (uriMatcher.match(uri))
        {
            case contactOperationList:
                return AddressBookCPContract.CONTENT_TYPE;
            case contactOperationId:
                return AddressBookCPContract.CONTENT_TYPE_ITEM;
            default:
                return null;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        long id;
        switch (uriMatcher.match(uri))
        {
            case contactOperationList:
                SQLiteDatabase db = database.getWritableDatabase();
                id =  db.insert(ContactEntryColumns.tableName, null, values);
                db.close();
                break;

            default:
                throw new UnsupportedOperationException("Operation not supported over URI: " + uri);
        }

        Uri itemUri = ContentUris.withAppendedId(uri, id);
        //Notify all listeners of changes
        if(id > 0)
            getContext().getContentResolver().notifyChange(itemUri, null);
        return itemUri;
    }

    @Override
    public boolean onCreate()
    {
        database = new DBHelper(this.getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder)
    {
        //Validate columns in projection
        CheckColumns(projection);

        switch (uriMatcher.match(uri))
        {
            case contactOperationList:
                SQLiteDatabase db = database.getWritableDatabase();
                return db.query(ContactEntryColumns.tableName, projection, selection, selectionArgs, null, null, sortOrder);

            default:
                throw new UnsupportedOperationException("Operation not supported over URI: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int deletedElements;

        SQLiteDatabase db = database.getWritableDatabase();
        switch (uriMatcher.match(uri))
        {
            case contactOperationList:
                deletedElements = db.delete(ContactEntryColumns.tableName, selection, selectionArgs);
                db.close();
                break;

            case contactOperationId:
                String idStr = uri.getLastPathSegment();
                String sel = ContactEntryColumns._ID + " = ?";
                String[] args = new String[] {idStr};
                deletedElements = db.delete(ContactEntryColumns.tableName, sel, args);
                break;

            default:
                throw new UnsupportedOperationException("Operation not supported over URI: " + uri);
        }

        //Notify all listeners of changes
        if(deletedElements > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedElements;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs)
    {
        int updatedElements;

        SQLiteDatabase db = database.getWritableDatabase();
        switch (uriMatcher.match(uri))
        {
            case contactOperationList:
                updatedElements = db.update(ContactEntryColumns.tableName, values, selection, selectionArgs);
                db.close();
                break;

            case contactOperationId:
                String idStr = uri.getLastPathSegment();
                String sel = ContactEntryColumns._ID + " = ?";
                String[] args = new String[] {idStr};
                updatedElements = db.update(ContactEntryColumns.tableName, values, sel, args);
                break;

            default:
                throw new UnsupportedOperationException("Operation not supported over URI: " + uri);
        }

        //Notify all listeners of changes
        if(updatedElements > 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return updatedElements;
    }

    private void CheckColumns(String[] projection)
    {
        if(projection == null)
            return;

        ArrayList<String> requested = new ArrayList<>(Arrays.asList(projection));
        if(!ContactEntryColumns.availableColumns.containsAll(requested))
            throw new IllegalArgumentException("Unknown columns in projection");
    }
}
