package bgu.spl.net.api.bidi;

public interface BidiMessagingProtocol<T>  {
	/**
	 * Used to initiate the current client protocol with its personal connection ID and the connections implementation
	**/
    void start(int connectionId, ConnectionsImpl<T> connections);
    
    void process(T message);
	
	/**
     * @return true if the connection should be terminated
     */
    boolean shouldTerminate();
}
