package p2p_gnutella;

/**
 * This is the AutoPull class.
 * Which handles the pull action for each downloaded file.
 * 
 * @author Fan Lin
 * @version 1.0
 * @since 2014-11-06
 * */

import java.util.Timer;
import java.util.TimerTask;

public class AutoPull extends TimerTask{
	
	private String pullFile;
	private Timer mytimer;

	public AutoPull (String fileName, Timer timer){
		pullFile = fileName;
		mytimer = timer;
	}
	
	@Override
	public synchronized void run() {
		try {
			// quit timer is the file is already invalid
			if (Client.downloadFiles.get(pullFile).conState.equals("Invalid"))
				mytimer.cancel();
			Thread.sleep(Client.downloadFiles.get(pullFile).TTR*1000);
			System.out.println(pullFile + " Expiered!");
			Client.downloadFiles.get(pullFile).conState = "TTR expeied";
			// pull in a lazy manner, the interval is decided by TTR
			Thread.sleep(Client.downloadFiles.get(pullFile).TTR*1000);
			System.out.println("Now Pulling... " + pullFile);
			Client.downloadFiles.get(pullFile).pull();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
