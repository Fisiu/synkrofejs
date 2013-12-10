package pl.fidano.android.synkrofejs;

import java.io.File;
import java.io.FileOutputStream;

import pl.fidano.android.synkrofejs.Utils.Utils;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";
	private ListView contactsListView;
	private ContactsBaseAdapter mAdapter;
	private MatrixCursor matrixCursor;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i(getClass().getSimpleName(), "onCreate()");

		TextView headerImgSystem = (TextView) findViewById(R.id.headerImgSystem);
		TextView headerImgFacebook = (TextView) findViewById(R.id.headerImgFacebook);

		headerImgSystem.setText(R.string.label_phone);
		headerImgFacebook.setText(R.string.label_facebook);

		contactsListView = (ListView) findViewById(R.id.contactsList);
		contactsListView.setFastScrollEnabled(true);

		// The contacts from the contacts content provider is stored in this cursor
		matrixCursor = new MatrixCursor(new String[] { "id", "name", "phone", "email", "photo" });
		progressDialog = new ProgressDialog(this);

		mAdapter = new ContactsBaseAdapter(this, matrixCursor);
		contactsListView.setAdapter(mAdapter);

		ListViewContactsLoader listViewContactsLoader = new ListViewContactsLoader();
		listViewContactsLoader.execute();

		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView name = (TextView) view.findViewById(R.id.contactName);
				Toast.makeText(getBaseContext(), name.getText(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** An AsyncTask class to retrieve and load listview with contacts */
	private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.setTitle("Loading contacts...");
			progressDialog.setMessage("Please wait");
			progressDialog.setCancelable(false);
			progressDialog.show();
		}

		@Override
		protected Cursor doInBackground(Void... params) {
			Uri contactsUri = Contacts.CONTENT_URI;
			// Querying the table ContactsContract.Contacts to retrieve all the contacts
			Cursor contactsCursor = getContentResolver().query(contactsUri, ContactsQuery.PROJECTION,
					ContactsQuery.SELECTION, null, ContactsQuery.SORT_ORDER);
			while (contactsCursor.moveToNext()) {
				long contactId = contactsCursor.getLong(ContactsQuery.ID);
				// Getting Display Name
				String displayName = contactsCursor.getString(ContactsQuery.DISPLAY_NAME);
				// Querying the table ContactsContract.Data to retrieve individual items like
				// home phone, mobile phone, work email etc corresponding to each contact
				Cursor dataCursor = getContentResolver().query(DataQuery.CONTENT_URI, null,
						Data.CONTACT_ID + "=" + contactId, null, null);
				String mobilePhone = "";
				String photoPath = "";
				byte[] photoByte = null;
				String homeEmail = "";
				while (dataCursor.moveToNext()) {
					// Getting Phone numbers
					if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(
							CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
						switch (dataCursor.getInt(dataCursor.getColumnIndex(CommonDataKinds.Phone.TYPE))) {
						case CommonDataKinds.Phone.TYPE_HOME:
							// homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
							break;
						case CommonDataKinds.Phone.TYPE_MOBILE:
							mobilePhone = dataCursor.getString(dataCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
							break;
						case CommonDataKinds.Phone.TYPE_WORK:
							// workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
							break;
						}
					}
					// Getting EMails
					if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(
							CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
						switch (dataCursor.getInt(dataCursor.getColumnIndex(CommonDataKinds.Email.TYPE))) {
						case CommonDataKinds.Email.TYPE_HOME:
							homeEmail = dataCursor.getString(dataCursor.getColumnIndex(CommonDataKinds.Email.ADDRESS));
							break;
						case CommonDataKinds.Email.TYPE_WORK:
							// workEmail = dataCursor.getString(dataCursor.getColumnIndex("data1"));
							break;
						}
					}
					// Getting Photo
					if (dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(
							CommonDataKinds.Photo.CONTENT_ITEM_TYPE)) {
						photoByte = dataCursor.getBlob(dataCursor.getColumnIndex(CommonDataKinds.Photo.PHOTO));
						if (photoByte != null) {
							Bitmap bitmap = BitmapFactory.decodeByteArray(photoByte, 0, photoByte.length);
							// Getting Caching directory
							File cacheDirectory = getBaseContext().getCacheDir();

							// Temporary file to store the contact image
							File tmpFile = new File(cacheDirectory.getPath() + "/synkrofejs_" + contactId + ".png");
							// The FileOutputStream to the temporary file
							try {
								FileOutputStream fOutStream = new FileOutputStream(tmpFile);
								// Writing the bitmap to the temporary file as png file
								bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutStream);
								// Flush the FileOutputStream
								fOutStream.flush();
								// Close the FileOutputStream
								fOutStream.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
							photoPath = tmpFile.getPath();
						} else {
							photoPath = null;
						}
					}
				}
				dataCursor.close();
				// Adding id, display name, path to photo and other details to cursor
				matrixCursor.addRow(new String[] { Long.toString(contactId), displayName, mobilePhone, homeEmail,
						photoPath });
			}
			contactsCursor.close();
			return matrixCursor;
		}

		@Override
		protected void onPostExecute(Cursor result) {
			mAdapter.notifyDataSetChanged();
			progressDialog.dismiss();
		}
	}

	public interface ContactsQuery {

		// An identifier for the loader
		final static int QUERY_ID = 1;
		// A content URI for the Contacts table
		final static Uri CONTENT_URI = Contacts.CONTENT_URI;
		// The selection clause for the CursorLoader query. The search criteria
		// defined here
		// restrict results to contacts that have a display name and are linked
		// to visible groups.
		// Notice that the search on the string provided by the user is
		// implemented by appending
		// the search string to CONTENT_FILTER_URI.
		// FIXME: Select only contacts in phone, ignore those on sim card
		final static String SELECTION = (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME)
				+ "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1" + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";
		// The desired sort order for the returned Cursor. In Android 3.0 and
		// later, the primary
		// sort key allows for localization. In earlier versions. use the
		// display name as the sort
		// key.
		final static String SORT_ORDER = Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;
		// The projection for the CursorLoader query. This is a list of columns
		// that the Contacts
		// Provider should return in the Cursor.
		final static String[] PROJECTION = {

				// The contact's row id
				Contacts._ID,

				// A pointer to the contact that is guaranteed to be more
				// permanent than _ID. Given
				// a contact's current _ID value and LOOKUP_KEY, the Contacts
				// Provider can generate
				// a "permanent" contact URI.
				// Contacts.LOOKUP_KEY,

				// In platform version 3.0 and later, the Contacts table
				// contains
				// DISPLAY_NAME_PRIMARY, which either contains the contact's
				// displayable name or
				// some other useful identifier such as an email address. This
				// column isn't
				// available in earlier versions of Android, so you must use
				Contacts.DISPLAY_NAME,
				// instead.
				// Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,

				// TODO: Add email
				// ContactsContract.CommonDataKinds.Phone.NUMBER,

				// In Android 3.0 and later, the thumbnail image is pointed to
				// by
				// PHOTO_THUMBNAIL_URI. In earlier versions, there is no direct
				// pointer; instead,
				// you generate the pointer from the contact's ID value and
				// constants defined in
				// android.provider.ContactsContract.Contacts.
				// Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI :
				// Contacts._ID,
				// Contacts.PHOTO_URI,

				// The sort order column for the returned Cursor, used by the
				// AlphabetIndexer
				SORT_ORDER, };

		// The query column numbers which map to each value in the projection
		final static int ID = 0;
		// final static int LOOKUP_KEY = 1;
		final static int DISPLAY_NAME = 1;
		// final static int PHONE_NUMBER = 3;
		// final static int PHOTO_THUMBNAIL_DATA = 4;
		// final static int SORT_KEY = 5;
	}

	public interface DataQuery {
		public static Uri CONTENT_URI = Data.CONTENT_URI;
	}
}
