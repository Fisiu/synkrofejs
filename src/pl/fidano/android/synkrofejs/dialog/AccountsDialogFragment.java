package pl.fidano.android.synkrofejs.dialog;

import pl.fidano.android.synkrofejs.MainActivity;
import pl.fidano.android.synkrofejs.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AccountsDialogFragment extends DialogFragment {

	AccountsDialogListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			// Instantiate the AccountsDialogListener so we can send events to the host
			mListener = (AccountsDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement AccountsDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		final CharSequence[] items = args.getCharSequenceArray(MainActivity.BUNDLE_KEY);
		// Use the Builder class for convenient dialog construction
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.title_account_dialog).setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onAccountSelected(which);
			}
		}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// User cancelled the dialog
				mListener.onNegativeButtonClick();
			}
		});
		// Create the AlertDialog object and return it
		return builder.create();
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		dialog.dismiss();
		mListener.onNegativeButtonClick();
	}

	public interface AccountsDialogListener {
		public void onAccountSelected(int accountEmail);

		public void onNegativeButtonClick();
	}
}