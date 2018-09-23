package codes.fepi.core;

import codes.fepi.FxApp;
import codes.fepi.Video;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class YTDL {

	public static void checkPlaylist(URI uri, Consumer<List<Video>> videosDone, Consumer<Exception> errors) {
		Thread thread = new Thread(() -> {
			try {
				List<Video> videos = new LinkedList<>();
				executeCommand((line) -> {
					JSONObject jsonVideo = new JSONObject(line);
					videos.add(new Video(jsonVideo.getString("title"), jsonVideo.getString("url"), true));
					videosDone.accept(videos);
				}, "-j", "--flat-playlist", uri.toString());
			} catch (Exception e) {
				errors.accept(e);
			}
		});
		thread.start();
	}

	private static void executeCommand(Consumer<String> lineHandler, String... args) throws Exception {
		Path jarPath = Paths.get(FxApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		Path ytdl = jarPath.getParent().resolve("ytdl");
		ProcessBuilder builder = new ProcessBuilder(ytdl.toString());
		Collections.addAll(builder.command(), args);
		System.out.printf("executing: %s", builder.command());
		Process process = builder.start();
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = input.readLine()) != null) {
			lineHandler.accept(line);
		}
		input.close();
	}
}
