#include <connectionHandler.h>
 
using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}
    
ConnectionHandler::~ConnectionHandler() {
    close();
}
 
bool ConnectionHandler::connect() {
    std::cout << "Starting connect to " 
        << host_ << ":" << port_ << std::endl;
    try {
		tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint
		boost::system::error_code error;
		socket_.connect(endpoint, error);
		if (error)
			throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
	boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
			tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);			
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    //for(int i = 0; i < bytesToWrite; i++){   //for testing
    //    char d = bytes[i];
    //    std::cout<<d;
    //}
	boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
			tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
		if(error)
			throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    currLengthMsg = 0;
    return true;
}
 
bool ConnectionHandler::getLine(std::string& line) {

    return getFrameAscii(line, ';');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, ';');
}
 
bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch = ';';
    // Stop when we encounter the null character. 
    // Notice that the null character is not appended to the frame string.
    try {
		do{
			getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}
 
bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
    char* bytes = encode(frame);
    bool result = sendBytes( bytes , currLengthMsg);
	if(!result) return false;
	return sendBytes(&delimiter,1);
}
 
// Close down the connection properly.
void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }

}


string lastWord(string str)
{
    string ans = str;
    for (int i = ans.length() - 1; i > 0; i--)
    {
        if (ans[i] == ' ')
            return ans.substr(i + 1);
    }
    return ans;
}
string findWord(string str,int num)
{
    string ans = str;
    int counter = 1;

    for (unsigned int i = 0; i < ans.length(); i++)
    {
        if (ans[i] == ' ')
        {
            counter++;
        }
        if (counter == num)
        {
            int stop = 0;
            int j = i + 1;
            bool stopped = true;
            while (stopped)
            {
                if (ans[j] == ' ')
                {
                    stopped = false;
                    stop=j-1;
                    return (ans.substr(i + 1, stop-(i)));
                }
                j++;
            }

        }
    }
    return ans;

}
short ConnectionHandler::bytesToShort(char* bytesArr)

{

    short result = (short)((bytesArr[0] & 0xff) << 8);

    result += (short)(bytesArr[1] & 0xff);

    return result;

}



void ConnectionHandler::shortToBytes(short num, char* bytesArr)

{

    bytesArr[0] = ((num >> 8) & 0xFF);

    bytesArr[1] = (num & 0xFF);

}


char* ConnectionHandler::encode(const std::string& s)
{
    char* bytes;
    bytes = (char*)malloc(1000*sizeof(char));

    string completeWord = s;
    string firstWord = s.substr(0, s.find(" ")) ;
    short opcode = 0;
    if (firstWord == "REGISTER"){
        opcode = 1;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string Username=findWord(completeWord,2);
        putInArrayFromThirdPlace(Username, bytes);
        bytes[currLengthMsg++] = 0;
        string password = findWord(completeWord, 3);
        putInArrayFromThirdPlace(password, bytes);
        bytes[currLengthMsg++] = 0;
        string birthday = lastWord(completeWord);
        putInArrayFromThirdPlace(birthday, bytes);
        bytes[currLengthMsg++] = 0;

    }
    else if (firstWord == "LOGIN"){
        opcode = 2;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string Username = findWord(completeWord, 2);
        putInArrayFromThirdPlace(Username, bytes);
        bytes[currLengthMsg++] = 0;
        string password = findWord(completeWord,3);
        putInArrayFromThirdPlace(password, bytes);
        bytes[currLengthMsg++] = 0;
        string captcha = lastWord(completeWord);
        if(captcha == "1")
            bytes[currLengthMsg++] = 49;
        else if(captcha == "0")
            bytes[currLengthMsg++] = 48;   //The char 0 not the char /0

    }
    else if (completeWord == "LOGOUT"){
        opcode = 3;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;

    }
    else if (firstWord == "FOLLOW"){
        opcode = 4;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string follow = findWord(completeWord, 2);
        string Username = lastWord(completeWord);
        if (follow == "0")
            bytes[currLengthMsg++] = '0';
        else
            bytes[currLengthMsg++] = '1';
        putInArrayFromThirdPlace(Username, bytes);
        bytes[currLengthMsg++] = 0;

    }
    else if (firstWord == "POST"){
        opcode = 5;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string content = completeWord.substr(5);
        putInArrayFromThirdPlace(content, bytes);
        bytes[currLengthMsg++] = 0;

    }
    else if (firstWord == "PM")
    {
        opcode = 6;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string Username = findWord(completeWord, 2);
        putInArrayFromThirdPlace(Username, bytes);
        bytes[currLengthMsg++] = 0;
        string sendingDateAndTime = lastWord(completeWord);
        string content = completeWord.substr(4 + Username.length(), completeWord.length() - sendingDateAndTime.length());
        putInArrayFromThirdPlace(content, bytes);
        bytes[currLengthMsg++] = 0;
        putInArrayFromThirdPlace(sendingDateAndTime, bytes);
        bytes[currLengthMsg++] = 0;

    }
    else if (completeWord == "LOGSTAT"){
        opcode = 7;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;

    }
    else if (firstWord == "STAT"){
        opcode = 8;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string ListOfUserNames = completeWord.substr(5);
        putInArrayFromThirdPlace(ListOfUserNames, bytes);
        bytes[currLengthMsg++] = 0;

    }
    else if(firstWord == "BLOCK"){
        opcode = 12;
        shortToBytes(opcode, bytes);
        currLengthMsg += 2;
        string userName = completeWord.substr(6);
        putInArrayFromThirdPlace(userName, bytes);
        bytes[currLengthMsg++] = 0;
    }
    else{
        std::cout<<"Input is not valid."<<endl;
    }
    return bytes;
}


void ConnectionHandler::putInArrayFromThirdPlace(std::string ans, char *bytes) {
    for(unsigned int i = 0; i < ans.length(); i++){
        bytes[currLengthMsg] = ans[i];
        currLengthMsg++;
    }
}



string ConnectionHandler::decode(string bytes)
{
    short opcode = 0;
    char c = 0;     //To find the 0 in the strings
    string ans = bytes;
    string rest;

    // Saves the opcode
    char opcodeBytes[2];
    opcodeBytes[0] = bytes[0];
    opcodeBytes[1] = bytes[1];
    opcode = bytesToShort(opcodeBytes);
    rest = ans.substr(2);

    if (opcode == 9)
    {
        std::string kindMsg;
        std::string msgOp = rest.substr(0, 1);   //pm or post
        if(msgOp == "1")
            kindMsg = "PUBLIC";
        else
            kindMsg = "PM";
        rest = rest.substr(1);
        int index = rest.find(c);
        std::string postingUserName = rest.substr(0, index);
        rest = rest.substr(index + 1);
        std::string content = rest.substr(0, rest.find(c));
        std::cout << "NOTIFICATION " << kindMsg << " " << postingUserName << " " << content <<endl;
    }
    else if (opcode == 10)
    {
        int index = rest.find(c);
        std::string Msgop = rest.substr(0, index);
        if(Msgop == "03") {    //Logout
            std::cout<<"ACK " << std::stoi(Msgop) << std::endl ;
            //return "ACK " + Msgop;
            return Msgop;

        }
        else if(Msgop == "07" || Msgop == "08"){  //Logstat
            int tmp = 0;
            rest = rest.substr(2);
            std::cout<<"ACK " << std::stoi(Msgop);
            while(tmp < 4){
                char currBytes[2];
                currBytes[0] = rest[0];
                currBytes[1] = rest[1];
                short curr = bytesToShort(currBytes);
                std::cout<<" " << curr;
                rest = rest.substr(2);
                tmp++;
            }
            std::cout<<""<<endl;
        }
        else if(Msgop == "04"){     //Follow
            rest = rest.substr(index);
            std::string follow = rest.substr(0,2);
            rest = rest.substr(1);
            index= rest.find(c);
            std::string name = rest.substr(index);
            std::cout<<"ACK " << std::stoi(Msgop) << " " << follow << " " << name <<std::endl;
        }
        else{
            std::cout<<"ACK " << std::stoi(Msgop) << std::endl;
            return Msgop;
        }
    }
    else if (opcode == 11)
    {
        int index = rest.find(c);
        rest = rest.substr(0, index);
        std::cout<<"ERROR " << std::stoi(rest) << std::endl;
        return rest;
    }
    else if (opcode == 12)
    {
        int index = rest.find(c);
        std::string Msgop = rest.substr(0, index);
        std::cout<<"ACK " << std::stoi(Msgop) << std::endl;
        return Msgop;
    }
    else
    {
        std::cout<<"Not valid"<<endl;
    }

    return "";
}


