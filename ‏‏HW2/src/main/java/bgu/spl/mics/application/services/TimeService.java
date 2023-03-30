package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.StopBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import org.omg.Messaging.SyncScopeHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link /*TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int speed = 0;
	private int duration = 0;
	private int currTick = 1;
	Timer timer;

	public TimeService(int speedOfTick, int duration) {
		super("TimeService");
		this.duration = duration;
		this.speed = speedOfTick;
		timer = new Timer();

	}



		@Override
	protected void initialize() {
		timer.scheduleAtFixedRate(new MyTimerTask(duration), 0, speed);

	}


	class MyTimerTask extends TimerTask {
		TickBroadcast tickBroadcast;
		int duration;

		public MyTimerTask(int duration) {
			this.duration = duration;
			tickBroadcast = new TickBroadcast();
		}

		public void run(){
			if( duration > 0){
				messageBus.sendBroadcast(tickBroadcast);
				tickBroadcast.advanceTick();
				duration--;
			}
			else{
				try {
					terminate();
					messageBus.sendBroadcast(new StopBroadcast());

				} catch (InterruptedException e) {
					System.out.println("Interupted");
					e.printStackTrace();
				}
				timer.cancel();
				Thread.currentThread().interrupt();
				//return;
			}
		}
	}
}
