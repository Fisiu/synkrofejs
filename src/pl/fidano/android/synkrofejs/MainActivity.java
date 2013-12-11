package pl.fidano.android.synkrofejs;

import java.util.Arrays;

import pl.fidano.android.synkrofejs.Utils.Utils;
import pl.fidano.android.synkrofejs.dialog.AccountsDialogFragment;
import pl.fidano.android.synkrofejs.dialog.AccountsDialogFragment.AccountsDialogListener;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements LoaderCallbacks<Cursor>, AccountsDialogListener {

	private static final String TAG = "MainActivity";

	private static final String ACCOUNT_TYPE = "com.google";
	public static final String BUNDLE_KEY = "accounts";
	CharSequence[] accountNames;

	private RelativeLayout header;
	private ProgressBar progressBar;

	private ListView contactsListView;
	private ContactsAdapter contactsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Log.i(getClass().getSimpleName(), "onCreate()");

		TextView headerImgSystem = (TextView) findViewById(R.id.headerImgSystem);
		TextView headerImgFacebook = (TextView) findViewById(R.id.headerImgFacebook);

		headerImgSystem.setText(R.string.label_phone);
		headerImgFacebook.setText(R.string.label_facebook);

		header = (RelativeLayout) findViewById(R.id.contactListHeader);
		header.setVisibility(View.INVISIBLE);

		progressBar = (ProgressBar) findViewById(R.id.contactProgressBar);

		contactsListView = (ListView) findViewById(R.id.contactsList);
		contactsListView.setFastScrollEnabled(true);
		contactsListView.setEmptyView(null);

		contactsAdapter = new ContactsAdapter(this);
		contactsListView.setAdapter(contactsAdapter);

		showAccountDialog();

		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Cursor cursor = contactsAdapter.getCursor();
				cursor.moveToPosition(position);

				// Creates a contact lookup Uri from contact ID and lookup_key
				final long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
				final String contactLookupKey = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
				final Uri uri = Contacts.getLookupUri(contactId, contactLookupKey);
				// TODO: Make use from contacts uri

				TextView name = (TextView) view.findViewById(R.id.contactName);
				Toast.makeText(getBaseContext(), name.getText(), Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		contactsAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		Log.d(TAG, "onCreateLoader");
		// If this is the loader for finding contacts in the Contacts Provider
		// (the only one supported)
		if (id == ContactsQuery.QUERY_ID) {

			contactsListView.setEmptyView(progressBar);

			final Uri contentUri = ContactsQuery.CONTENT_URI;
			return new CursorLoader(getApplication(), contentUri, ContactsQuery.PROJECTION, ContactsQuery.SELECTION,
					null, ContactsQuery.SORT_ORDER);
		}

		Log.d(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		header.setVisibility(View.VISIBLE);
		contactsAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		contactsAdapter.swapCursor(null);
	}

	public interface ContactsQuery {

		// An identifier for the loader
		final static int QUERY_ID = 1;
		// A content URI for the Contacts table
		final static Uri CONTENT_URI = Contacts.CONTENT_URI;
		// The selection clause for the CursorLoader query. The search criteria
		// defined here restrict results to contacts that have a display name
		// and are linked to visible groups.
		// Notice that the search on the string provided by the user is
		// implemented by appending the search string to CONTENT_FILTER_URI.
		final static String SELECTION = (Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME)
				+ "<>''" + " AND " + Contacts.HAS_PHONE_NUMBER + "=1" + " AND " + Contacts.IN_VISIBLE_GROUP + "=1";
		// The desired sort order for the returned Cursor. In Android 3.0 and
		// later, the primary sort key allows for localization. In earlier
		// versions. use the display name as the sort key.
		final static String SORT_ORDER = Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY : Contacts.DISPLAY_NAME;
		// The projection for the CursorLoader query. This is a list of columns
		// that the Contacts
		// Provider should return in the Cursor.
		final static String[] PROJECTION = { Contacts._ID, Contacts.LOOKUP_KEY,
				Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME };

		// The query column numbers which map to each value in the projection
		static final int ID = 0;
		static final int LOOKUP_KEY = 1;
		static final int DISPLAY_NAME = 2;
	}

	private void showAccountDialog() {
		AccountsDialogFragment accountsDialog = new AccountsDialogFragment();
		Bundle args = new Bundle();
		args.putCharSequenceArray(BUNDLE_KEY, getGoogleAccounts());
		accountsDialog.setArguments(args);
		accountsDialog.show(getSupportFragmentManager(), "TAG");
	}

	private CharSequence[] getGoogleAccounts() {
		AccountManager manager = AccountManager.get(this);
		Account[] accounts = manager.getAccountsByType(ACCOUNT_TYPE);
		accountNames = new CharSequence[accounts.length];
		for (int i = 0; i < accounts.length; i++) {
			accountNames[i] = accounts[i].name;
		}
		return accountNames;
	}

	@Override
	public void onAccountSelected(int accountEmail) {
		Toast.makeText(this, accountNames[accountEmail], Toast.LENGTH_SHORT).show();
		// TODO: Setup cursorloader to fetch contacts for selected account only
		getSupportLoaderManager().initLoader(1, null, this);
	}

	@Override
	public void onNegativeButtonClick() {
		// no account chosen, exit app
		Toast.makeText(this, "Exiting...", Toast.LENGTH_SHORT).show();
		this.finish();
	}

}
