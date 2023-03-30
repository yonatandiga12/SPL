package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
//import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.ConnectionsImpl;
import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final BidiMessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
    private int id;

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, BidiMessagingProtocol<T> protocol, int id) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
        this.id = id;
        ConnectionsImpl<T> connections = ConnectionsImpl.getInstance();
        connections.addConnection(this, id);
        this.protocol.start(id, connections);
    }


    @Override
    public void send(T msg) {
        try{
            out = new BufferedOutputStream(sock.getOutputStream());
            if (msg != null) {
                out.write(encdec.encode(msg));
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) {
                    protocol.process(nextMessage);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }





}
