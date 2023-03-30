package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.ArrayList;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    int tickCount = 0;
    private ConfrenceInformation confrenceInformation;
    private ArrayList<Model> models;

    public ConferenceService(ConfrenceInformation conf) {
        super("Conference Service");
        confrenceInformation = conf;
        models = new ArrayList<>();
    }

    @Override
    protected void initialize() {

        subscribeBroadcast(TickBroadcast.class, tickBroadcast ->{
            tickCount++;

            if (timeTopublishSomething()) {
                sendBroadcast(new PublishConferenceBroadcast(confrenceInformation.getPublications()));
                messageBus.unregister(this);
                try {
                    terminate();
                    Thread.currentThread().interrupt();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        subscribeEvent(PublishResultsEvent.class, publishResultsEvent ->{
            Model m = publishResultsEvent.getModel();
            if(m.resultIsGood()){
                models.add(m);
                confrenceInformation.addPublication(m);
            }
            complete(publishResultsEvent, publishResultsEvent.getModel());

        });


    }

    private boolean timeTopublishSomething() {
        return confrenceInformation.getDate() == tickCount;
    }
}
