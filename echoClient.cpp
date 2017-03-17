#include <stdlib.h>
#include <boost/locale.hpp>
#include "../include/connectionHandler.h"
#include <boost/thread.hpp>
#include <iostream>
#include <boost/asio.hpp>


/**
 * sends messages from the user to the server
 * running on a thread of it own
 * (different from the receive msg)
 */
void sendMsg(ConnectionHandler *handler) {
	while(handler->getTerminated()) {
		const short bufsize = 1024;
		char buf[bufsize];
		std::cin.getline(buf, bufsize);
		std::string line(buf);
		if (!handler->sendLine(line)) {
			std::cout << "Disconnected. Exiting...\n" << std::endl;
			break;
		}

		if (line=="QUIT"){
			boost::this_thread::sleep_for(boost::chrono::milliseconds(200));
		}
	}
	handler->close();
}


/**
 * handler is a pointer to the connection handler
 * checks constantly for messages from the server
 * when receives "SYSMSG QUIT ACCEPTED" it closes
 * running on a thread on its own (different from the send msg)
 */
void receiveMsg(ConnectionHandler *handler) {
	int len;
	while(1) {
		std::string answer;
		if (!handler->getLine(answer)) {
			handler->setTerminated();
			std::cout << "Disconnected. Exiting...\n" << std::endl;
		}
		len=answer.length();
		answer.resize(len-1);
		std::cout << "SERVER: " << answer << std::endl;
		if (answer == "SYSMSG QUIT ACCEPTED") {
			handler->setTerminated();
			std::cout << "Exiting...\n" << std::endl;
			break;
		}
	}
	handler->close();
}


/**
* This code assumes that the server replies the exact text the client sent it
*  (as opposed to the practical session example)
*/
int main (int argc, char *argv[]) {



	std::string host = argv[1];
	short port= atoi(argv[2]);


	ConnectionHandler *connectionHandler = new ConnectionHandler(host, port);
	if (!connectionHandler->connect()) {
		std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
		return 1;
	}

	boost::thread t1(sendMsg,connectionHandler);
	boost::thread t2(receiveMsg,connectionHandler);
	t1.join();
	t2.join();

	delete connectionHandler;
}
//132.72.45.36
