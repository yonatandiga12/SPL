package bgu.spl.net.srv;

import bgu.spl.net.DataBase;
import bgu.spl.net.api.bidi.BGUProtocol;
import bgu.spl.net.api.bidi.BidiMessageEncoderDecoder;
import bgu.spl.net.impl.rci.ObjectEncoderDecoder;

import java.util.ArrayList;

public class mainServer {

    public static void main(String[] args) {

        ArrayList<String> forbidden = new ArrayList<>();
        forbidden.add("trump");      forbidden.add("war");
        DataBase dataBase = new DataBase(forbidden);


// you can use any server...
        Server.threadPerClient(
                7777, //port
                () -> new BGUProtocol(dataBase), //protocol factory
                BidiMessageEncoderDecoder::new //message encoder decoder factory
        ).serve();

//        Server.reactor(
//                Runtime.getRuntime().availableProcessors(),
//                7777, //port
//                () -> new BGUProtocol(dataBase), //protocol factory
//                BidiMessageEncoderDecoder::new //message encoder decoder factory
//        ).serve();

    }

}
