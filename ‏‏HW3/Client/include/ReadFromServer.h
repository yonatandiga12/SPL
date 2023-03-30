#ifndef CLIENT_READFROMSERVER_H
#define CLIENT_READFROMSERVER_H


#include "connectionHandler.h"

class ReadFromServer{

private:
    //bool shouldTermiinate = false;

public:
    ReadFromServer();
    virtual ~ReadFromServer();
    void run(ConnectionHandler *ch);

};



#endif //CLIENT_READFROMSERVER_H
