# Gnutella-style P2P system

This is a Gnutella-style P2P file sharing system with consistency mechanisms.

The program is written with Java JDK 1.7.0_67. Gson is used.

# System Architecture

The system is a typical Gnutella p2p sharing system, which is as known as Client-Client model system. The unique component in this system is Client.

In this model, Peer acts as a client and also as a server. One Peer is as the source sending data and the other one works as destination accepting data, and vice versa. Once a Peer sends out a query message, the message can be spread through the whole network.

A peer will search for a desired file by sending out a query message to all its neighbors, and all its neighbors will check if they have the file. If not, the will broadcast the message to all their neighbors, till TTL decreases to zero. Once find the file in a peer, the peer will send back a hit message to the sender. And the sender then set up a Socket to download the file from the destination.

The support of maintaining file consistency is added. A server could push an Invalidate message to all its neighbors and then it will be broadcast to all peers in the Network just as a query message dose. And a client could pull the origin server to see if a cached file is valid. The pull method could be automatically done if a file’s TTR is expired. The result message will be displayed for each peer.

# Functions
`Query`: A peer can search a specific file in the network. 

`File Download`: Invoked by a peer to download a file from another peer. To do this, a peer send a download request to the corresponding peer, finally establish a Socket connection with that peer to transfer the desired file. 

`Auto Update`: 
A file alteration monitor is built for each peer at a specific path. If a user modifies, deletes or creates some files registered at a server, the peer will update its sharing list immediately.

`Push`: Upon an owned file changing, the corresponding peer will broadcast an Invalidate Message to the network, which acts exactly as a query message. Every peer in the network will receive this message and check if it has the file in downloaded file lists. If yes, check the version number in order to change this file’s consistency state. If no, just forward this Invalidate Message to its neighbors.

`Pull`: A peer could pull the original file server for a specific downloaded file’s version number. And update this file’s state. The pull method could be done periodically, the interval is decided by the TTR with is defined in configure file. And also, for each file the pull method is in a lazy manner. Which means upon TTR expires, after a short time a peer then do a pull request.

`Refresh`: A peer could pull the original file server for all its specific downloaded files version number. Then update all their consistency states. Then the user could choose to re-download an invalid file from its original server, which could make this file valid again.
