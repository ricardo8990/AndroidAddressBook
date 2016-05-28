package g54mdp.sanchez.ricardo.addressbook;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * This activity creates, update or delete a contact.
 * It receives a contactId to be updated, if not provided it will create a new one
 * The picturePath is the value which will be stored as Image_Uri in the content provider
 */
public class ContactActivity extends ActionBarActivity
{
    private Long contactId;
    private final int cameraActionCode = 0;
    private final int photosActionCode = 1;
    private String picturePath;


    //region Override methods

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_details);

        //Retrieve the previous Id if any
        if(savedInstanceState != null)
        {
            contactId = savedInstanceState.getLong("contactId");
            picturePath = savedInstanceState.getString("picturePath");
        }
        else
        {
            //Get the ID in case it comes from a selected item in the list
            Bundle bundle = getIntent().getExtras();
            contactId = bundle == null ? null : bundle.getLong("Id", -1);
        }

        Button deleteButton = (Button) findViewById(R.id.DeleteButton);
        ImageButton imageButton = (ImageButton) findViewById(R.id.UplodaImageButton);
        ImageView imageView = (ImageView) findViewById(R.id.ImageContact);

        if (contactId != null)
        {
            //If there is an Id, initialize the values and enable the edit button
            //Get the contact with this ID
            String selectionClause = AddressBookCPContract.Items._ID + " = ?";
            String[] selectionArgs = {String.valueOf(contactId)};

            Cursor cursor = getContentResolver().query(AddressBookCPContract.CONTACT_URI,
                    AddressBookCPContract.ProjectionAllColumns, selectionClause, selectionArgs, null);

            //If the content resolver retrieve an error, log and return to the main activity
            if(cursor == null)
            {
                Log.d("g54mdp", "Error while retrieving contact with Id: " + contactId);
                finish();
                return;
            }

            //If the content resolver does not find any value, show message and return to main activity
            if(cursor.getCount() != 1)
            {
                //Show notification to the user
                Toast toast = Toast.makeText(this, getString(R.string.NoContactFound), Toast.LENGTH_SHORT);
                toast.show();
                finish();
                return;
            }

            //Get widgets
            EditText nameET = (EditText) findViewById(R.id.editTextName);
            EditText phoneET = (EditText) findViewById(R.id.editTextPhone);
            EditText mailET = (EditText) findViewById(R.id.editTextEmail);

            //Set the values
            cursor.moveToNext();
            nameET.setText(cursor.getString(cursor.getColumnIndex(AddressBookCPContract.Items.NAME)));
            phoneET.setText(cursor.getString(cursor.getColumnIndex(AddressBookCPContract.Items.PHONE)));
            mailET.setText(cursor.getString(cursor.getColumnIndex(AddressBookCPContract.Items.EMAIL)));
            //Keep the previous path if any, it may be updated
            if(picturePath == null)
                picturePath = cursor.getString(cursor.getColumnIndex(AddressBookCPContract.Items.IMAGE_URI));
            cursor.close();

            //Enable the delete button
            deleteButton.setVisibility(View.VISIBLE);
        }
        else
        {
            //Otherwise, it is a new contact so enable the new contact button
            //Show button to add image
            imageButton.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            //Disable delete button
            deleteButton.setVisibility(View.GONE);
        }
        //Show image if any
        if(picturePath != null && !picturePath.isEmpty())
        {
            imageButton.setVisibility(View.GONE);
            imageView.setImageURI(Uri.parse(picturePath));
            imageView.setVisibility(View.VISIBLE);
        }
        else
        {
            //Else hide the imageView and show the button
            imageButton.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstance)
    {
        if(contactId != null)
            savedInstance.putLong("contactId", contactId);
        savedInstance.putString("picturePath", picturePath);
        super.onSaveInstanceState(savedInstance);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //If you return from the gallery the data will be null
        if(resultCode != RESULT_OK || (requestCode != 0 && data == null))
        {
            Log.d("g54mdp", "There was an error using the request code: " + requestCode);
            return;
        }
        //Get widgets
        ImageView imageView = (ImageView) findViewById(R.id.ImageContact);
        ImageButton imageButton = (ImageButton) findViewById(R.id.UplodaImageButton);

        Uri imageUri = null;
        switch (requestCode)
        {
            case cameraActionCode:
                //If the picture was taken with the camera, we use the picturePath containing the URI
                //with the previously created file
                imageUri = Uri.parse(picturePath);
                break;
            case photosActionCode:
                //If the picture was taken from the gallery, the uri will be contained in the data
                //In order to keep the app light, don't copy the file and just use the URI, even though
                // if the file is deleted from elsewhere, the image will not be shown
                imageUri = data.getData();
                break;
        }

        //Update the uri to the image
        picturePath = imageUri != null ? imageUri.toString() : null;
        imageView.setImageURI(imageUri);
        //Show the image and hide the button
        imageView.setVisibility(View.VISIBLE);
        imageButton.setVisibility(View.GONE);
    }
    //endregion

    //region Methods for updating the contact
    public void SaveContact(View view)
    {
        //Get values from interface
        ContentValues values;
        String message;
        try
        {
            values = GetValuesFromInterface();
        }
        catch(IllegalArgumentException e)
        {
            //Show notification to the user if there are errors
            Toast toast = Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if(values == null)
            return;

        //Save the contact if it is new
        if(contactId == null)
        {
            getContentResolver().insert(AddressBookCPContract.CONTACT_URI, values);
            message = getString(R.string.NewContactCreated);
        }
        else
        {
            //Edit the contact
            Uri uri = ContentUris.withAppendedId(AddressBookCPContract.CONTACT_URI, contactId);
            getContentResolver().update(uri, values, null, null);
            message = getString(R.string.ContactUpdated);
        }

        //Show notification to the user
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.show();

        //Destroy this activity
        finish();
    }

    public void DeleteContact(View view)
    {
        //Show an alert dialog to confirm the action
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_action_new_contact);
        builder.setTitle(getString(R.string.DeleteContact));
        builder.setMessage(getString(R.string.AreYouSure));
        builder.setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //Delete the contact
                        Uri uri = ContentUris.withAppendedId(AddressBookCPContract.CONTACT_URI, contactId);
                        getContentResolver().delete(uri, null, null);

                        //Show notification
                        Toast toast = Toast.makeText(getBaseContext(), getString(R.string.ContactDeleted), Toast.LENGTH_SHORT);
                        toast.show();

                        //Return to main activity
                        finish();
                    }
                });
        builder.setNegativeButton(getString(R.string.No), null);
        builder.show();
    }

    private ContentValues GetValuesFromInterface()
    {
        //Get widgets
        EditText nameET = (EditText) findViewById(R.id.editTextName);
        EditText phoneET = (EditText) findViewById(R.id.editTextPhone);
        EditText mailET = (EditText) findViewById(R.id.editTextEmail);

        //Extract values
        String name = nameET.getText().toString();
        String phone = phoneET.getText().toString();
        String email = mailET.getText().toString();
        String imageUri = picturePath;

        ArrayList<String> errors = hasInvalidElements(name, phone, email);
        if(errors != null && errors.size() > 0)
        {
            String message = "";
            for(String error : errors)
            {
                message += error + ", ";
            }
            throw new IllegalArgumentException(message);
        }

        //Generate content value for content provider
        ContentValues cv = new ContentValues();
        cv.put(AddressBookCPContract.Items.NAME, name);
        cv.put(AddressBookCPContract.Items.PHONE, phone);
        cv.put(AddressBookCPContract.Items.EMAIL, email);
        cv.put(AddressBookCPContract.Items.IMAGE_URI, imageUri);

        return cv;
    }

    //It validates that all required elements are filled
    private ArrayList<String> hasInvalidElements(String name, String phone, String email)
    {
        ArrayList<String> errors = new ArrayList<>();
        //Validate the name
        if(name == null || name.trim().isEmpty())
        {
            errors.add(getString(R.string.NameRequired));
        }

        //Validate the phone
        if(phone == null || phone.isEmpty())
        {
            errors.add(getString(R.string.PhoneRequired));
        }
        else if(!Patterns.PHONE.matcher(phone).matches())
        {
            errors.add(getString(R.string.PhoneFormatNoValid));
        }

        //Validate the email
        if(email == null)
        {
            errors.add(getString(R.string.InvalidEmail));
        }
        else if(!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            errors.add(getString(R.string.EmailFormatNoValid));
        }

        return errors;
    }
    //endregion

    //region Methods for manage the images
    public void UploadImageContact(View view)
    {
        ShowImageDialog(true);
    }

    public void UpdateImageContact(View view)
    {
        ShowImageDialog(false);
    }

    void ShowImageDialog(boolean isNewImage)
    {
        //Options depends on if we are going to update or create a new image
        final CharSequence[] items = {getString(R.string.Camera), getString(R.string.Photos),
                isNewImage ? getString(R.string.Cancel) : getString(R.string.RemovePhoto)};

        //Ask for the action to perform using a message
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setIcon(R.drawable.ic_action_new_contact);
        alert.setTitle(getString(R.string.UpdateImage));
        alert.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(items[which] == getString(R.string.Camera))
                {
                    //Create a file to store the picture
                    File photoFile = createImageFile();
                    //Select camera and sent an intent for the camera
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    startActivityForResult(intent, cameraActionCode);
                }
                else if(items[which] == getString(R.string.Photos))
                {
                    //Select photos, sent an intent for the gallery, it needs access to external storage
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, photosActionCode);
                }
                else if(items[which] == getString(R.string.RemovePhoto))
                {
                    picturePath = null;
                    //Hide the image and show the new image button
                    ImageView imageView = (ImageView) findViewById(R.id.ImageContact);
                    imageView.setVisibility(View.GONE);
                    ImageButton imageButton = (ImageButton) findViewById(R.id.UplodaImageButton);
                    imageButton.setVisibility(View.VISIBLE);
                }
            }
        });
        alert.show();
    }

    //Function taken from SDK documentation: http://developer.android.com/training/camera/photobasics.html
    //To create the file where the camera image will be stored
    private File createImageFile()
    {
        try
        {
            String timeStamp = SimpleDateFormat.getDateTimeInstance().format(new Date());
            String imageFileName = "CONTACT_IMAGE_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            picturePath = "file:" + image.getAbsolutePath();
            return image;
        }
        catch (Exception e)
        {
            Log.d("g54mdp", "File could not be created: " + e.getMessage());
            finish();
            return null;
        }
    }
    //endregion
}
