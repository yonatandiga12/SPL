#ifndef CLIENT_READFROMKEYBOARD_H
#define CLIENT_READFROMKEYBOARD_H

#include "connectionHandler.h"

class ReadFromKeyboard{

private:
    bool shouldTermiinate = false;
    bool isLoggedIn = false;

public:
    ReadFromKeyboard();
    virtual ~ReadFromKeyboard();

    void run(ConnectionHandler *ch);


};







#endif //CLIENT_READFROMKEYBOARD_H
