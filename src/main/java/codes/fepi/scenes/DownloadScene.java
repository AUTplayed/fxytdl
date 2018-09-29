package codes.fepi.scenes;

import codes.fepi.FxApp;
import codes.fepi.controls.CallbackButton;
import codes.fepi.controls.ControlCell;
import codes.fepi.core.AudioFormat;
import codes.fepi.core.PlaylistStatus;
import codes.fepi.core.YTDL;
import codes.fepi.entities.Video;
import codes.fepi.global.Properties;
import com.sun.javafx.scene.control.skin.TableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DownloadScene extends AbstractScene {

	private FilteredList<Video> videoList;
	private TableView<Video> videoTable;
	private DoubleProperty progressValue;
	private StringProperty playlistText;
	private Label countLabel;
	private SelectionModel<AudioFormat> selectedAudioFormat;

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
		videoList = new FilteredList<>(videoTable.getItems());
		videoTable.setItems(videoList);
		Node searchBox = createSearchBox();
		ProgressBar progressBar = createProgressBar();
		styleProgressBar(progressBar);

		Node buttons = createButtonsBar();
		root.getChildren().addAll(playlistInput, searchBox, videoTable, progressBar, buttons);
	}

	private void download(Button button) {
		button.setDisable(true);
		progressValue.setValue(0);
		ObservableList<Video> toDownload = videoList.getSource().stream().filter(Video::isDownload).collect(Collectors.toCollection(FXCollections::observableArrayList));
		toDownload.get(0).setInProgress(true);
		AtomicInteger downloaded = new AtomicInteger(0);
		List<Video> succVideos = new ArrayList<>(toDownload.size());
		YTDL.downloadVideos(toDownload, selectedAudioFormat.getSelectedItem(), (video, exception) -> {
			Platform.runLater(() -> {
				downloaded.incrementAndGet();
				video.setInProgress(false);
				progressValue.setValue(downloaded.get() / (double) toDownload.size());
				if (exception != null) {
					alertException(exception);
				} else {
					succVideos.add(video);
					video.setDownload(false);
				}
			});
		}, () -> {
			try {
				// clear temp files that are left behind after download failure
				File[] files = Properties.getOutputPath().toFile().listFiles((dir, name) -> name.endsWith(".temp"));
				if (files != null) {
					for (File file : files) {
						file.delete();
					}
				}
				PlaylistStatus.updateDownloadedVideos(succVideos);
			} catch (IOException e) {
				alertException(e);
			}
			Platform.runLater(() -> {
				button.setDisable(false);
				System.out.println("finished");
			});
		});
	}

	private Node createPlaylistInput() {
		TextField textField = new TextField();
		textField.setPromptText("your playlist url");
		playlistText = textField.textProperty();
		anchorNode(textField, null, 200d, null, 0d);

		Button checkPlaylist = new CallbackButton("check playlist", this::checkPlaylist, true);
		Button checkPlaylistClear = new CallbackButton("check noclear", this::checkPlaylist, false);
		anchorNode(checkPlaylist, null, 100d, null, null);
		anchorNode(checkPlaylistClear, null, 0d, null, null);

		AnchorPane box = new AnchorPane(textField, checkPlaylist, checkPlaylistClear);
		anchorNode(box, 10d, 10d, null, 10d);
		return box;
	}

	private void checkPlaylist(Button button, Object[] args) {
		boolean clear = (boolean) args[0];
		button.setDisable(true);
		progressValue.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		YTDL.checkPlaylist(URI.create(playlistText.get()), videos -> {
			Platform.runLater(() -> {
				if (clear) {
					videoList.getSource().clear();
				}
				((ObservableList<Video>) videoList.getSource()).addAll(videos);
				resizeColumns(videoTable);
				button.setDisable(false);
				progressValue.setValue(0);
				countLabel.setText(String.format("%d/%d", videoList.size(), videoList.getSource().size()));
			});
		}, e -> {
			Platform.runLater(() -> {
				alertException(e);
				button.setDisable(false);
				progressValue.setValue(0);
			});
		});
	}

	private Node createButtonsBar() {
		Button back = new CallbackButton("back", () -> FxApp.changeScene(SystemScene.class));
		Button checkAll = new CallbackButton("check all", () -> videoList.forEach(video -> video.setDownload(true)));
		Button uncheckAll = new CallbackButton("uncheck all", () -> videoList.forEach(video -> video.setDownload(false)));
		Button download = new CallbackButton("download", this::download);
		ComboBox<AudioFormat> formatComboBox = new ComboBox<>();
		formatComboBox.getItems().addAll(AudioFormat.values());
		selectedAudioFormat = formatComboBox.getSelectionModel();
		selectedAudioFormat.select(AudioFormat.aac);
		HBox hBox = new HBox(back, checkAll, uncheckAll, download, formatComboBox);
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

	private Node createSearchBox() {
		countLabel = new Label();
		countLabel.setFont(Font.font(18));
		anchorNode(countLabel, null, 0d, null, null);
		TextField textField = new TextField();
		textField.setPromptText("search");
		textField.setOnKeyReleased(event -> {
			videoList.setPredicate(video -> video.getTitle().toLowerCase().contains(textField.textProperty().get()));
			countLabel.setText(String.format("%d/%d", videoList.size(), videoList.getSource().size()));
		});
		anchorNode(textField, null, 100d, null, 0d);

		AnchorPane anchorPane = new AnchorPane(textField, countLabel);
		anchorNode(anchorPane, 40d, 10d, null, 10d);
		return anchorPane;
	}

	private TableView<Video> createVideoTable() {
		TableView<Video> videoTable = new TableView<>();
		addSimpleColumns(videoTable, "title");
		addColumn(videoTable, "download", CheckBoxTableCell::new);
		addColumn(videoTable, "url", () -> new ControlCell<Video, String>(url -> {
			Hyperlink hyperlink = new Hyperlink(url);
			hyperlink.setOnAction(event -> FxApp.hostServices.showDocument(Properties.ytBaseUrl + url));
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

		videoTable.setRowFactory(tv -> {
			TableRow<Video> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					Video video = row.getItem();
					video.setDownload(!video.isDownload());
				}
			});
			return row;
		});
		videoTable.setEditable(true);
		return videoTable;
	}

	private void styleVideoTable(TableView<Video> videos) {
		videos.setPrefWidth(600);
		anchorNode(videos, 70d, 10d, 70d, 10d);
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
