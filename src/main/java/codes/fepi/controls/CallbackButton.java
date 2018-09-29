package codes.fepi.controls;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CallbackButton extends Button {
	public CallbackButton(String text, EventHandler<MouseEvent> handler) {
		super(text);
		this.setOnMouseClicked(handler);
	}

	public CallbackButton(String text, Runnable handler) {
		super(text);
		this.setOnMouseClicked(event -> handler.run());
	}

	public CallbackButton(String text, Consumer<Button> handler) {
		super(text);
		this.setOnMouseClicked(event -> handler.accept(this));
	}

	public CallbackButton(String text, BiConsumer<Button, Object[]> handler, Object... args) {
		super(text);
		this.setOnMouseClicked(event -> handler.accept(this, args));
	}
}
