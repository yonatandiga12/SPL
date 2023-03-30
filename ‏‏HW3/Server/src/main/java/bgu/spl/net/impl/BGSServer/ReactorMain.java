package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.DataBase;
import bgu.spl.net.api.bidi.BGUProtocol;
import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.srv.Server;

import java.util.ArrayList;

public class ReactorMain {

    public static void main(String[] args) {


        if(args.length < 2){
            return;
        }

        String portString = args[0];
        int port = Integer.parseInt(portString);
        String numThreadsString = args[1];
        int numThreads = Integer.parseInt(numThreadsString);


        //int port = 7776;
        //int numThreads = 4;

        ArrayList<String> forbidden = new ArrayList<>();
        forbidden.add("trump");      forbidden.add("war");
        DataBase dataBase = new DataBase(forbidden);

        System.out.println("In ReactorMain");
        Server.reactor(
                numThreads,
                port,                       //port
                () -> new BGUProtocol(dataBase), //protocol factory
                BidiMessageEncoderDecoder::new   //message encoder decoder factory
        ).serve();

    }
}
