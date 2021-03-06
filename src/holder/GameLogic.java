package holder;

import java.util.ArrayList;
import java.util.Random;

import com.sun.javafx.tk.Toolkit;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import main.Main;
import model.*;
import modelText.MenuText;
import modelText.StageText;
import ui.GameScreen;

public class GameLogic {
	private int score=0;
	private int health;
	private int miss=0;
	private int totalMiss=0;
	private int perfect=0;
	private AnimationTimer gameloop;
	private AnimationTimer gameOverloop;
	private GraphicsContext gc;
	private Font font = Font.font("Cloud", FontWeight.LIGHT, 30);
	private ArrayList<String> current_wave = new ArrayList<>();
	private ArrayList<String> used = new ArrayList<>();
	private int chapter = 1;
	private int hitting = 0;
	private int hitting2 =0;
	private int hit_count=0;
	private boolean gameStart = true;
	private boolean setupChapter = false;
	private boolean focusing = false;
	private boolean endChapter = false;
	private boolean gameEnding = false;
	private String name = "_";
	private AudioClip gameover = new AudioClip(ClassLoader.getSystemResource("sound/gameover.mp3").toString());
	private AudioClip gameplay = new AudioClip(ClassLoader.getSystemResource("sound/gameplay.mp3").toString());
	private AudioClip gameend = new AudioClip(ClassLoader.getSystemResource("sound/gameend.mp3").toString());
	
	public GameLogic(){
		gameloop = new AnimationTimer(){
			Long start=0l;
			int frameCount = 0;
			int wait = 0;
			@Override
			public void handle(long now) {
				long diff = now-start;
				if(diff>=1000000000l){ // 1 second
					System.out.println(frameCount);
					frameCount=0;
					start = now;
				}
				
				if(gameStart){
					gc.setFont(font);
					gameplay.play();
					if(wait==0){
						health = ConfigOption.health;
						RenderableHolder.instance.add(new MenuText("Ready",-4.5,gc));
					}
					wait++;
					if(wait>119){
						setupChapter = true;
						gameStart = false;
						//Remove Ready Text
						RenderableHolder.instance.remove(1);
						wait=0;
					}
				}
				//set Up Level
				if(setupChapter){
					for(IRenderable i: RenderableHolder.instance.getEntities()){
						if(i instanceof StageText)((StageText) i).setDestroy(true);
					}
					
					RenderableHolder.instance.add(new StageText("Chapter "+chapter,gc));
					current_wave.clear();
					addEnemies();
					removeSpace(current_wave);
					setupChapter = false;
				}
				
				// isn't focusing
				if(InputHolder.keyTriggered.size()!=0){
					if(!focusing){
						for(int i = 0;i<current_wave.size();i++){
							if(InputHolder.getLastTrigger().equals(current_wave.get(i).substring(0,1).toUpperCase()) && 
									((Enemy) RenderableHolder.instance.getEntities().get(i+3)).getX()<1300){
								focusing = true;
								hitting = i;
								hitting2 = RenderableHolder.instance.getEntities().size();
								((Enemy) RenderableHolder.instance.getEntities().get(hitting+3)).setZ(Integer.MAX_VALUE-1);
								((Enemy) RenderableHolder.instance.getEntities().get(hitting+3)).setFocus(true);
								//set focus on Enemy                              //+3 Skip Bg main bunger
								((Enemy) RenderableHolder.instance.getEntities().get(hitting+3)).hit();
								((MainCharacter) RenderableHolder.instance.getEntities().get(1)).shoot();
								current_wave.set(hitting, current_wave.get(hitting).substring(1));
								score+=5;
							}
						}
					} else if(focusing){  // already set focused Enemy
						if(InputHolder.getLastTrigger().equals(current_wave.get(hitting).substring(0,1).toUpperCase())){
							((Enemy) RenderableHolder.instance.getEntities().get(hitting2-2)).hit();
							current_wave.set(hitting, current_wave.get(hitting).substring(1));
							((MainCharacter) RenderableHolder.instance.getEntities().get(1)).shoot();
							score+=5;
							// Enemy Dead
							if(current_wave.get(hitting).equals("")){
								focusing = false;
								current_wave.remove(hitting);
								RenderableHolder.instance.remove(hitting2-2);
								if(miss==0){
									perfect++;
									if(perfect>=10)score+=50;
									else if(perfect>=5)score+=25;
									else score+=5;
								}else miss=0;
								if(current_wave.size()==0){
									chapter++;
									// Game end chapter 3
									if(chapter>=4){
										gameEnding=true;
									}
									else endChapter = true;
								}
							}
						}
						else{ //miss the shot
							totalMiss++;
							perfect=0;
							miss++;
							((Enemy) RenderableHolder.instance.getEntities().get(hitting2-2)).miss();
						}
					}
				}
				
				// Wait after all Enemys are dead.
				if(endChapter){
					wait++;
					if(wait == 120){
						wait=0;
						setupChapter = true;
						endChapter = false;
					}
				}
				if(gameEnding){
					wait++;
					if(wait==120){
						wait=0;
						gc.setGlobalAlpha(0.1);
						gc.setLineWidth(10);
						gameplay.stop();
						gameend.play();
						gameOverloop.start();
						this.stop();
					}
				}
				
				//Enemy attacking
				moveAndActtack();
				
				frameCount++;
				removeDestroyedEntities();
				RenderableHolder.instance.sort();
				paint();
				InputHolder.postUpdate();
			}
		};
		
		gameOverloop = new AnimationTimer(){
			Long start =0l;
			int count = 1;
			int frame = 0;
			int w =1;
			int h =1;
			@Override
			public void handle(long now) {
				long diff = now-start;
				if(diff>=1000000000l){
				}
				if(count>=20){
					gc.setGlobalAlpha(1);
					gc.setLineWidth(2);
					gc.setFill(Color.BLACK);
					gc.setStroke(Color.WHITE);
					//open end screen animation
					gc.fillRect(ConfigOption.width/2-w/2, ConfigOption.height/2+50-h/2, w, h);
					gc.strokeRect(ConfigOption.width/2-w/2, ConfigOption.height/2+50-h/2, w, h);
					gc.setFill(Color.WHITE);
					if(w<500){
						w+=30;
					}else if(h<300){
						h+=20;
					}else if(ConfigOption.checkHighScore(score)){
						gc.fillText("ENTER YOUR NAME", ConfigOption.width/2-135, ConfigOption.height/2-30);
						gc.fillText("SCORE : "+score, ConfigOption.width/2-90, ConfigOption.height/2+150);
						// keying name
						if(InputHolder.keyTriggered.size()!=0){
							if(InputHolder.getLastTrigger().equals("ENTER")){
								try {
									if(ConfigOption.addHighScore(name, score)){
										// save success full
										System.out.println("save highscore success");
										Main.toggleScene();
										resetGamelogic();
										w=0;h=0;count=0;
										this.stop();
									}
								} catch (NonameException e) {
									// there is no name
									this.stop();
									e.show();
									this.start();
								}
							}
							else if(InputHolder.getLastTrigger().equals("BACK_SPACE")){
								if(name.length()>2)name = name.substring(0, name.length()-3)+"_";
								else name = "_";
							}
							else if(name.length()<=30){
								name = name.substring(0, name.length()-1)+InputHolder.getLastTrigger()+" _";
							}
						}
						gc.fillText(name.substring(0,name.indexOf("_")), ConfigOption.width/2-200, ConfigOption.height/2+60);
						double name_width = Toolkit.getToolkit().getFontLoader().computeStringWidth(name, gc.getFont());
						if(count%4<=1){
							gc.fillText(name.substring(name.length()-1), ConfigOption.width/2-210+name_width, ConfigOption.height/2+60);
						}
					}else{
						// Score does not High enough
						gc.fillText("SORRY BUT YOU'RE TOO NOOB", ConfigOption.width/2-210, ConfigOption.height/2-20);
						gc.fillText("SCORE : "+score, ConfigOption.width/2-90, ConfigOption.height/2+150);
						if(count%4<=1)gc.fillText("PRESS ENTER", ConfigOption.width/2-100, ConfigOption.height/2+65);
						if(InputHolder.keyTriggered.size()!=0){
							if(InputHolder.getLastTrigger().equals("ENTER")){
								System.out.println("To Main Menu");
								Main.toggleScene();
								resetGamelogic();
								w=0;h=0;count=0;
								this.stop();
							}
						}
					}
					
				}
				if(frame > 15){
					if(count<5){
						if(gameEnding)gc.setFill(Color.GREEN);
						else gc.setFill(Color.RED);
						gc.fillRect(0, 0, ConfigOption.width, ConfigOption.height);
					}
					else if(count<20){
						gc.setStroke(Color.BLACK);
						if(gameEnding){
							gc.setFill(Color.BLUE);
							gc.setLineWidth(3);
							gc.fillText("CONGRATULATION", ConfigOption.width/2-125, ConfigOption.height/2-170);
							gc.strokeText("CONGRATULATION", ConfigOption.width/2-125, ConfigOption.height/2-170);
							if(ConfigOption.difficulty.equals("HARD")){
								gc.setFill(Color.BLACK);
								gc.fillText("HIDDEN CODE : HIDDEN CODE", ConfigOption.width/2-205, ConfigOption.height/2+250);
								gc.strokeText("HIDDEN CODE : HIDDEN CODE", ConfigOption.width/2-205, ConfigOption.height/2+250);
							}
						}
						else{
							gc.setFill(Color.RED);
							gc.fillText("GAME OVER", ConfigOption.width/2-85, ConfigOption.height/2-150);
							gc.strokeText("GAME OVER", ConfigOption.width/2-85, ConfigOption.height/2-150);							
						}
					}
					frame = 0;
					count++;
				}
				frame++;
				InputHolder.postUpdate();
			}
			
		};
	}
	
	private void addEnemies(){
		int order = 1;
		if(ConfigOption.difficulty.equals("EASY")){
			if(chapter == 1){
				//fetch word
				fetchWord(1,4,current_wave);
				for(String i: current_wave){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*401),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter == 2){
				//fetch word
				fetchWord(1,3,current_wave);
				fetchWord(2,4,current_wave);
				for(String i: current_wave.subList(0, 2)){
					RenderableHolder.instance.add(new Dog(1200+300*order+(int)(Math.random()*401),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order = 1;
				for(String i : current_wave.subList(2, 7)){
					RenderableHolder.instance.add(new Zombie(1200+200*order+(int)(Math.random()*301),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter == 3){
				//fetch word
				fetchWord(1,4,current_wave);
				fetchWord(2,6,current_wave);
				for(String i: current_wave.subList(0, 4)){
					RenderableHolder.instance.add(new Dog(1000+200*order+(int)(Math.random()*351),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order = 1;
				for(String i: current_wave.subList(4, 10)){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*251),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			order=1;
			used.clear();
		}
		else if (ConfigOption.difficulty.equals("MEDIUM")){
			if(chapter==1){
				fetchWord(1,3,current_wave);
				fetchWord(2,2,current_wave);
				for(String i: current_wave){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*351),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter==2){
				fetchWord(2,5,current_wave);
				fetchWord(3,1,current_wave);
				for(String i: current_wave.subList(0, 3)){
					RenderableHolder.instance.add(new Dog(1000+200*order+(int)(Math.random()*401),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order = 1;
				for(String i: current_wave.subList(3, 6)){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*301),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter==3){
				fetchWord(2,6,current_wave);
				fetchWord(3,3,current_wave);
				for(String i: current_wave.subList(0, 4)){
					RenderableHolder.instance.add(new Dog(1000+200*order+(int)(Math.random()*301),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order=1;
				for(String i: current_wave.subList(4, 9)){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*251),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			order = 1;
			used.clear();
		}
		else if (ConfigOption.difficulty.equals("HARD")){
			if(chapter==1){
				fetchWord(1,1,current_wave);
				fetchWord(2,4,current_wave);
				for(String i: current_wave){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*351),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter==2){
				fetchWord(2,6,current_wave);
				fetchWord(3,3,current_wave);
				for(String i: current_wave.subList(0, 4)){
					RenderableHolder.instance.add(new Dog(1000+200*order+(int)(Math.random()*351),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order = 1;
				for(String i: current_wave.subList(4, 9)){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*301),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			if(chapter==3){
				fetchWord(1,2,current_wave);
				fetchWord(2,5,current_wave);
				fetchWord(3,4,current_wave);
				for(String i: current_wave.subList(0, 4)){
					RenderableHolder.instance.add(new Dog(1000+200*order+(int)(Math.random()*251),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
				order=1;
				for(String i: current_wave.subList(4, 11)){
					RenderableHolder.instance.add(new Zombie(1000+200*order+(int)(Math.random()*251),
							90+(int)(Math.random()*601),i,gc));
					order++;
				}
			}
			order = 1;
			used.clear();
		}
	}
	
	//fetch word
	private void fetchWord(int rank,int amount,ArrayList<String> wave){
		String[] a = ConfigOption.getRank(rank);
		String word;
		String char1;
		int range = a.length;
		for(int i =0;i<amount;i++){
			do{
				int ran = (int)(Math.random()*range);
				word = a[ran];
				char1 = word.substring(0,1).toUpperCase();
			}while(used.contains(char1));
			used.add(char1);
			wave.add(word);
		}
	}
	
	private void removeSpace(ArrayList<String> wave){
		for(int i = 0;i<wave.size();i++){
			wave.set(i, wave.get(i).replaceAll("\\s",""));
		}
	}
	
	private void removeDestroyedEntities(){
		for(int i=0;i<RenderableHolder.instance.getEntities().size();i++){
			if(RenderableHolder.instance.getEntities().get(i).isDestroy())
				RenderableHolder.instance.remove(i);
		}
	}
	
	//Call draw in RenderableHolder
	private void paint(){
		for(int i=0;i<RenderableHolder.instance.getEntities().size();i++){
			RenderableHolder.instance.getEntities().get(i).draw(gc);
		}
		gc.fillText("SCORE : "+score, 710, 740);
		gc.strokeText("SCORE : "+score, 710, 740);
		gc.setFill(Color.YELLOW);
		gc.strokeText("PERFECT COMBO : " + perfect, 310, 740);
		gc.fillText("PERFECT COMBO : " + perfect, 310, 740);
		gc.setFill(Color.RED);
		gc.fillText("HEALTH : "+health, 20, 740);
		gc.strokeText("HEALTH : "+health,20,740);
		gc.setFill(Color.CORNFLOWERBLUE);
		gc.fillText(ConfigOption.difficulty, 50, 60);
	}
	
	private void moveAndActtack(){
		for(int i=0;i<RenderableHolder.instance.getEntities().size();i++){
			if(RenderableHolder.instance.getEntities().get(i) instanceof Enemy){
				if(((Enemy) RenderableHolder.instance.getEntities().get(i)).Move());
				else{
					hit_count++;
					if(hit_count>29){
						health-=5;
						hit_count = 0;
						if(health<=0){
							gameOverloop.start();
							gameplay.stop();
							gameover.play();
							paint();
							//set gc for game over
							gc.setGlobalAlpha(0.1);
							gc.setLineWidth(10);
							gameloop.stop();
							System.out.println("GAME OVER !!");
						}
					}
				}
			}
		}
	}
	
	
	//add basic Object
	public void setIRenderable(){
		RenderableHolder.instance.removeAll();
		//add bg
		RenderableHolder.instance.add(new BackGround());
		//add Main Character, Gun & Bunger
		RenderableHolder.instance.add(new MainCharacter(100,395));
		RenderableHolder.instance.add(new Bunger(150,90));
	}
	public void GameLoopStart(){
		gameloop.start();
	}
	public void setGc(GraphicsContext gc){
		this.gc = gc;
	}
	public void resetGamelogic(){
		score = 0;
		name = "_";
		miss = 0;
		totalMiss = 0;
		perfect = 0;
		chapter = 1;
		hitting = 0;
		hitting2 = 0;
		hit_count = 0;
		gameStart = true;
		setupChapter = false;
		focusing = false;
		endChapter = false;
		gameEnding = false;
		gameover.stop();
		gameend.stop();
	}
}
