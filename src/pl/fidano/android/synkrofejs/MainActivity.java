package pl.fidano.android.synkrofejs;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	Log.i(getClass().getSimpleName(), "onCreate()");

	TextView label = (TextView) findViewById(R.id.firstLabel);
	label.setText("Text set during runtime :)");

	ListView lv = (ListView) findViewById(R.id.contactsList);

	Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
		null, null);
	ListAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_item, cursor, new String[] {
		ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
		ContactsContract.Contacts._ID }, new int[] { R.id.contactName, R.id.contactDetails, R.id.contactID }, 0);
	lv.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }

}
