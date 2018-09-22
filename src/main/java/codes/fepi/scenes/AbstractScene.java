package codes.fepi.scenes;

import javafx.scene.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public abstract class AbstractScene {

	protected Scene scene;
	protected AnchorPane root;
	protected Stage stage;

	public AbstractScene(Stage stage) {
		root = new AnchorPane();
		scene = new Scene(root);
		this.stage = stage;
		stage.setScene(scene);
	}
	public void start() {

	}

}
