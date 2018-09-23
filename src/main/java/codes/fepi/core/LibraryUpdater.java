package codes.fepi.core;

import codes.fepi.FxApp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.function.Consumer;

public class LibraryUpdater {

	public static void updateYTDL(Consumer<Exception> finished) {
		Thread updaterThread = new Thread(() -> {
			try {
				URL url = getLatestReleaseUrl("rg3", "youtube-dl", "youtube-dl");
				URLConnection req = Objects.requireNonNull(url).openConnection();
				InputStream is = req.getInputStream();
				Path jarPath = Paths.get(FxApp.class.getProtectionDomain().getCodeSource().getLocation().toURI());
				Path ytdl = jarPath.getParent().resolve("ytdl");
				File file = new File(ytdl.toUri());
				FileOutputStream fo = new FileOutputStream(file);
				System.out.println(file.getAbsolutePath());
				byte[] buffer = new byte[4096];
				int len;
				while ((len = is.read(buffer)) > 0) {
					fo.write(buffer, 0, len);
				}
				fo.close();
				is.close();
				finished.accept(null);
			} catch (Exception e) {
				finished.accept(e);
			}
		});
		updaterThread.start();
	}

	public static void updateFFMPEG(Consumer<Exception> finished) {
		finished.accept(new Exception("Not implemented yet, please download manually and put the binary in the same folder as this jar"));
	}

	private static URL getLatestReleaseUrl(String author, String repo, String target) throws Exception {
		URL url = new URL(String.format("https://api.github.com/repos/%s/%s/releases/latest", author, repo));
		HttpURLConnection req = (HttpURLConnection) url.openConnection();
		req.setRequestMethod("GET");
		req.setRequestProperty("content-type", "application/json");
		req.setRequestProperty("accept", "application/json");
		BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
		req.connect();
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		String res = sb.toString();
		JSONObject json = new JSONObject(res);
		JSONArray assets = json.getJSONArray("assets");
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			target += ".exe";
		}
		for (int i = 0; i < assets.length(); i++) {
			JSONObject asset = assets.getJSONObject(i);
			String name = asset.getString("name");
			if (target.equals(name)) {
				String downloadUrl = asset.getString("browser_download_url");
				return new URL(downloadUrl);
			}
		}
		return null;
	}
}
