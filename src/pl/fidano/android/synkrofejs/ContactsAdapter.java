package pl.fidano.android.synkrofejs;

import pl.fidano.android.synkrofejs.MainActivity.QueryContactsInGroup;
import pl.fidano.android.synkrofejs.utils.ContactFaceTask;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
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
		View rootView = layoutInflater.inflate(R.layout.contact_item, viewGroup, false);

		ViewHolder holder = new ViewHolder();
		holder.cName = (TextView) rootView.findViewById(R.id.contactName);
		holder.cImage = (ImageView) rootView.findViewById(R.id.contactImage);
		rootView.setTag(holder);
		return rootView;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		// set data
		final long contactId = cursor.getLong(QueryContactsInGroup.ID);
		final String name = cursor.getString(QueryContactsInGroup.DISPLAY_NAME);

		holder.cName.setText(name);

		final Bitmap bitmap = faceWorker.loadImage(this, contactId);
		holder.cImage.setImageBitmap(bitmap);
	}

	private static class ViewHolder {
		TextView cName;
		ImageView cImage;
	}
}