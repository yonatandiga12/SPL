#include "ReadFromKeyboard.h"
#include "../include/ReadFromKeyboard.h"


ReadFromKeyboard::ReadFromKeyboard() = default;

void ReadFromKeyboard::run(ConnectionHandler *ch) {
    while(!shouldTermiinate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        if (!ch->sendLine(line)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        if(line.find("LOGIN") == std::string::npos)  //temp fix only if he was logged in the logout will bew successful
            isLoggedIn = true;
        if(line == "LOGOUT")
            if(isLoggedIn)
                shouldTermiinate = true;

    }
}

ReadFromKeyboard::~ReadFromKeyboard() = default;


