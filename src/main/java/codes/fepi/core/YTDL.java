package codes.fepi.core;

import codes.fepi.FxApp;
import codes.fepi.Video;
import codes.fepi.global.Properties;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
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
		ProcessBuilder builder = new ProcessBuilder(Properties.getYtdlPath().toString());
		Collections.addAll(builder.command(), args);
		System.out.printf("executing: %s\n", builder.command());
		Process process = builder.start();
		BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = input.readLine()) != null) {
			lineHandler.accept(line);
		}
		input.close();
	}

	public static void downloadVideos(List<Video> toDownload, BiConsumer<Video, Exception> progress, Runnable finished) {
		Thread thread = new Thread(() -> {
			ThreadPoolExecutor threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(3);
			for (Video video : toDownload) {
				threadPool.submit(() -> downloadSingleVideo(video, progress));
			}
			try {
				threadPool.shutdown();
				threadPool.awaitTermination(180, TimeUnit.MINUTES);
				finished.run();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		thread.start();
	}

	private static void downloadSingleVideo(Video toDownload, BiConsumer<Video, Exception> progress) {
		List<String> arguments = new LinkedList<>();
		Path ffmpegPath = Properties.getFfmpegPath();
		if (ffmpegPath != null) {
			Collections.addAll(arguments, "--ffmpeg-location", ffmpegPath.toString());
		}
		arguments.add("-o");
		arguments.add(Properties.getOutputPath().resolve("%(title)s.temp").toString());
		arguments.add("-x");
		arguments.add("--audio-format");
		arguments.add("aac");
		arguments.add("--restrict-filenames");
		arguments.add(Properties.ytBaseUrl + toDownload.getUrl());
		try {
			executeCommand((line) -> {
				System.out.println(line);
			}, arguments.toArray(new String[0]));
		} catch (Exception e) {
			progress.accept(toDownload, e);
			return;
		}
		progress.accept(toDownload, null);
	}
}
