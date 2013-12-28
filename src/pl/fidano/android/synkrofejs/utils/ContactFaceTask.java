package pl.fidano.android.synkrofejs.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

import pl.fidano.android.synkrofejs.ContactsAdapter;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.widget.ImageView;

public class ContactFaceTask {

	private static final String TAG = "ContactFaceTask";

	private Context context;
	private HashMap<Long, Drawable> cache;
	private static final Drawable DEFAULT_FACE = null;
	private ContactsAdapter adapter;

	public ContactFaceTask(Context context) {
		this.context = context;
		cache = new HashMap<Long, Drawable>();
	}

	public Drawable loadImage(ContactsAdapter adapter, long contactId) {
		this.adapter = adapter;

		if (cache.containsKey(contactId)) {
			Log.d(TAG, "item [" + contactId + "] already in cache");
			return cache.get(contactId);
		} else {
			new ImageTask().execute(contactId);
			return DEFAULT_FACE;
		}
	}

	private class ImageTask extends AsyncTask<Long, Void, Drawable> {

		private long contactId;

		@Override
		protected Drawable doInBackground(Long... params) {
			contactId = params[0];
			Bitmap bitmap = BitmapFactory.decodeStream(openPhoto(contactId));

			if (isCancelled()) {
				return null;
			}
			if (bitmap == null) {
				return null;
			}
			Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
			return drawable;
		}

		@Override
		protected void onPostExecute(Drawable result) {
			Log.d(TAG, "Task completed!");
			synchronized (this) {
				// add image to cache
				cache.put(contactId, result);
			}
			if (result != null) {
				adapter.notifyDataSetChanged();
			}
		}

		private InputStream openPhoto(long contactId) {
			Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
			Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
			Cursor cursor = context.getContentResolver().query(photoUri, new String[] { Contacts.Photo.PHOTO }, null,
					null, null);
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
}
