package codes.fepi.scenes;

import codes.fepi.FxApp;
import codes.fepi.controls.CallbackButton;
import codes.fepi.core.LibraryUpdater;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class SystemScene extends AbstractScene {

	private DoubleProperty progress;

	public SystemScene(Stage stage) {
		super(stage);
		stage.setHeight(300);
		stage.setWidth(300);
	}

	@Override
	public void start() {
		Button updateYTDL = new CallbackButton("update ytdl", this::updateYTDL);
		Button updateFFMPEG = new CallbackButton("update ffmpeg", this::updateFFMPEG);
		HBox updateBox = new HBox(updateYTDL, updateFFMPEG);
		updateBox.setSpacing(20);
		updateBox.setAlignment(Pos.CENTER);
		Button download = new CallbackButton("download", () -> FxApp.changeScene(DownloadScene.class));
		HBox downloadBox = new HBox(download);
		downloadBox.setAlignment(Pos.CENTER);
		VBox holder = new VBox(updateBox, downloadBox);
		holder.setAlignment(Pos.CENTER);
		holder.setSpacing(20);
		anchorNode(holder, 10d, 10d, 40d, 10d);

		ProgressBar progressBar = new ProgressBar(0);
		anchorNode(progressBar, null, 10d, 10d, 10d);
		progress = progressBar.progressProperty();

		root.getChildren().addAll(holder, progressBar);
	}

	private void updateYTDL(Node button) {
		progress.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		button.setDisable(true);
		LibraryUpdater.updateYTDL((exception) -> alertStatus(button, exception));
	}

	private void updateFFMPEG(Node button) {
		progress.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		button.setDisable(true);
		LibraryUpdater.updateFFMPEG((exception) -> alertStatus(button, exception));
	}

	private void alertStatus(Node button, Exception exception) {
		Platform.runLater(() -> {
			progress.setValue(0);
			button.setDisable(false);
			if (exception != null) {
				Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage(), ButtonType.CLOSE);
				alert.show();
			} else {
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully finished", ButtonType.OK);

				alert.setHeaderText(((Button) button).getText());
				alert.show();
			}
		});
	}
}
