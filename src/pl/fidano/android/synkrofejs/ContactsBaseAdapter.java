package pl.fidano.android.synkrofejs;

import pl.fidano.android.synkrofejs.model.SimpleContact;
import android.content.Context;
import android.database.MatrixCursor;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsBaseAdapter extends BaseAdapter {

	private final Context context;
	private MatrixCursor matrixCursor;

	public ContactsBaseAdapter(Context context, MatrixCursor matrixCursor) {
		this.context = context;
		this.matrixCursor = matrixCursor;
	}

	@Override
	public int getCount() {
		return matrixCursor.getCount();
	}

	@Override
	public SimpleContact getItem(int position) {
		matrixCursor.moveToPosition(position);
		String id = matrixCursor.getString(0);
		String name = matrixCursor.getString(1);
		String phone = matrixCursor.getString(2);
		String email = matrixCursor.getString(3);
		String photo = matrixCursor.getString(4);
		SimpleContact contact = new SimpleContact(Long.parseLong(id), name, phone, email, photo);
		return contact;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		TextView idView;
		TextView nameView;
		TextView detailsView;
		ImageView photoView;

		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false);
			idView = (TextView) convertView.findViewById(R.id.contactID);
			nameView = (TextView) convertView.findViewById(R.id.contactName);
			detailsView = (TextView) convertView.findViewById(R.id.contactDetails);
			photoView = (ImageView) convertView.findViewById(R.id.contactImage);
			convertView.setTag(new ViewHolder(idView, nameView, detailsView, photoView));
		} else {
			ViewHolder viewHolder = (ViewHolder) convertView.getTag();
			idView = viewHolder.idView;
			nameView = viewHolder.nameView;
			detailsView = viewHolder.detailsView;
			photoView = viewHolder.photoView;
		}

		SimpleContact simpleContact = getItem(position);
		nameView.setText(simpleContact.getName());
		idView.setText(simpleContact.getNumber());
		detailsView.setText(simpleContact.getEmail());
		if (simpleContact.getPhotoPath() == null) {
			photoView.setImageResource(R.drawable.ic_contact_picture);
		} else {
			photoView.setImageBitmap(BitmapFactory.decodeFile(simpleContact.getPhotoPath()));
		}
		
		return convertView;
	}

	private static class ViewHolder {
		public final TextView idView;
		public final TextView nameView;
		public final TextView detailsView;
		public final ImageView photoView;

		public ViewHolder(TextView idView, TextView nameView, TextView detailsView, ImageView photoView) {
			this.idView = idView;
			this.nameView = nameView;
			this.detailsView = detailsView;
			this.photoView = photoView;
		}
	}
}
