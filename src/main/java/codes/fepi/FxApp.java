package codes.fepi;

import codes.fepi.scenes.AbstractScene;
import codes.fepi.scenes.VideoScene;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.stage.Stage;

import java.lang.reflect.InvocationTargetException;

public class FxApp extends Application {

	private static Stage stage;
	public static HostServices hostServices;

	@Override
	public void start(Stage stage) {
		FxApp.stage = stage;
		FxApp.hostServices = getHostServices();
		changeScene(VideoScene.class);
		stage.show();
	}

	public static void main(String args[]) {
		launch(args);
	}

	public static void changeScene(Class<? extends AbstractScene> scene) {
		try {
			AbstractScene abstractScene = scene.getConstructor(Stage.class).newInstance(stage);
			abstractScene.start();
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
