package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.Event;

public class TickBroadcast implements Broadcast {

    int currTick = 0;
    public TickBroadcast(){

    }


    public String getName() {
        return "TickBroadcast" ;
    }

    public void advanceTick(){
        currTick++;
    }

    public int getTick(){
        return currTick;
    }
}
