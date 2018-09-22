package codes.fepi.scenes;

import codes.fepi.FxApp;
import codes.fepi.Video;
import codes.fepi.controls.ControlCell;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.function.Supplier;

public class VideoScene extends AbstractScene {

	public VideoScene(Stage stage) {
		super(stage);
		stage.setWidth(600);
		stage.setHeight(600);
	}

	@Override
	public void start() {
		TableView<Video> videos = createVideoTable();
		videos.setPrefWidth(600);
		root.getChildren().add(videos);
		AnchorPane.setLeftAnchor(videos, 10.0);
		AnchorPane.setBottomAnchor(videos, 10.0);
		AnchorPane.setTopAnchor(videos, 10.0);
		AnchorPane.setRightAnchor(videos, 10.0);
		for (int i = 0; i < 20; i++) {
			boolean random = ((int) (Math.random() * 100)) % 2 == 0;
			videos.getItems().add(new Video(UUID.randomUUID().toString(), "https://youtube.com", random, random));
		}
	}

	private TableView<Video> createVideoTable() {
		TableView<Video> videoTable = new TableView<>();
		addSimpleColumns(videoTable, "title");
		addColumn(videoTable, "download", CheckBoxTableCell::new);
		addColumn(videoTable, "url", () -> new ControlCell<Video, String>(url -> {
			Hyperlink hyperlink = new Hyperlink(url);
			hyperlink.setOnAction(event -> FxApp.hostServices.showDocument(url));
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
}
