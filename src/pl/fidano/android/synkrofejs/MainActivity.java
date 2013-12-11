package pl.fidano.android.synkrofejs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pl.fidano.android.synkrofejs.Utils.Utils;
import pl.fidano.android.synkrofejs.model.Contact;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private static final String TAG = "MainActivity";

	private static final String ACCOUNT_TYPE = "com.google";
	private static final String MY_CONTACTS_GROUP = "6";

	private Drawable unchangedThumb;
	private Drawable defaultThumb;
	private SparseArray<Drawable> thumbCache;

	private ListView contactsListView;
	private ContactsAdapter contactsAdapter;

	private TextView emptyList;
	private ProgressBar progressBar;

	private LoadContactsTask contactsLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i(getClass().getSimpleName(), "onCreate()");

		unchangedThumb = new BitmapDrawable(getResources(), Bitmap.createBitmap(1, 1, Config.ALPHA_8));
		defaultThumb = getResources().getDrawable(R.drawable.ic_contact_picture);
		thumbCache = new SparseArray<Drawable>();

		TextView headerImgSystem = (TextView) findViewById(R.id.headerImgSystem);
		headerImgSystem.setText(R.string.label_phone);

		TextView headerImgFacebook = (TextView) findViewById(R.id.headerImgFacebook);
		headerImgFacebook.setText(R.string.label_facebook);

		contactsListView = (ListView) findViewById(R.id.contactsList);
		emptyList = (TextView) findViewById(R.id.emptyList);
		progressBar = (ProgressBar) findViewById(R.id.loading);

		contactsListView.setFastScrollEnabled(true);
		contactsListView.setEmptyView(progressBar);

		contactsAdapter = new ContactsAdapter();
		contactsListView.setAdapter(contactsAdapter);

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
		contactsLoader = new LoadContactsTask();
		// contactsLoader.execute("fisiu82@gmail.com");
		contactsLoader.execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class LoadContactsTask extends AsyncTask<String, Void, List<Contact>> {

		@Override
		protected List<Contact> doInBackground(String... params) {
			Cursor contactCursor = getContentResolver().query(ContactQuery.CONTENT_URI, ContactQuery.PROJECTION,
					ContactQuery.SELECTION, null, ContactQuery.SORT_ORDER);
			if (contactCursor == null)
				return null;

			try {
				List<Contact> contacts = new ArrayList<Contact>();
				while (contactCursor.moveToNext()) {
					Contact contact = new Contact();
					contact.setContactId(contactCursor.getInt(ContactQuery.ID));
					contact.setDisplayName(contactCursor.getString(ContactQuery.DISPLAY_NAME));
					contacts.add(contact);
				}
				return contacts;
			} finally {
				contactCursor.close();
			}
		}

		@Override
		protected void onPostExecute(List<Contact> result) {
			if (result != null) {
				contactsAdapter.update(result);
			}
			// FIXME we don't have to do this every time!
			progressBar.setProgress(100);
			// contactsListView.setEmptyView(emptyList);
		}

	}

	private interface ContactQuery {
		static final Uri CONTENT_URI = Contacts.CONTENT_URI;
		static final String SELECTION = (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME)
				+ "<>''" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1" + " AND " + Contacts.HAS_PHONE_NUMBER + "=1";
		static final String SORT_ORDER = Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;
		static final String[] PROJECTION = { Contacts._ID, Contacts.LOOKUP_KEY,
				Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME };

		static final int ID = 0;
		static final int DISPLAY_NAME = 2;

	}

	class LoadThumbTask extends AsyncTask<Integer, Void, Drawable> {

		/**
		 * Raw contact ID of the contact to load the thumbnail for.
		 */
		private int rawContactId;

		@Override
		protected Drawable doInBackground(Integer... params) {
			rawContactId = params[0];
			Bitmap bitmap = BitmapFactory.decodeStream(openPhoto(rawContactId));
			if (isCancelled()) {
				return null;
			}
			if (bitmap == null) {
				return null;
			}
			Drawable drawable = new BitmapDrawable(getResources(), bitmap);
			return drawable;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			Log.d("LoadThumbTask", "onPostExecute()");
			// asyncTasks.remove(this);
			if (result != null) {
				storeDrawableInCache(rawContactId, result);
				// removeDiskCache(rawContactId);
				contactsAdapter.notifyDataSetChanged();
			}
		}

		private void storeDrawableInCache(int contactId, Drawable drawable) {
			thumbCache.put(contactId, drawable);
		}

		public InputStream openPhoto(int contactId) {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
			Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
			Cursor cursor = getContentResolver().query(photoUri, new String[] { Contacts.Photo.PHOTO }, null, null,
					null);
			if (cursor == null) {
				return null;
			}
			try {
				if (cursor.moveToFirst()) {
					byte[] data = cursor.getBlob(0);
					if (data != null) {
						return new ByteArrayInputStream(data);
					}
				}
			} finally {
				cursor.close();
			}
			return null;
		}

	}

	class ContactsAdapter extends BaseAdapter {

		private LayoutInflater layoutInflater;
		private List<Contact> items = Collections.emptyList();

		public ContactsAdapter() {
			layoutInflater = LayoutInflater.from(getBaseContext());
		}

		public void update(List<Contact> newItems) {
			this.items = newItems;
			notifyDataSetChanged();
		}

		public List<Contact> getContactList() {
			return Collections.unmodifiableList(items);
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Contact getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView nameView;
			TextView phoneView;
			TextView emailView;
			ImageView photoView;

			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.contact_item, null);
				nameView = (TextView) convertView.findViewById(R.id.contactName);
				phoneView = (TextView) convertView.findViewById(R.id.contactPhone);
				emailView = (TextView) convertView.findViewById(R.id.contactEmail);
				photoView = (ImageView) convertView.findViewById(R.id.contactImage);
				convertView.setTag(new ViewHolder(position, nameView, phoneView, emailView, photoView));
			} else {
				ViewHolder viewHolder = (ViewHolder) convertView.getTag();
				nameView = viewHolder.name;
				phoneView = viewHolder.phone;
				emailView = viewHolder.email;
				photoView = viewHolder.photo;
			}

			Contact contact = getItem(position);
			nameView.setText(contact.getDisplayName());

			// TODO: bind the rest of data
			Drawable thumb = thumbCache.get(contact.getContactId());
			if (thumb == null) {
				new LoadThumbTask().execute(contact.getContactId());
			}
			// thumb = thumbCache.get(contact.getContactId());
			photoView.setImageDrawable(thumb);

			return convertView;
		}

		private class ViewHolder {
			int position;
			TextView name;
			TextView phone;
			TextView email;
			ImageView photo;

			public ViewHolder(int position, TextView name, TextView phone, TextView email, ImageView photo) {
				this.position = position;
				this.name = name;
				this.phone = phone;
				this.email = email;
				this.photo = photo;
			}
		}
	}
}