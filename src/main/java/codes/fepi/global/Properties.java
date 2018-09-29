package codes.fepi.global;

import codes.fepi.FxApp;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Properties {

	private static Path folder;
	public static boolean win;
	public static String ytBaseUrl = "https://youtube.com/watch?v=";

	static {
		try {
			folder = Paths.get(FxApp.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
			win = System.getProperty("os.name").toLowerCase().contains("win");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public static Path getFfmpegPath() {
		if (win) {
			return getTempPath().resolve("ffmpeg.exe").toAbsolutePath();
		}
		return null;
	}

	public static Path getYtdlPath() {
		return getTempPath().resolve("ytdl").toAbsolutePath();
	}

	public static Path getOutputPath() {
		Path out = folder.resolve("out");
		if (!out.toFile().exists()) {
			out.toFile().mkdir();
		}
		return out;
	}

	public static Path getTempPath() {
		Path temp = folder.resolve("temp");
		if (!temp.toFile().exists()) {
			temp.toFile().mkdir();
		}
		return temp;
	}
}
