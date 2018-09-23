package codes.fepi.scenes;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

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

}
