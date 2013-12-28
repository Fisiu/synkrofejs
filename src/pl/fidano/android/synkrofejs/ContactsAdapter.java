package pl.fidano.android.synkrofejs;

import pl.fidano.android.synkrofejs.MainActivity.QueryContactsInGroup;
import pl.fidano.android.synkrofejs.dialog.UserImageDialogFragment;
import pl.fidano.android.synkrofejs.utils.Constans;
import pl.fidano.android.synkrofejs.utils.ContactFaceTask;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends CursorAdapter {

	@SuppressWarnings("unused")
	private static final String TAG = "ContactsAdapter";

	private LayoutInflater layoutInflater;
	private ContactFaceTask contactFaceTask;

	public ContactsAdapter(Context context, ContactFaceTask contactFaceTask) {
		super(context, null, 0);
		layoutInflater = LayoutInflater.from(context);
		this.contactFaceTask = contactFaceTask;
	}

	@Override
	public View newView(final Context context, Cursor cursor, ViewGroup viewGroup) {
		View rootView = layoutInflater.inflate(R.layout.contact_item, viewGroup, false);

		ViewHolder holder = new ViewHolder();
		holder.cName = (TextView) rootView.findViewById(R.id.contactName);
		holder.cImage = (ImageView) rootView.findViewById(R.id.contactImage);
		rootView.setTag(holder);
		return rootView;
	}

	@Override
	public void bindView(View view, final Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		// set data
		final long contactId = cursor.getLong(QueryContactsInGroup.ID);
		final String name = cursor.getString(QueryContactsInGroup.DISPLAY_NAME);

		holder.cName.setText(name);

		final Bitmap bitmap = contactFaceTask.loadImage(this, contactId);
		holder.cImage.setImageBitmap(bitmap);
		holder.cImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				DialogFragment newFragment = new UserImageDialogFragment();
				Bundle args = new Bundle();
				args.putLong(Constans.BUNDLE_KEY_USER_ID, contactId);
				newFragment.setArguments(args);
				newFragment.show(((FragmentActivity) context).getSupportFragmentManager(), "");

			}
		});
	}

	private static class ViewHolder {
		TextView cName;
		ImageView cImage;
	}
}
