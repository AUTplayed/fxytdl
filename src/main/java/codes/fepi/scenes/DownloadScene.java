package codes.fepi.scenes;

import codes.fepi.FxApp;
import codes.fepi.Video;
import codes.fepi.controls.CallbackButton;
import codes.fepi.controls.ControlCell;
import codes.fepi.core.YTDL;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DownloadScene extends AbstractScene {

	private ObservableList<Video> videoList;
	private TableView<Video> videoTable;
	private DoubleProperty progressValue;
	private StringProperty playlistText;

	public DownloadScene(Stage stage) {
		super(stage);
		stage.setWidth(900);
		stage.setHeight(600);
	}

	@Override
	public void start() {
		Node playlistInput = createPlaylistInput();

		videoTable = createVideoTable();
		styleVideoTable(videoTable);
		videoList = videoTable.getItems();

		ProgressBar progressBar = createProgressBar();
		styleProgressBar(progressBar);

		Node buttons = createButtonsBar();
		root.getChildren().addAll(playlistInput, videoTable, progressBar, buttons);

		/*for (int i = 0; i < 20; i++) {
			boolean random = ((int) (Math.random() * 100)) % 2 == 0;
			videoList.add(new Video(UUID.randomUUID().toString(), "https://youtube.com", random, random));
		}*/
	}

	private void download() {
		List<Video> toDownload = videoList.stream().filter(Video::isDownload).collect(Collectors.toList());
	}

	private Node createPlaylistInput() {
		TextField textField = new TextField();
		textField.setPromptText("your playlist url");
		playlistText = textField.textProperty();
		anchorNode(textField, null, 100d, null, 0d);

		Button checkPlaylist = new CallbackButton("check playlist", this::checkPlaylist);
		anchorNode(checkPlaylist, null, 0d, null, null);

		AnchorPane box = new AnchorPane(textField, checkPlaylist);
		anchorNode(box, 10d, 10d, null, 10d);
		return box;
	}

	private void checkPlaylist(Node button) {
		button.setDisable(true);
		progressValue.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		YTDL.checkPlaylist(URI.create(playlistText.get()), videos -> {
			Platform.runLater(() -> {
				videoList.clear();
				videoList.addAll(videos);
				resizeColumns(videoTable);
				button.setDisable(false);
				progressValue.setValue(0);
			});
		}, e -> {
			Platform.runLater(() -> {
				Alert error = new Alert(Alert.AlertType.ERROR, e.getMessage(), ButtonType.CLOSE);
				error.show();
			});
		});
	}

	private Node createButtonsBar() {
		Button back = new CallbackButton("back", () -> FxApp.changeScene(SystemScene.class));
		Button checkAll = new CallbackButton("check all", () -> videoList.forEach(video -> video.setDownload(true)));
		Button uncheckAll = new CallbackButton("uncheck all", () -> videoList.forEach(video -> video.setDownload(false)));
		Button download = new CallbackButton("download", this::download);
		HBox hBox = new HBox(back, checkAll, uncheckAll, download);
		hBox.setSpacing(10);
		hBox.setAlignment(Pos.CENTER);
		anchorNode(hBox, null, 10d, 10d, 10d);
		return hBox;
	}

	private void styleProgressBar(ProgressBar progressBar) {
		anchorNode(progressBar, null, 10d, 40d, 10d);
	}

	private ProgressBar createProgressBar() {
		ProgressBar progressBar = new ProgressBar(0);
		progressValue = progressBar.progressProperty();
		return progressBar;
	}

	private TableView<Video> createVideoTable() {
		TableView<Video> videoTable = new TableView<>();
		addSimpleColumns(videoTable, "title");
		addColumn(videoTable, "download", CheckBoxTableCell::new);
		addColumn(videoTable, "url", () -> new ControlCell<Video, String>(url -> {
			Hyperlink hyperlink = new Hyperlink(url);
			hyperlink.setOnAction(event -> FxApp.hostServices.showDocument("https://youtube.com/watch?v=" + url));
			return hyperlink;
		}));
		addColumn(videoTable, "inProgress", () -> new ControlCell<Video, Boolean>(inProgress -> {
			if (inProgress != null && inProgress) {
				ProgressIndicator progress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
				progress.setPrefWidth(10);
				progress.setPrefHeight(10);
				return progress;
			}
			return null;
		}));


		videoTable.setEditable(true);
		return videoTable;
	}

	private void styleVideoTable(TableView<Video> videos) {
		videos.setPrefWidth(600);
		anchorNode(videos, 40d, 10d, 70d, 10d);
	}

	private <S, T> void addSimpleColumns(TableView<S> table, String... properties) {
		for (String property : properties) {
			TableColumn<S, T> col = new TableColumn<>(property);
			col.setCellValueFactory(new PropertyValueFactory<>(property));
			table.getColumns().add(col);
		}
	}

	private <S, T> void addColumn(TableView<S> table, String property, Supplier<TableCell<S, T>> cellSupplier) {
		TableColumn<S, T> col = new TableColumn<>(property);
		col.setCellValueFactory(new PropertyValueFactory<>(property));
		col.setCellFactory(tc -> cellSupplier.get());
		table.getColumns().add(col);
	}

	/**
	 * WARNING, EXPENSIVE
	 */
	private void resizeColumns(TableView tableView) {
		try {
			Method columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
			columnToFitMethod.setAccessible(true);
			for (Object column : tableView.getColumns()) {
				columnToFitMethod.invoke(tableView.getSkin(), column, -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
