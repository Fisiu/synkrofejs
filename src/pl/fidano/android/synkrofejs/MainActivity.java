package pl.fidano.android.synkrofejs;

import pl.fidano.android.synkrofejs.dialog.AccountsDialogFragment;
import pl.fidano.android.synkrofejs.dialog.AccountsDialogFragment.AccountsDialogListener;
import pl.fidano.android.synkrofejs.utils.ContactFaceTask;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
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
	private static final String MY_CONTACTS_GROUP_ID = "6";
	public static final String BUNDLE_KEY = "accounts";
	private CharSequence[] accountNames;
	private static String selectedAccount;

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

		contactsAdapter = new ContactsAdapter(this, new ContactFaceTask(this));
		contactsListView.setAdapter(contactsAdapter);

		showAccountDialog();

		contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// final Cursor cursor = contactsAdapter.getCursor();
				// cursor.moveToPosition(position);

				// Creates a contact lookup Uri from contact ID and lookup_key
				// final long contactId = cursor.getLong(cursor.getColumnIndex(Contacts._ID));
				// final String contactLookupKey = cursor.getString(cursor.getColumnIndex(Contacts.LOOKUP_KEY));
				// final Uri uri = Contacts.getLookupUri(contactId, contactLookupKey);
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

		// TODO: Allow to display contacts from custom groups chosen by user.
		if (id == QueryContactsInGroup.QUERY_ID) {

			contactsListView.setEmptyView(progressBar);
			int groupId = getMyContactsGroupId();
			return new CursorLoader(getApplication(), QueryContactsInGroup.CONTENT_URI,
					QueryContactsInGroup.PROJECTION, QueryContactsInGroup.SELECTION + " = ?", new String[] { ""
							+ groupId }, QueryContactsInGroup.SORT_ORDER);
		}

		Log.d(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
		progressBar.setVisibility(View.GONE);
		return null;
	}

	/**
	 * Get "_id" of My Contacts group.
	 * 
	 * @return int with id of My Contacts group or -1 when not found such a group
	 */
	private int getMyContactsGroupId() {
		Cursor myContactsGroupCursor = getContentResolver().query(QueryGroupMyContacts.CONTENT_URI,
				QueryGroupMyContacts.PROJECTION, null, null, null);

		int myContactsGroupsId = -1;
		String sourceId = "";
		try {
			while (myContactsGroupCursor.moveToNext()) {
				sourceId = myContactsGroupCursor.getString(QueryGroupMyContacts.GROUP_SOURCE_ID);
				if (sourceId.equals(MY_CONTACTS_GROUP_ID)) {
					myContactsGroupsId = myContactsGroupCursor.getInt(QueryGroupMyContacts.ID);
					// we have My Contacts group id, don't search further
					break;
				}
			}
		} finally {
			myContactsGroupCursor.close();
		}
		return myContactsGroupsId;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		header.setVisibility(View.VISIBLE);
		contactsAdapter.swapCursor(cursor);
		// testCursor(cursor);
	}

	/** Simple method to check cursor structure and data */
	@SuppressWarnings("unused")
	private void testCursor(Cursor cursor) {
		progressBar.setVisibility(View.GONE);
		Log.d(TAG, "results: " + cursor.getCount() + ", cols: " + cursor.getColumnCount());
		int n = 0;
		while (cursor.moveToNext()) {
			Log.d(TAG, "===========================================>");
			if (n == 5)
				break;
			for (int i = 0; i < cursor.getColumnCount(); i++) {
				Log.d(TAG, "" + cursor.getColumnName(i) + "[" + cursor.getColumnIndex(cursor.getColumnName(i)) + "]"
						+ " : " + cursor.getString(i));
			}
			n++;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		contactsAdapter.swapCursor(null);
	}

	/** cursor query parameters for "My Contacts" group */
	public interface QueryGroupMyContacts {
		static final int QUERY_ID = 2;
		static final Uri CONTENT_URI = Groups.CONTENT_URI.buildUpon()
				.appendQueryParameter(Groups.ACCOUNT_TYPE, ACCOUNT_TYPE)
				.appendQueryParameter(Groups.ACCOUNT_NAME, selectedAccount).build();
		/** List of column to return in cursor */
		static final String[] PROJECTION = { Groups._ID, Groups.SOURCE_ID };
		/** Selection allows to restrict results to defined criteria */
		static final String SELECTION = null;
		static final String SORT_ORDER = null;

		static final int ID = 0;
		static final int GROUP_SOURCE_ID = 1;
	}

	/** Cursor query parameters for 'contacts in chosen group' */
	public interface QueryContactsInGroup {
		static final int QUERY_ID = 1;
		static final Uri CONTENT_URI = Data.CONTENT_URI.buildUpon()
				.appendQueryParameter(Groups.ACCOUNT_TYPE, ACCOUNT_TYPE)
				.appendQueryParameter(Groups.ACCOUNT_NAME, selectedAccount).build();
		/** List of column to return in cursor */
		static final String[] PROJECTION = { GroupMembership._ID, GroupMembership.CONTACT_ID,
				GroupMembership.DISPLAY_NAME };
		/** Restrict results for contacts with specified group */
		static final String SELECTION = GroupMembership.GROUP_ROW_ID;
		static final String SORT_ORDER = Data.SORT_KEY_PRIMARY;

		static final int ID = 1;
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
		selectedAccount = (String) accountNames[accountEmail];
		getSupportLoaderManager().initLoader(QueryContactsInGroup.QUERY_ID, null, this);
	}

	@Override
	public void onNegativeButtonClick() {
		// no account chosen, exit app
		Toast.makeText(this, "Exiting...", Toast.LENGTH_SHORT).show();
		this.finish();
	}

}
