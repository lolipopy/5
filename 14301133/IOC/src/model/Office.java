package model;

public class Office {

	private String officeId;

	public Office(String id) {
		this.officeId = id;
	}

	public String getOfficeId() {
		return officeId;
	}

	public void setOfficeId(String officeId) {
		this.officeId = officeId;
	}

	public String toString() {
		return "No." + this.officeId;
	}

}
