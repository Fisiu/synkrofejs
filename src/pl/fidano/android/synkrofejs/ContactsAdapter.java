package pl.fidano.android.synkrofejs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import pl.fidano.android.synkrofejs.MainActivity.QueryContactsInGroup;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {

	private static final String TAG = "ContactsAdapter";

	private Context context;
	private LayoutInflater layoutInflater;

	public ContactsAdapter(Context context) {
		super(context, null, 0);
		this.context = context;
		layoutInflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		Log.d(TAG, "newView()");

		View rootView = layoutInflater.inflate(R.layout.contact_item, viewGroup, false);
		return rootView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		Log.d(TAG, "bindView()");

		TextView nameView = (TextView) view.findViewById(R.id.contactName);
		ImageView photoView = (ImageView) view.findViewById(R.id.contactImage);

		// set data
		final long contactId = cursor.getLong(QueryContactsInGroup.ID);
		final String name = cursor.getString(QueryContactsInGroup.DISPLAY_NAME);

		final InputStream is = openPhoto(contactId);
		final Bitmap image;
		if (is == null) {
			// use default image when no image set
			image = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		} else {
			image = BitmapFactory.decodeStream(is);
		}

		nameView.setText(name);
		photoView.setImageBitmap(image);
	}

	private InputStream openPhoto(long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		CursorLoader cursorLoader = new CursorLoader(context, photoUri, new String[] { Contacts.Photo.PHOTO }, null,
				null, null);
		Cursor cursor = cursorLoader.loadInBackground();
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