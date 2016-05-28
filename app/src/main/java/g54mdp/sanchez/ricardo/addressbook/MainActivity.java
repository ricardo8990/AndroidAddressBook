package g54mdp.sanchez.ricardo.addressbook;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * Main activity, it is composed mainly by a listView which should be populated using the
 * ContentProvider created
 */
public class MainActivity extends ActionBarActivity
{

    //region Override methods
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        UpdateContactListView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id)
        {
            case R.id.action_createContact:
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion

    /**
     * Update the listView with the contacts obtained from the ContentProvider
     * The cursor is not closed because it produce problems to the list when it's populated
     */
    void UpdateContactListView()
    {
        //Id's to be updated from the cursor
        int[] listItems = {R.id.list_item_id, R.id.list_item_image, R.id.list_item_name};

        //Obtain the cursor
        String[] projection = new String[]
                {
                        AddressBookCPContract.Items._ID,
                        AddressBookCPContract.Items.IMAGE_URI,
                        AddressBookCPContract.Items.NAME
                };
        Cursor c = getContentResolver().query(AddressBookCPContract.CONTACT_URI, projection, null, null, null);
        //Populate the list view using a cursor adapter
        ListView contactListView = (ListView) findViewById(R.id.ContactListView);
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.contact_list_item, c, projection, listItems, 0);
        //Set how the image will be managed
        cursorAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder()
        {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex)
            {
                if(view.getId() == R.id.list_item_image)
                {
                    setPicture((ImageView) view, cursor.getString(cursor.getColumnIndex(AddressBookCPContract.Items.IMAGE_URI)));
                    return true;
                }
                return false;
            }
        });
        contactListView.setAdapter(cursorAdapter);

        //Add a listener to the listView
        contactListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //Initialize the contact activity and send the id selected
                Bundle bundle = new Bundle();
                bundle.putLong("Id", id);
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    /**
     * It scales the picture to avoid running out of memory when displaying the image
     * http://developer.android.com/training/camera/photobasics.html
     */
    private void setPicture(ImageView imageView, String imageUri)
    {
        if(imageUri == null || imageUri.isEmpty())
        {
            imageView.setImageResource(R.mipmap.no_contact);
            return;
        }
        // Get the dimensions of the View
        int targetW = (int) getResources().getDimension(R.dimen.ImageViewWidth);
        int targetH = (int) getResources().getDimension(R.dimen.ImageViewHeight);

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        String path = getPathFromUri(imageUri);
        if(path == null)
        {
            Log.d("g54mdp", "Could not obtain the path for the URI: " + imageUri);
            imageView.setImageResource(R.mipmap.no_contact);
            return;
        }
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    /**
     * The value stored in the ContentProvider is an Uri, in order to decode the image we need the
     * physical path
     * The cursor can thrown an error for different ways, due to this is not a key feature it is
     * surrounded by a try-catch and log the error
     * @param uriString The URI obtained from the ContentProvider
     * @return The path to be used for decode
     */
    private String getPathFromUri(String uriString)
    {
        String path = null;
        try
        {
            Uri uri = Uri.parse(uriString);
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null)
            {
                //The source is in the gallery
                cursor.moveToFirst();
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                cursor.close();
            } else
            {
                //Source is a local path
                path = uri.getPath();
            }
        }
        catch (Exception e)
        {
            Log.d("g54mdp", "Error while trying to get the path: " + e.getMessage());
        }
        return path;
    }
}
