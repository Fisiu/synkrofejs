package pl.fidano.android.synkrofejs;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	Log.i(getClass().getSimpleName(), "onCreate()");

	TextView headerImgSystem = (TextView) findViewById(R.id.headerImgSystem);
	TextView headerImgFacebook = (TextView) findViewById(R.id.headerImgFacebook);

	final ListView lv = (ListView) findViewById(R.id.contactsList);

	String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
	Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null,
		null, sortOrder);
	adapter = new SimpleCursorAdapter(this, R.layout.contact_item, cursor, new String[] {
		ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME,
		ContactsContract.Contacts._ID }, new int[] { R.id.contactName, R.id.contactDetails, R.id.contactID }, 0);
	lv.setAdapter(adapter);
	lv.setOnItemClickListener(new OnItemClickListener() {

	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) lv.getItemAtPosition(position);
		String name = cursor.getString(cursor.getColumnIndex("display_name"));
		Toast.makeText(getBaseContext(), name, Toast.LENGTH_SHORT).show();
	    }
	});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	// Inflate the menu; this adds items to the action bar if it is present.
	getMenuInflater().inflate(R.menu.main, menu);
	return true;
    }
}
