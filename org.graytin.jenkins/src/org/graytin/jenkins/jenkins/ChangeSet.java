package org.graytin.jenkins.jenkins;

public class ChangeSet {
	public static final ChangeSet EMPTY = new ChangeSet("N/A", "?");

	String message;

	String user;

	public ChangeSet(String message, String user) {
		this.message = message;
		this.user = user;
	}

	public String getMessage() {
		return message;
	}

	public String getUser() {
		return user;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ChangeSet other = (ChangeSet) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return message + " (" + user + ")";
	}

}