package holder;

import java.util.ArrayList;

public class InputHolder {
	//hold input value
	public static double mouseX, mouseY;
	public static boolean mouseLeftDown,mouseOnScreen;
	public static boolean mouseLeftDownTrigger;
	public static ArrayList<String> keyPressed = new ArrayList<>();
	public static ArrayList<String> keyTriggered = new ArrayList<>();
	
	public static void postUpdate(){
		mouseLeftDownTrigger = false;
		keyTriggered.clear();
	}
	
	public static String getLastTrigger(){
		if(keyTriggered.get(keyTriggered.size()-1).equals("PERIOD"))return ".";
		else if(keyTriggered.get(keyTriggered.size()-1).equals("QUOTE"))return"'";
		else if(keyTriggered.get(keyTriggered.size()-1).equals("COMMA"))return",";		
		return keyTriggered.get(keyTriggered.size()-1);
	}
}
