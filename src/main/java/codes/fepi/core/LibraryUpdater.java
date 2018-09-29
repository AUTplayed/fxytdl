package codes.fepi.core;

import codes.fepi.global.Properties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LibraryUpdater {

	public static void updateYTDL(Consumer<Exception> finished) {
		Thread updaterThread = new Thread(() -> {
			try {
				URL url = getLatestReleaseUrl("rg3", "youtube-dl", "youtube-dl");
				downloadFile("ytdl", url);
				finished.accept(null);
			} catch (Exception e) {
				finished.accept(e);
			}
		});
		updaterThread.start();
	}

	public static void updateFFMPEG(Consumer<Exception> finished) {
		Thread updaterThread = new Thread(() -> {
			if (!Properties.win) {
				finished.accept(new Exception("nix* os detected, please use a package manager of your choosing to download ffmpeg"));
				return;
			}
			try {
				Document document = Jsoup.connect("http://ffmpeg.org/download.html#build-windows").get();
				Element windowsBuildLink = document.selectFirst("#build-windows").selectFirst("a");
				String dlSite = windowsBuildLink.attr("abs:href");
				Document dlSiteDoc = Jsoup.connect(dlSite).get();
				Elements versions = dlSiteDoc.select("input[name=v]");
				String stableVersion = versions.stream().filter(v -> v.parent().attr("title").toLowerCase().contains("release")).findAny().get().val();
				// hardcoded win64, fuck 32
				String downloadLink = String.format("%s/win64/static/ffmpeg-%s-win64-static.zip", dlSiteDoc.location(), stableVersion);
				String zipName = "ffmpeg.zip";
				downloadFile(zipName, new URL(downloadLink));
				unzipArchive(zipName);
				Properties.getTempPath().resolve(zipName).toFile().delete();
				finished.accept(null);
			} catch (Exception e) {
				e.printStackTrace();
				finished.accept(e);
			}
			//finished.accept(new Exception("Not implemented yet, please download manually and put the binary in the same folder as this jar"));
		});
		updaterThread.start();
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
		if (Properties.win) {
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

	private static void downloadFile(String name, URL url) throws IOException {
		URLConnection req = Objects.requireNonNull(url).openConnection();
		req.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
		InputStream is = req.getInputStream();
		Path filepath = Properties.getTempPath().resolve(name);
		File file = new File(filepath.toUri());
		FileOutputStream fo = new FileOutputStream(file);
		byte[] buffer = new byte[4096];
		int len;
		while ((len = is.read(buffer)) > 0) {
			fo.write(buffer, 0, len);
		}
		fo.close();
		is.close();
		System.out.println("downloaded: " + file.getAbsolutePath());
	}

	private static void unzipArchive(String archiveName) throws IOException {
		byte[] buffer = new byte[1024];
		ZipInputStream zis = new ZipInputStream(new FileInputStream(Properties.getTempPath().resolve(archiveName).toFile()));
		ZipEntry zipEntry = zis.getNextEntry();
		while (zipEntry != null) {
			String fileName = zipEntry.getName();
			// only extract files in bin folder
			String binRegex = "/bin/";
			if (fileName.contains(binRegex)) {
				fileName = fileName.replaceFirst(".*" + binRegex, "");
				if (!fileName.isEmpty()) {
					File extractedFile = Properties.getTempPath().resolve(fileName).toFile();
					FileOutputStream fos = new FileOutputStream(extractedFile);
					int len;
					while ((len = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, len);
					}
					fos.close();
					System.out.println("extracted: " + extractedFile.getAbsolutePath());
				}
			}
			zipEntry = zis.getNextEntry();
		}
		zis.closeEntry();
		zis.close();
	}
}
