package org.graytin.jenkins.jenkins;

import org.eclipse.jface.fieldassist.IContentProposal;

public class Job implements IContentProposal {

	final String name;

	final String url;

	final String color;

	public Job(String name, String url, String color) {
		this.name = name;
		this.color = color;
		this.url = url;
	}

	@Override
	public String getContent() {
		return name;
	}

	@Override
	public int getCursorPosition() {
		return 0;
	}

	@Override
	public String getLabel() {
		return name + "  ( " + color + " )";
	}

	@Override
	public String getDescription() {
		return url;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		Job other = (Job) obj;
		if (color == null) {
			if (other.color != null)
				return false;
		} else if (!color.equals(other.color))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}
