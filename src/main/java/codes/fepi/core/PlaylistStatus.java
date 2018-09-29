package codes.fepi.core;

import codes.fepi.entities.Video;
import codes.fepi.global.Properties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PlaylistStatus {
	private static Path playlist = Properties.getLibPath().resolve("playlist");

	private static List<Video> getDownloadedVideos() throws IOException {
		if (!playlist.toFile().exists()) {
			return new ArrayList<>();
		}
		List<String> lines = getPlaylistLines();
		ArrayList<Video> videos = new ArrayList<>(lines.size() / 2);
		for (int i = 0; i < lines.size(); i += 2) {
			videos.add(new Video(lines.get(i), lines.get(i + 1), false));
		}
		return videos;
	}

	private static List<String> getPlaylistLines() throws IOException {
		List<String> lines = Files.readAllLines(playlist);
		if (lines.size() % 2 != 0) {
			lines.remove(lines.size() - 1);
		}
		return lines;
	}

	public static void updateDownloadStatus(List<Video> videos) throws IOException {
		List<Video> downloadedVideos = getDownloadedVideos();
		for (Video video : videos) {
			if (downloadedVideos.contains(video)) {
				video.setDownload(false);
			}
		}
	}

	public static void updateDownloadedVideos(List<Video> downloaded) throws IOException {
		if (!playlist.toFile().exists()) {
			playlist.toFile().createNewFile();
		}
		List<String> playlistLines = getPlaylistLines();
		for (Video video : downloaded) {
			playlistLines.add(video.getTitle());
			playlistLines.add(video.getUrl());
		}
		Files.write(playlist, playlistLines);
	}
}
