package codes.fepi.scenes;

import codes.fepi.FxApp;
import codes.fepi.controls.CallbackButton;
import codes.fepi.core.LibraryUpdater;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicInteger;


public class SystemScene extends AbstractScene {

	private DoubleProperty progress;
	private AtomicInteger runningUpdaters = new AtomicInteger(0);

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
		Label title = new Label("Fx youtube downloader");
		title.setFont(Font.font(18));
		Label author = new Label("by github.com/AUTplayed");
		author.setFont(Font.font(10));
		Label hint = new Label("update both libs before your first download");
		hint.setFont(Font.font(10));
		VBox holder = new VBox(title, author, hint, updateBox, downloadBox);
		holder.setAlignment(Pos.CENTER);
		holder.setSpacing(20);
		anchorNode(holder, 10d, 10d, 40d, 10d);

		ProgressBar progressBar = new ProgressBar(0);
		anchorNode(progressBar, null, 10d, 10d, 10d);
		progress = progressBar.progressProperty();

		root.getChildren().addAll(holder, progressBar);
	}

	private void updateYTDL(Button button) {
		progress.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		button.setDisable(true);
		runningUpdaters.incrementAndGet();
		LibraryUpdater.updateYTDL((exception) -> alertStatus(button, exception));
	}

	private void updateFFMPEG(Button button) {
		progress.setValue(ProgressBar.INDETERMINATE_PROGRESS);
		button.setDisable(true);
		runningUpdaters.incrementAndGet();
		LibraryUpdater.updateFFMPEG((exception) -> alertStatus(button, exception));
	}

	private void alertStatus(Button button, Exception exception) {
		Platform.runLater(() -> {
			if (runningUpdaters.decrementAndGet() == 0) {
				progress.setValue(0);
			}
			button.setDisable(false);
			if (exception != null) {
				alertException(exception);
			} else {
				Alert alert = new Alert(Alert.AlertType.INFORMATION, "Successfully finished", ButtonType.OK);
				alert.setHeaderText(button.getText());
				alert.show();
			}
		});
	}
}
