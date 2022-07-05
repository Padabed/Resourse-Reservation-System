	"Network Node"
	Communication with clients and newly connected nodes uses the TCP protocol

	Description:
    The computing system consists of a set of processes located in the network.
Each process has a certain set of resources. Processes constitute a logical network, which organization depends on implementation and the processes are its nodes.
The network is incrementally created by executing consecutive processes (creation of consecutive nodes) and connecting them to the already existing network.
After starting, each process connects to the network according to the assumed network communication model and starts waiting for possible connections from new system components (next added nodes) or from clients willing to reserve the resources.

    In project implemented Network Node to implement a network node.
It includes the functions of creating a network node with a given constructor,
adding new nodes, establishing a network connection, setting a parent node in the network,
as well as getting a network node from the general list of nodes located on the network,
getting child nodes, and getting nodes neighbors (this list includes child nodes and a parent node).
The host is inherited from the Destination class, which is its base class and already has all the geters and seters inside, as well as basic constructors, an overridden equals method, toString.
The network node works with a ResourceManager, which stores the resource list.
The ResourceManager has methods for managing data about resources, as well as printing this data, and managing it accordingly.

    The project implements the AllocationRequest class, which implements the Callable<AllocationRequest> interface.
The class contains data about the request, such as client id , request status, all resources in the request, resource history, ServerSocket, network host, and to whom the request is assigned (Destination).
Accordingly, constructors for this class with or without a client id, as well as geters and seters, such as get id, request status, get visited nodes.
Methods for building a protocol (buildProtocol)
	

