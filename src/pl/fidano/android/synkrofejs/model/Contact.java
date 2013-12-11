package pl.fidano.android.synkrofejs.model;

public class Contact {

	private int contactId;
	private String displayName;

	public int getContactId() {
		return contactId;
	}

	public void setContactId(int contactId) {
		this.contactId = contactId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Contact() {
	}

	public Contact(int contactId, String displayName) {
		this.contactId = contactId;
		this.displayName = displayName;
	}

}
