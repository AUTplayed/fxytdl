package codes.fepi.core;

public enum AudioFormat {
	best,
	aac,
	flac,
	mp3,
	m4a,
	opus,
	vorbis,
	wav;

	@Override
	public String toString() {
		return this.name();
	}
}
