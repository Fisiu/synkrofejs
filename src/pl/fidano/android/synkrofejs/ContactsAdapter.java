package pl.fidano.android.synkrofejs;

import pl.fidano.android.synkrofejs.MainActivity.QueryContactsInGroup;
import pl.fidano.android.synkrofejs.utils.ContactFaceTask;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {

	private static final String TAG = "ContactsAdapter";

	private LayoutInflater layoutInflater;
	private ContactFaceTask faceWorker;

	public ContactsAdapter(Context context, ContactFaceTask faceWorker) {
		super(context, null, 0);
		layoutInflater = LayoutInflater.from(context);
		this.faceWorker = faceWorker;
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

		Drawable dr = faceWorker.loadImage(this, contactId);
		if (dr != null) {
			photoView.setImageDrawable(dr);
		} else {
			photoView.setImageResource(R.drawable.ic_contact_picture);
		}
		nameView.setText(name);
	}
	
	
}