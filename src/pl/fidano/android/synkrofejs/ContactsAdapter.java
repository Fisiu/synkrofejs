package pl.fidano.android.synkrofejs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import pl.fidano.android.synkrofejs.MainActivity.ContactsQuery;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {

	private static final String TAG = "ContactsAdapter";
	private Context context;

	public ContactsAdapter(Context context) {
		super(context, null, 0);
		this.context = context;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		View rootView = layoutInflater.inflate(R.layout.contact_item, viewGroup, false);

		return rootView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		TextView idView = (TextView) view.findViewById(R.id.contactID);
		TextView nameView = (TextView) view.findViewById(R.id.contactName);
		TextView detailsView = (TextView) view.findViewById(R.id.contactDetails);
		ImageView photoView = (ImageView) view.findViewById(R.id.contactImage);

		// set data
		final String id = cursor.getString(ContactsQuery.ID);
		final String name = cursor.getString(ContactsQuery.DISPLAY_NAME);

		final String number = getPhoneNoumber(id);
		final String email = getEmailAddress(id);

		final long contactId = Long.parseLong(id);
		final InputStream is = openPhoto(contactId);
		final Bitmap image;
		if (is == null) {
			// use default image when no image set
			image = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
		} else {
			image = BitmapFactory.decodeStream(is);
		}

		nameView.setText(name);
		idView.setText(number);
		detailsView.setText(email);
		photoView.setImageBitmap(image);
	}

	private InputStream openPhoto(long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		Cursor cursor = context.getContentResolver().query(photoUri, new String[] { Contacts.Photo.PHOTO }, null, null,
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

	private String getPhoneNoumber(String id) {
		Cursor phoneCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { id }, null);
		String phoneNo = "";
		// get first phone number
		if (phoneCur.getCount() > 0) {
			phoneCur.moveToFirst();
			phoneNo = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		}
		phoneCur.close();
		return phoneNo;
	}

	private String getEmailAddress(String id) {
		Cursor emailCur = context.getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { id }, null);
		String email = "";
		// get first email address
		if (emailCur.getCount() > 0) {
			emailCur.moveToFirst();
			email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
		}
		emailCur.close();
		return email;
	}
}