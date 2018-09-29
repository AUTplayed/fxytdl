package codes.fepi.scenes;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class AbstractScene {

	Scene scene;
	AnchorPane root;
	Stage stage;

	public AbstractScene(Stage stage) {
		root = new AnchorPane();
		scene = new Scene(root);
		this.stage = stage;
		stage.setScene(scene);
	}

	public void start() {

	}

	void anchorNode(Node node, Double top, Double right, Double bottom, Double left) {
		if (top != null) {
			AnchorPane.setTopAnchor(node, top);
		}
		if (right != null) {
			AnchorPane.setRightAnchor(node, right);
		}
		if (bottom != null) {
			AnchorPane.setBottomAnchor(node, bottom);
		}
		if (left != null) {
			AnchorPane.setLeftAnchor(node, left);
		}
	}

	void alertException(Exception e) {
		ButtonType copy = new ButtonType("copy & close");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		Alert error = new Alert(Alert.AlertType.ERROR, String.format("%s\n%s", sw.toString(), e.getMessage()), ButtonType.CLOSE, copy);
		error.setResizable(true);
		error.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
		ButtonType buttonType = error.showAndWait().orElse(ButtonType.CLOSE);
		if (buttonType.equals(copy)) {
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(e.getMessage());
			Clipboard.getSystemClipboard().setContent(clipboardContent);
		}
	}

}
