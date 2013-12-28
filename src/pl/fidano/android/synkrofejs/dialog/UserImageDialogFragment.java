package pl.fidano.android.synkrofejs.dialog;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import pl.fidano.android.synkrofejs.R;
import pl.fidano.android.synkrofejs.utils.Constans;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class UserImageDialogFragment extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		final long contactId = args.getLong(Constans.BUNDLE_KEY_USER_ID);
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.dialog_user_image, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.user_image);

		final Bitmap bitmap = BitmapFactory.decodeStream(openDisplayPhoto(contactId));
		imageView.setImageBitmap(bitmap);

		builder.setView(view)
		// Add action buttons
				.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dismiss();
					}
				});
		return builder.create();
	}

	public InputStream openDisplayPhoto(long contactId) {
		Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		return Contacts.openContactPhotoInputStream(getActivity().getContentResolver(), contactUri);
	}
}
