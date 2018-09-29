package codes.fepi.entities;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Video {
	private String title;
	private String url;
	private BooleanProperty download;
	private BooleanProperty inProgress;

	public Video(String title, String url, boolean download) {
		this(title, url, download, false);
	}

	public Video(String title, String url, boolean download, boolean inProgress) {
		this.title = title;
		this.url = url;
		this.download = new SimpleBooleanProperty(download);
		this.inProgress = new SimpleBooleanProperty(false);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isDownload() {
		return download.get();
	}

	public BooleanProperty downloadProperty() {
		return download;
	}

	public void setDownload(boolean download) {
		this.download.set(download);
	}

	public boolean isInProgress() {
		return inProgress.get();
	}

	public BooleanProperty inProgressProperty() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress.set(inProgress);
	}

	@Override
	public String toString() {
		return "Video{" +
				"title='" + title + '\'' +
				", url='" + url + '\'' +
				", download=" + download +
				", inProgress=" + inProgress +
				'}';
	}

	@Override
	public boolean equals(Object obj) {
		return this.url.equals(((Video) obj).getUrl());
	}
}
