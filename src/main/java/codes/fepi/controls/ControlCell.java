package codes.fepi.controls;

import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.layout.Pane;

import java.util.function.Function;

public class ControlCell<S, T> extends TableCell<S, T> {
	private Function<T, Node> creator;

	public ControlCell(Function<T, Node> creator) {
		this.creator = creator;
	}

	@Override
	protected void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		Node node = creator.apply(item);
		setGraphic(node != null ? node : new Pane());
	}
}
