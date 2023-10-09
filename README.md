# File Sharing Cloud

## Project Overview

This project aims to create a functional distributed system for working with ASCII-encoded textual files. The system provides the following functionalities:

- Adding new files with unique names and paths to the system.
- Retrieving arbitrary files from the distributed system.
- Deleting files on the local server.
- Topological organization of the system to enable faster file retrieval.
- Fault tolerance.

## Functional Requirements

### Basic Functionality

The File Sharing Cloud system is designed for working with ASCII-encoded textual files. Files should be organized in a virtual file structure, where each individual node can contain an arbitrary subset of this entire structure. Interaction with the system is performed via the command line interface (CLI). The virtual file system should be maintained as long as there is at least one active node. When the last node is shut down, the virtual file system ceases to exist, and it should not be possible to reconstruct it later.

The basic functionalities for the system include:

- **Add:** Adding a new file to the system.
- **Pull:** Retrieving a file that is currently not present on the node.
- **Remove:** Deleting a file from the system.

There should be a local directory on the local system that starts empty and is used for working with the system:

- **Working Directory:** This is where files that the user actively wants to work with will reside.

### Node Configuration

When a node starts, it should automatically read a configuration file containing the following attributes:

- **Working Directory:** The path on the local system where working files are stored (string).
- **Port:** The port on which the node will listen (short).
- **IP Address and Port of the Bootstrap Node:** Refer to section 3.1 for details (string and short).
- **Weak Failure Threshold:** Refer to section 3.2 for details (int).
- **Strong Failure Threshold:** Refer to section 3.2 for details (int).

It is assumed that all nodes will have consistent configuration files, and there is no need for additional checks to ensure this. It is allowed for the system to not function correctly due to incorrectly configured configuration files.

### Commands

Users can issue the following commands to the system:

- **add name:** Adds a file with the specified name to the system. 'Name' can also be a directory, in which case the entire content is recursively added to the system. If the virtual system already contains a file with this name at this path, an error should be reported, and the file should not be added.
- **pull name:** Retrieves a file with the specified name from the system. The file name is specified as the full path to the file and its name.
- **remove name:** Removes a file with the specified name from the system. 'Name' can also be a directory, in which case the remove operation is performed recursively for the entire directory.
- **stop:** Gracefully shuts down the node.

For all commands, when specifying file names, relative paths should be used with respect to the working directory specified in the configuration file. File names will never contain spaces, so there is no need to support them.

## Non-Functional Requirements

### Architecture of the System

There are two possible architectures for the system, each with its own scoring:

1. **Sparse Graph:** In this architecture, as the graph is not complete, node A must find a (not necessarily complete) path to node B to send a message. Here, it is essential that the number of hops between A and B follows a logarithmic relationship with the total number of nodes. If the number of hops between arbitrary A and B approaches a linear relationship, the implementation will be evaluated as if it were a complete graph. For the graph to be considered sparse, the number of neighbors for all nodes must have a logarithmic relationship with the total number of nodes. There should be no central point of failure (a node whose shutdown causes the system to stop). There should be no bottleneck; for this project, a bottleneck is defined as a node (or a fixed number of nodes) through which communication frequently flows. If there are nodes through which communication frequently flows, but their number depends on the number of nodes in the system, so that communication is naturally distributed among them, then they are not considered bottlenecks. Starting new nodes and stopping active nodes can and should trigger a restructuring of the system.

2. **Complete Graph:** In this architecture, every node is connected to every other node, and communication is always direct.

### Failure Detection

Node failure detection occurs in two phases. As a constant system parameter, there should be a weak failure threshold (e.g., 1000ms) and a strong failure threshold (e.g., 10000ms), both expressed as the time a node does not respond to a ping.

- If a node exceeds the weak failure threshold, it is marked as suspicious, but node removal and system restructuring are not initiated. Instead, we seek confirmation from another stable node that the suspicious node is indeed problematic.
- If we receive confirmation that the node is suspicious and then the strong failure threshold is exceeded, the node is removed from the system, and system restructuring begins.

When a node fails, its previous work must not be lost. If there is an available node (one of its buddies), it should take over the tasks previously assigned to the failed node.

The system should be able to withstand a "worst-case" scenario in which an intelligent malicious attacker can select and simultaneously crash any two nodes every five minutes.

### Data Distribution in the System

Files are located on the node where they were created, and there must be at least one backup copy of each file somewhere in the system.

### General Non-Functional Requirements

The system should function on a real network, where every message has arbitrary delays, and channels are not FIFO. If the system is tested on a single machine, artificial delays should be introduced when sending messages for realistic testing. Even if testing is done on a single machine, it is not allowed to specify the destination IP address as "localhost" when sending a message.

All communication should be explicitly defined and documented. If a node receives a message that does not adhere to the protocol, it should be rejected and ignored. The protocol documentation should, at a minimum, include everything required to write a new servent that can participate in the system's operation. Typically, this includes a list of all messages in the system, their format (the order of values sent, their type, and their meaning), and any specific message sending order.

## Getting Started

To get started with this project, follow these steps:

1. Clone the repository to your local machine.
2. Configure the nodes according to the provided guidelines in the configuration file.
3. Build and run the system using the chosen programming language.
4. Use the command line interface (CLI) to interact with the system and perform file operations.

