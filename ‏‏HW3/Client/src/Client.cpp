#include <stdlib.h>
#include <connectionHandler.h>
#include <ReadFromKeyboard.h>
#include <thread>
#include <ReadFromServer.h>


int main (int argc, char *argv[]) {


    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }

    std::string host = argv[1];
    short port = atoi(argv[2]);


    //std::string host = "127.0.0.1";
    //short port = 7776;


    auto *connectionHandler = new ConnectionHandler(host, port);

    if (!connectionHandler->connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }

    ReadFromKeyboard readFromKeyboard;
    ReadFromServer readFromServer;

    std::thread threadRead (&ReadFromKeyboard::run, &readFromKeyboard, connectionHandler);
    std::thread threadWrite (&ReadFromServer::run, &readFromServer, connectionHandler);
    threadRead.join();
    threadWrite.join();

    delete connectionHandler;

    return 0;
}

