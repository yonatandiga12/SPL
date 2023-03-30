package bgu.spl.mics;

import bgu.spl.mics.application.messages.StopBroadcast;
//import bgu.spl.mics.application.messages.TickBroadcast;
//import bgu.spl.mics.application.objects.CPU;
//import bgu.spl.mics.application.services.CPUService;
//import bgu.spl.mics.application.services.StudentService;
//import bgu.spl.mics.application.services.TimeService;
//import sun.misc.Queue;
//import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;

//import java.util.ArrayList;
//import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	/**
	 * @INV: MicroService != null
	 * 		 Event != null
	 */
	private static ConcurrentHashMap<MicroService, LinkedBlockingDeque<Message>> MsgQueue;   			  //Will hold all the queues of the microservices
	private static ConcurrentHashMap<MicroService, BlockingDeque<Class<? extends Message>>> MicroServicesListedTo;   //Will hold list of just the subscription/registration info
	private static ConcurrentHashMap<Class<? extends Event>, LinkedList<MicroService>> eventsRoundRobin;
	private static ConcurrentHashMap<Event , Future> futureHashMap;
	private static MessageBusImpl instance = null;
	private static boolean isDone = false;


	private MessageBusImpl(){
		MsgQueue = new ConcurrentHashMap<>();
		MicroServicesListedTo = new ConcurrentHashMap<>();
		eventsRoundRobin = new ConcurrentHashMap<>();
		futureHashMap = new ConcurrentHashMap<>();
	}

	public static MessageBusImpl getInstance(){
		if(!isDone){
			synchronized (MessageBusImpl.class){
				if(!isDone){
					instance = new MessageBusImpl();
					isDone = true;
				}
			}
		}
		return instance;
	}


	@Override
	/**
	 * @PRE m != null
	 * @POST Queue size = @pre(Queue size) + 1
	 */
	public synchronized <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		for (Map.Entry<MicroService, BlockingDeque<Class<? extends Message>>> entry : MicroServicesListedTo.entrySet()) {
			if(entry.getKey().equals(m)) {
				entry.getValue().offerFirst(type);
				if(!eventsRoundRobin.containsKey(type)) {
					eventsRoundRobin.put(type, new LinkedList<>());
				}
				eventsRoundRobin.get(type).add(m);
			}
		}
	}

	@Override
	/**
	 * @POST Queue size = @pre(Queue size) + 1
	 */
	public synchronized void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		for (Map.Entry<MicroService, BlockingDeque<Class<? extends Message>>> entry : MicroServicesListedTo.entrySet()) {
			if(entry.getKey().equals(m)) {
				entry.getValue().offerFirst(type);
			}
		}
	}

	@Override
	/**
	 * @PRE e.isDone = true
	 *  @PRE result != null
	 * @POST future.get() = result
	 */
	public <T> void complete(Event<T> e, T result) {
		Future res = futureHashMap.remove(e);
		if(res != null && !res.isDone())
			res.resolve(result);

	}

	@Override
	/**
	 * @PRE  M'sQueue.contains(b) = false
	 * @POST M'sQueue.contains(b) = true
	 */
	public synchronized void sendBroadcast(Broadcast b) {
		if(b instanceof StopBroadcast){
			for (MicroService microService : MicroServicesListedTo.keySet()) {
				LinkedBlockingDeque<Message> curr = MsgQueue.get(microService);
				curr.offerFirst(b);
				MsgQueue.putIfAbsent(microService, curr);
			}
				return;
		}
		for (Map.Entry<MicroService, BlockingDeque<Class<? extends Message>>> entry : MicroServicesListedTo.entrySet()) {
			if (entry.getValue().contains(b.getClass())) {
				LinkedBlockingDeque<Message> curr = MsgQueue.get(entry.getKey());
				curr.offerFirst(b);
				MsgQueue.putIfAbsent(entry.getKey(), curr);
			}
		}
	}

	@Override
	/**
	 * @PRE  M'sQueue.contains(e) = false
	 * @POST M'sQueue.contains(e) = true
	 */
	public synchronized <T> Future<T> sendEvent(Event<T> e) {
		Future future = new Future();
		futureHashMap.put(e, future);
		if(checkIfEventExists(e.getClass())){
			MicroService chosenOne = selectForRoundRobin(e);
			if(chosenOne!= null){
				LinkedBlockingDeque<Message> curr = MsgQueue.get(chosenOne);
				curr.offer(e);
				MsgQueue.putIfAbsent(chosenOne, curr);
			}
			return future;
		}
		else{
			return null;
		}
	}

	private boolean checkIfEventExists(Class<? extends Event> eventClass) {
		for(Map.Entry<MicroService, BlockingDeque<Class<? extends Message>>> entry : MicroServicesListedTo.entrySet()){
			if(entry.getValue().contains(eventClass))
				return true;
		}
		return false;
	}

	private <T> MicroService selectForRoundRobin(Event<T> e) {
		MicroService chosen = null;
		if(eventsRoundRobin.containsKey(e.getClass())){
			chosen = eventsRoundRobin.get(e.getClass()).removeLast();
			eventsRoundRobin.get(e.getClass()).addFirst(chosen);
		}
		return chosen;
	}

	@Override
	/**
	 * @POST Queue size = @pre(Queue size) + 1
	 */
	public synchronized void register(MicroService m) {
		MicroServicesListedTo.putIfAbsent(m, new LinkedBlockingDeque<>());
		MsgQueue.putIfAbsent(m, new LinkedBlockingDeque<>());
	}


	@Override
	/**
	 * @PRE m!=null
	 * @POST Queue size = @pre(Queue size) - 1
	 */
	public void unregister(MicroService m) {
		MicroServicesListedTo.remove(m);
		MsgQueue.remove(m);
		for(Map.Entry<Class<? extends Event>, LinkedList<MicroService>> entry : eventsRoundRobin.entrySet()){
			if(entry.getValue().contains(m)){
				entry.getValue().remove(m);
			}
		}
	}

	@Override
	/**
	 * @POST Queue size = @pre(Queue size) - 1
	 */
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if(!MicroServicesListedTo.containsKey(m) ){
			throw new InterruptedException("This MicroService is not registered");
		}
		Message output = MsgQueue.get(m).takeFirst();
		return output;
	}


}
