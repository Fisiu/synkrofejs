package pl.fidano.android.synkrofejs.model;

public class SimpleContact {

	private long id;
	private String name;
	private String number;
	private String email;
	private String photoPath;

	public SimpleContact(long id, String name, String number, String email, String photoPath) {
		this.id = id;
		this.name = name;
		this.number = number;
		this.email = email;
		this.photoPath = photoPath;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhotoPath() {
		return photoPath;
	}

	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}

}
