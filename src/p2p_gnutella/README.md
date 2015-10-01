# Implementation
## `Client.java` 
The class of a peer, all required functions should be realized here. If a peer has n neighbors, we set up at least n+4 threads. Note n threads for connection with n neighbors, three thread for serversockets to listening to incoming requests, and one is main thread for user interface. If any downloaded file exists, a thread for timer will be set up for every file to handle the auto pull function.
## `IndexHandler.java`
The class of IndexHandler, mainly responses to the Query request. Forward the query message to all neighbors, and keep track of the migration of the message in order to find the sender. A mutex lock is used here to ensure only one process could do a query at a time, which could avoid collisions. 
## `MessageID.java` 
A data structure which stores required information for a message ID.
## `Message.java` 
A data structure which stores required information for a message. There are some simple methods to do modify the message.
## `HitMessage.java` 
A data structure which stores required information for a hit message. 
## `SetupListener.java` 
The class of SetupListener, fork a thread to listen to network setup request from neighbors.
## `SendListener.java` 
Fork a thread to any Download request from other peers.
## `PullListener.java` 
Fork a thread to listen to any pull request from other peers. And will send back the original file version number to requester.
## `FileInfo.java` 
The class of file information, store required file information for specific file.
## `InvalidateMessage.java` 
A data structure which stores required information for a invalidate Message.
## `AutoPull.java` 
The class of auto pull, which handles the pull action for each downloaded file.
