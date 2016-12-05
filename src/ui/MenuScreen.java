package ui;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;

import holder.ConfigOption;
import holder.IRenderable;
import holder.InputHolder;
import holder.RenderableHolder;
import holder.ThreadHolder;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.Main;
import model.BackGround;
import model.Gun;
import model.MenuText;
import model.OptionText;

public class MenuScreen extends StackPane{
	
	public static final MenuScreen instance = new MenuScreen();
	private Canvas canvas;
	private GraphicsContext gc;
	private Font font = Font.font("Cloud", FontWeight.LIGHT, 40);
	
	public MenuScreen(){
		this.canvas = new Canvas(ConfigOption.width,ConfigOption.height);
		gc = this.canvas.getGraphicsContext2D();
		
		//BackGround
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, ConfigOption.width, ConfigOption.height);
		
		// still can't blink
		ThreadHolder.instance.add(new Thread(new Runnable() {
			@Override
			public void run(){
				while(Main.instance.getScene()=="menuScene"){
					for(int i=RenderableHolder.instance.getEntities().size()-1;i>-1;i--){
						if(RenderableHolder.instance.getEntities().get(i).isFocused()){
							((MenuText) RenderableHolder.instance.getEntities().get(i)).drawFocus(gc);
						}
						else RenderableHolder.instance.getEntities().get(i).draw(gc);
						try {
							Thread.sleep(17);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}));
		ThreadHolder.instance.add(new Thread(new Runnable() {
			@Override
			public void run(){
				RenderableHolder.instance.removeAll();
				initializeOptionScreen();
				while(Main.instance.getScene()=="optionScene"){
					for(int i=RenderableHolder.instance.getEntities().size()-1;i>-1;i--){
						if(RenderableHolder.instance.getEntities().get(i).isFocused()){
							((OptionText) RenderableHolder.instance.getEntities().get(i)).drawFocus(gc);
						}
						else RenderableHolder.instance.getEntities().get(i).draw(gc);
						try {
							Thread.sleep(17);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}));
		this.getChildren().add(canvas);
	}
	
	public void initailizeHomeMenu(){
		//Paint Home Menu
		gc.setFont(font);
		this.gc.setFill(Color.WHITE);
		RenderableHolder.instance.add(new MenuText("START",0,gc));
		RenderableHolder.instance.add(new MenuText("OPTION",1,gc));
		RenderableHolder.instance.add(new MenuText("EXIT",2,gc));
		RenderableHolder.instance.add(new Gun(1000,600));
		
		//Event Handler Hovering Menu
		this.canvas.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				// TODO Auto-generated method stub
				InputHolder.mouseOnScreen = true;
			}
		});
		
		this.canvas.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event){
				InputHolder.mouseOnScreen = false;
			}
		});
		
		this.canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event){
				if(InputHolder.mouseOnScreen){
					InputHolder.mouseX = event.getX();
					InputHolder.mouseY = event.getY();
					for(IRenderable i: RenderableHolder.instance.getEntities()){
						if(i.inHitBox() && (i instanceof MenuText || i instanceof OptionText)){
							i.setFocus(true);
							for(IRenderable j: RenderableHolder.instance.getEntities()){
								if(!i.equals(j))j.setFocus(false);
							}
						}
					}
				}
			}
		});
		
		this.canvas.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event){
				if(InputHolder.mouseOnScreen){
					InputHolder.mouseX = event.getX();
					InputHolder.mouseY = event.getY();
					
				}
			}
		});
		
		this.canvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().toString() == "PRIMARY") {
					if(InputHolder.mouseLeftDown == false){
						InputHolder.mouseLeftDownTrigger = true;
						for(int i=RenderableHolder.instance.getEntities().size()-1;i>-1;i--){
							if(RenderableHolder.instance.getEntities().get(i).isFocused()){
								String name;
								if(RenderableHolder.instance.getEntities().get(i) instanceof MenuText){
									name = ((MenuText)RenderableHolder.instance.getEntities().get(i)).getName();
								}
								else name = ((OptionText)RenderableHolder.instance.getEntities().get(i)).getName();
								//click EXIT
								if(name == "EXIT"){
									System.out.println("EXIT");
									Main.instance.getStage().close();
								}
								//click Option
								if(name == "OPTION"){
									System.out.println("OPTION");
									Main.instance.setScene("optionScene");
									ThreadHolder.instance.getThreads().get(1).start();
								}
								//Health
								if(name == "< HEALTH >"){
									System.out.println("< HEALTH >");
								}
							}
						}
						InputHolder.postUpdate();
					}
					InputHolder.mouseLeftDown = true;
				}
			}
		});
		
		this.canvas.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton().toString() == "PRIMARY") {
					InputHolder.mouseLeftDown = false;
				}
			}
		});
	}
	
	public void initializeOptionScreen(){
		//BackGround
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, ConfigOption.width, ConfigOption.height);
		gc.setFont(font);
		this.gc.setFill(Color.WHITE);
		RenderableHolder.instance.add(new OptionText("< HEALTH >",ConfigOption.health+"",0,gc));
		RenderableHolder.instance.add(new OptionText("< DIFICULTY >",ConfigOption.dificulty,1,gc));
		RenderableHolder.instance.add(new OptionText("< SOUND >","10",2,gc));
	}
	
	public GraphicsContext getGc(){
		return this.gc;
	}
}
