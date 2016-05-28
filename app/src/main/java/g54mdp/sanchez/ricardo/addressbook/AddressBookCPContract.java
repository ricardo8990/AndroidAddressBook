package g54mdp.sanchez.ricardo.addressbook;

import android.net.Uri;

/**
 * Contract containing the constants defined for AddressBookContentProvider
 */
public final class AddressBookCPContract
{
    /**
     * The authority of the Content Provider
     */
    public static final String AUTHORITY = "g54mdp.sanchez.ricardo.addressbook.contentProvider";
    /**
     * The content URI
     */
    public static final Uri CONTACT_URI = Uri.parse("content://" + AUTHORITY + "/CONTACT");
    /**
     * Available fields in the Contact table
     */
    public static final class Items
    {
        /**
         * ID-Automatic
         */
        public static final String _ID = ContactEntryColumns._ID;
        /**
         * Contact name-String
         */
        public static final String NAME = ContactEntryColumns.column_name;
        /**
         * Phone number-String
         */
        public static final String PHONE = ContactEntryColumns.column_phone;
        /**
         * Email-String
         */
        public static final String EMAIL = ContactEntryColumns.column_email;
        /**
         * Image Uri-String, it only stores the URI, you should get the image bitmap for your own
         */
        public static final String IMAGE_URI = ContactEntryColumns.column_url_picture;
    }
    /**
     * Definition of content types
     */
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/g54mdp.ricardo.contact";
    public static final String CONTENT_TYPE_ITEM = "vnd.android.cursos.item/g54mdp.ricardo.contact";
    /**
     * This projection retrieve all available columns, if there is only need to certain columns
     * it can be constructed from the Items class
     */
    public static final String[] ProjectionAllColumns = new String[]
            {
                    Items._ID,
                    Items.NAME,
                    Items.PHONE,
                    Items.EMAIL,
                    Items.IMAGE_URI
            };
}
