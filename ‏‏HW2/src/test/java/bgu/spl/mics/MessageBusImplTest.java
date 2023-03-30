package bgu.spl.mics;

import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {

    //public static MicroService microService1;
    //public static MicroService microService2;
    //public static MessageBusImpl mBus;
    //public static Event event;
    //public static Broadcast broadcast;
    //public static Future<String> future;



    @Before
    public void setUp() throws Exception {
        //String[] arr = {"s"};
        //microService1 = new ExampleMessageSenderService("s",arr);
        //microService2 = new ExampleMessageSenderService("s",arr);
        //mBus = MessageBusImpl.getInstance();
        //event = new ExampleEvent("test");
        //broadcast = new ExampleBroadcast("test");
        //future = new Future<>();
    }

    @Test
    public void subscribeEvent() {
        //assertFalse(mBus.isMicroServiceSubscribed(event, microService1));
        //mBus.subscribeEvent(ExampleEvent.class, microService1);
        //assertTrue(mBus.isMicroServiceSubscribed(event, microService1));
    }

    @Test
    public void subscribeBroadcast() {
        //assertFalse(mBus.isMicroServiceSubscribed(broadcast, microService1));
        //mBus.subscribeBroadcast(broadcast, microService1);
        //assertTrue(mBus.isMicroServiceSubscribed(broadcast, microService1));
    }

    @Test
    public void complete() {
        //mBus.subscribeEvent(event, microService1);
        //Future<String> f = mBus.sendEvent(event);
        //mBus.complete(event, "test" );
        //try {
        //    assertEquals(f.get(), "test");
        //} catch (InterruptedException e) {
         //   e.printStackTrace();
        //}
    }

    @Test
    public void sendBroadcast() {
        //assertFalse(mBus.doesSendBActivated(event, microService1));
        //mBus.subscribeEvent(event, microService1);
        //mBus.subscribeEvent(event, microService2);
        //mBus.sendBroadcast(broadcast);
        //assertTrue(mBus.doesSendBActivated(event, microService1));
        //assertTrue(mBus.doesSendBActivated(event, microService2));

    }

    @Test
    public void sendEvent() {
        //assertFalse(mBus.doesSendBActivated(event, microService1));
        //mBus.subscribeEvent(event, microService1);
        //assertNull(future);
        //future = mBus.sendEvent(event);
        //assertNotNull(future);
        //assertTrue(mBus.doesSendBActivated(event, microService1));


    }

    @Test
    public void register() {
        //assertFalse(mBus.isMicroServiceRegisterd(microService1));
        //mBus.register(microService1);
        //assertTrue(mBus.isMicroServiceRegisterd(microService1));

    }

    @Test
    public void unregister() {
        //mBus.register(microService1);
        //assertTrue(mBus.isMicroServiceRegisterd(microService1));
        //mBus.unregister(microService1);
        //assertFalse(mBus.isMicroServiceRegisterd(microService1));
    }

    @Test
    public void awaitMessage() {
        //mBus.sendBroadcast(broadcast);
        //try {
        //    mBus.awaitMessage(microService1);
        //} catch (InterruptedException e) {
        //    e.printStackTrace();
        //}
        //assertFalse(mBus.doesSendBActivated( broadcast, microService1));
    }

}