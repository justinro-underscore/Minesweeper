package main;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

/**
 * Facilitates user interaction and handles how the game functions
 *
 * @author Justin Roderman
 * @since: August 2, 2018
 */

public class UIController extends Application
{
	@FXML private GridPane grdMap;
	@FXML private Label lblMinesLeft;
	@FXML private Label lblTime;
	@FXML private Label lblEndGame;
	@FXML private Button btnRestart;
	private int width;
	private int height;
	private int mines;
	private int[][] map; // The true map
	private SimpleStringProperty[][] dispMap; // The displayed map
	private SimpleIntegerProperty minesLeft;
	private SimpleIntegerProperty time = new SimpleIntegerProperty(0);
	private boolean firstClick = true;

	private final String FONT_STR = "-fx-font: normal bold 14px 'sans-serif'; -fx-border-color: black; -fx-border-width: 0.5;";
	private final String COLOR_STR = "-fx-text-fill: ";
	private final String BACKGROUND_STR = "-fx-background-color: ";
	private enum SurroundsMethod {
		COUNT,
		REVEAL,
		ZERO
	}
	private HashMap<Integer, String> colors;
	private Timer timer;

	/**
	 * Handles updating the time
	 */
	class UpdateTimeTask extends TimerTask
	{
        public void run() { Platform.runLater(() -> { time.set(time.get() + 1); }); }
    }

	/**
	 * Where the application launches from
	 * @param args What is passed in (don't worry about this)
	 */
	public static void main(String[] args)
	{
		launch(args);
	}

	/**
	 * Where the application launches from
	 * @throws IOException If an input or output exception occurred
	 */
	@Override
	public void start(Stage arg0) throws Exception
	{
		FXMLLoader load = new FXMLLoader(getClass().getResource("MinesweeperUI.fxml")); // You may have to change the path in order to access MinesweeperUI.fxml
		load.setController(this); // Makes it so that you can control the UI using this class

		Parent root = (Parent) load.load();
		Scene scene = new Scene(root);

		// Start the application
		Stage stage = new Stage();
		stage.setTitle("Minesweeper");
		stage.setScene(scene);
		initializeVariables();
		initializeUI();

		stage.show();
	}

	/**
	 * Sets all of the colors of the map
	 */
	private void initializeVariables()
	{
		colors = new HashMap<>();
		colors.put(1, "darkblue");
		colors.put(2, "green");
		colors.put(3, "red");
		colors.put(4, "purple");
		colors.put(5, "orange");
		colors.put(6, "aqua");
		colors.put(7, "blue");
		colors.put(8, "lightgreen");
	}

	/**
	 * Initializes all aspects of the UI
	 */
	private void initializeUI()
	{
		// TODO Switch to user controlled
		height = 16;
		width = 16;
		mines = 40;
		minesLeft = new SimpleIntegerProperty(mines);
		time = new SimpleIntegerProperty(0);

		dispMap = new SimpleStringProperty[width][height];
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				dispMap[i][j] = new SimpleStringProperty("~");
			}
		}

		final double WINDOW_HEIGHT = 540;
		final double WINDOW_WIDTH = 540;
		double cellHeight = WINDOW_HEIGHT / height;
		double cellWidth = WINDOW_WIDTH / width;
		grdMap.getColumnConstraints().clear();
		for(int i = 0; i < width; i++)
		{
			ColumnConstraints colConstrain = new ColumnConstraints(cellWidth);
			grdMap.getColumnConstraints().add(colConstrain);
		}
		grdMap.getRowConstraints().clear();
		for(int i = 0; i < height; i++)
		{
			RowConstraints rowConstrain = new RowConstraints(cellHeight);
			grdMap.getRowConstraints().add(rowConstrain);
		}

		grdMap.getChildren().clear();
		for(int i = 0; i < width; i++)
		{
			for(int j = 0; j < height; j++)
			{
				Label lbl = new Label();
				lbl.setPrefWidth(cellWidth);
				lbl.setPrefHeight(cellHeight);
				lbl.setAlignment(Pos.CENTER);
				lbl.textProperty().bind(dispMap[i][j]);
				lbl.setStyle(FONT_STR + BACKGROUND_STR + "lightgray");
				lbl.setId(i + " " + j + " ");
				grdMap.add(lbl, j, i);
			}
		}
		lblEndGame.setText("");
		grdMap.setDisable(false);

		lblMinesLeft.textProperty().bind(minesLeft.asString());
		timer = new Timer();
		lblTime.textProperty().bind(Bindings.format("%03d", time));
		initializeListeners();
	}

	/**
	 * Creates the map using the MapMaker class.
	 * Then makes it display
	 * @param y The y coordinate of the clicked cell
	 * @param x The x coordinate of the clicked cell
	 */
	private void createMap(int y, int x)
	{
		try {
			map = MapMaker.createMap(height, width, y, x, mines);
		}
		catch(RuntimeException re)
		{
			re.printStackTrace(); // TODO Make better error handling
		}

		ObservableList<Node> cells = grdMap.getChildren();
		cells.forEach(n -> {
			if(n instanceof Label)
			{
				Label lbl = (Label)n;
				String id = lbl.getId();
				int i = Integer.parseInt(id.substring(0, id.indexOf(" ")));
				int j = Integer.parseInt(id.substring(id.indexOf(" ") + 1, id.lastIndexOf(" ")));
				lbl.setId(i + " " + j + " " + map[i][j]);
			}
		});
	}

	/**
	 * Sets the functions that run when the user clicks on certain objects
	 */
	private void initializeListeners()
	{
		btnRestart.setOnAction(e -> {
			timer.cancel();
			firstClick = true;
			Platform.runLater(() -> { initializeUI(); });
		});

		ObservableList<Node> cells = grdMap.getChildren();
		cells.forEach(n -> {
			n.setOnMouseReleased(e -> {
				Label lbl = (Label)n; // So that we don't have to keep casting to Label
				String id = lbl.getId();
				int y = Integer.parseInt(id.substring(0, id.indexOf(" ")));
				int x = Integer.parseInt(id.substring(id.indexOf(" ") + 1, id.lastIndexOf(" ")));
				String cellTxt = dispMap[y][x].get();
				if(e.getButton() == MouseButton.PRIMARY)
				{
					if(cellTxt.equals("F"))
					{
						dispMap[y][x].set("?");
						minesLeft.set(minesLeft.get() + 1);
					}
					else if(cellTxt.equals("?"))
					{
						dispMap[y][x].set("F");
						lbl.setStyle(FONT_STR + COLOR_STR + "darkred;" + BACKGROUND_STR + "lightgray");
						minesLeft.set(minesLeft.get() - 1);
					}
					else // ~ or number
					{
						if(firstClick)
						{
							createMap(y, x);
					        timer.schedule(new UpdateTimeTask(), 0, 1000);
							id = lbl.getId();
							firstClick = false;
						}
						int cellVal = Integer.parseInt(id.substring(id.lastIndexOf(" ") + 1));
						if(lbl.getText().equals("~"))
						{
							if(cellVal == 0)
							{
								dispMap[y][x].set(" ");
								lbl.setStyle(FONT_STR + BACKGROUND_STR + "white");
								surroundingsMethod(SurroundsMethod.REVEAL, y, x, 0);
							}
							else if(cellVal == 9)
							{
								gameOver(y, x);
							}
							else
							{
								dispMap[y][x].set("" + cellVal);
								lbl.setStyle(FONT_STR + BACKGROUND_STR + "white;" + COLOR_STR + colors.get(cellVal));
							}
						}
						else if(cellVal > 0)
						{
							if(surroundingsMethod(SurroundsMethod.COUNT, y, x, cellVal))
								surroundingsMethod(SurroundsMethod.REVEAL, y, x, 0);
						}
					}
				}
				else
				{
					if(cellTxt.equals("F") || cellTxt.equals("?"))
					{
						if(cellTxt.equals("F"))
							minesLeft.set(minesLeft.get() + 1);
						dispMap[y][x].set("~");
						lbl.setStyle(FONT_STR + COLOR_STR + "black;" + BACKGROUND_STR + "lightgray");
					}
					else if(cellTxt.equals("~"))
					{
						dispMap[y][x].set("F");
						lbl.setStyle(FONT_STR + COLOR_STR + "darkred;" + BACKGROUND_STR + "lightgray");
						minesLeft.set(minesLeft.get() - 1);
					}
				}
				checkWin();
			});
		});
	}

	/**
	 * Checks surrounding cells and does different functions
	 * @param sm The function to run on surrounding cell
	 * @param y The y coordinate of the selected cell
	 * @param x The x coordinate of the selected cell
	 * @param cellVal The value of the selected cell
	 * @return Whether or not the player has flagged the correct amount of adjacent cells
	 */
	private boolean surroundingsMethod(SurroundsMethod sm, int y, int x, int cellVal)
	{
		int count = 0;
		int yLength = dispMap.length;
		int xLength = dispMap[0].length;
		for(int yChange = -1; yChange <= 1; yChange++)
		{
			if(y + yChange >= 0 && y + yChange < yLength) // Y-bounds check
			{
				for(int xChange = -1; xChange <= 1; xChange++)
				{
					if(x + xChange >= 0 && x + xChange < xLength) // X-bounds check
					{
						if(sm == SurroundsMethod.COUNT)
							count += (dispMap[y + yChange][x + xChange].get().equals("F") ? 1 : 0);
						else if(sm == SurroundsMethod.REVEAL)
						{
							if(!dispMap[y + yChange][x + xChange].get().equals("F") && dispMap[y + yChange][x + xChange].get().equals("~"))
							{
								int val = map[y + yChange][x + xChange];
								if(val == 9)
									gameOver((y + yChange), (x + xChange));
								else if(val == 0)
								{
									dispMap[y + yChange][x + xChange].set(" ");
									getLabel(y + yChange, x + xChange).setStyle(FONT_STR + BACKGROUND_STR + "white");
									surroundingsMethod(SurroundsMethod.REVEAL, (y + yChange), (x + xChange), 0);
								}
								else
								{
									dispMap[y + yChange][x + xChange].set("" + val);
									getLabel(y + yChange, x + xChange).setStyle(FONT_STR + BACKGROUND_STR + "white;" + COLOR_STR + colors.get(val));
								}
							}
						}
					}
				}
			}
		}
		return (count == cellVal);
	}

	/**
	 * Gets the label of the specified cell
	 * @param y The y coordinate of the specified cell
	 * @param x The x coordinate of the specified cell
	 * @return The label of the specified cell
	 */
	private Label getLabel(int y, int x)
	{
		ObservableList<Node> cells = grdMap.getChildren();
		for(int i = 0; i < cells.size(); i++)
		{
			Node n = cells.get(i);
			if(n instanceof Label)
			{
				String id = n.getId();
				int yCheck = Integer.parseInt(id.substring(0, id.indexOf(" ")));
				int xCheck = Integer.parseInt(id.substring(id.indexOf(" ") + 1, id.lastIndexOf(" ")));
				if(y == yCheck && x == xCheck)
					return (Label)n;
			}
		}
		throw new RuntimeException("Label not found");
	}

	/**
	 * Checks to see if the user has won yet
	 */
	private void checkWin()
	{
		if(minesLeft.get() != 0)
			return;
		ObservableList<Node> cells = grdMap.getChildren();
		for(int i = 0; i < cells.size(); i++)
		{
			Node n = cells.get(i);
			if(n instanceof Label && ((Label)n).getText().equals("~"))
				return;
		}

		// You win!
		grdMap.setDisable(true);
		timer.cancel();
		lblEndGame.setText("You win!\n\nYou cleared the minefield\nin " + time.get() + " seconds!");
	}

	/**
	 * Specifies what happens when the user hits a mine
	 * @param yDeadMine y coordinate of the exploded mine
	 * @param xDeadMine x coordinate of the exploded mine
	 */
	private void gameOver(int yDeadMine, int xDeadMine)
	{
		timer.cancel();
		ObservableList<Node> cells = grdMap.getChildren();
		cells.forEach(n -> {
			if(n instanceof Label)
			{
				Label lbl = (Label)n; // So that we don't have to keep casting to Label
				if(lbl.getText().equals("~") || lbl.getText().equals("F"))
				{
					String id = lbl.getId();
					int y = Integer.parseInt(id.substring(0, id.indexOf(" ")));
					int x = Integer.parseInt(id.substring(id.indexOf(" ") + 1, id.lastIndexOf(" ")));
					int cellVal = Integer.parseInt(id.substring(id.lastIndexOf(" ") + 1));
					if(cellVal == 9)
						dispMap[y][x].set("M");
					else if(lbl.getText().equals("F"))
						dispMap[y][x].set("X");
				}
			}
		});
		getLabel(yDeadMine, xDeadMine).setStyle(FONT_STR + BACKGROUND_STR + "red");
		grdMap.setDisable(true);
		lblEndGame.setText("Game Over!\n\nYou had " + minesLeft.get() + " mines left\nat " + time.get() + " seconds...");
	}
}




