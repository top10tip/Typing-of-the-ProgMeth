package holder;

import java.util.ArrayList;
import java.util.List;

public class ThreadHolder {
	public static ThreadHolder instance = new ThreadHolder();
	
	private List<Thread> threads = new ArrayList<Thread>();
	
	public List<Thread> getThreads(){
		return threads;
	}
}