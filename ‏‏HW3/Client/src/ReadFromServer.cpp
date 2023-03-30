#include "ReadFromServer.h"
#include "../include/ReadFromServer.h"


ReadFromServer::ReadFromServer() = default;

void ReadFromServer::run(ConnectionHandler *connectionHandler) {
    while(true) {
        std::string answer;
        if (!connectionHandler->getLine(answer)) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        int len = answer.length();
        answer.resize(len - 1);

        std::string result = connectionHandler->decode(answer);
        if (result == "03") {     //Logout
        // if (result == "ACK 03") {     //Logout
        //std::cout << "Exiting readFromServer...\n" << std::endl;
            break;
        }
    }
}

ReadFromServer::~ReadFromServer() = default;

