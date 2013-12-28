package pl.fidano.android.synkrofejs.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import pl.fidano.android.synkrofejs.ContactsAdapter;
import pl.fidano.android.synkrofejs.R;
import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class ContactFaceTask {

	private static final String TAG = "ContactFaceTask";

	private Context context;
	private HashMap<Long, Bitmap> cache;
	private static Bitmap DEFAULT_FACE = null;
	private ContactsAdapter adapter;

	public ContactFaceTask(Context context) {
		this.context = context;
		cache = new HashMap<Long, Bitmap>();
		DEFAULT_FACE = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
	}

	public Bitmap loadImage(ContactsAdapter adapter, long contactId) {
		this.adapter = adapter;

		// if contact id is already in cache
		if (cache.containsKey(contactId)) {
			final Bitmap bitmap = cache.get(contactId);
			if (bitmap != null) {
				return bitmap; // contact has image
			} else {
				return DEFAULT_FACE; // contact has no image, use placeholder
			}
		} else {
			// no contact id in cache
			new ImageTask().execute(contactId); // fetch image
			return DEFAULT_FACE; // use placeholder for now
		}
	}

	private class ImageTask extends AsyncTask<Long, Void, Bitmap> {

		private long contactId;

		@Override
		protected Bitmap doInBackground(Long... params) {
			contactId = params[0];
			Bitmap bitmap = BitmapFactory.decodeStream(openPhoto(contactId));

			if (isCancelled()) {
				return null;
			}
			if (bitmap == null) {
				return null;
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			Log.d(TAG, "Task completed!");
			if (isCancelled()) {
				result = null;
			}
			cache.put(contactId, result);
			if (result != null) {
				// refresh adapter only when contact has image
				adapter.notifyDataSetChanged();
			}
		}

		@SuppressLint("InlinedApi")
		private InputStream openPhoto(long contactId) {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
			return Contacts.openContactPhotoInputStream(context.getContentResolver(), contactUri);
		}
	}
}
