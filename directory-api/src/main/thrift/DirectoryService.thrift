namespace java at.ac.tuwien.aic.ws14.group2.onion.directory.api.service

struct NodeUsage {
    1: string beginTime,
    2: string endTime,
    3: i64 relayMsgCount,
    4: i64 createMsgCount,
}

struct ChainNodeInformation {
    1: i32 port,
    2: string address,
    3: optional string instanceName,
    4: string publicRsaKey //base64?
}

service DirectoryService {

   void ping(),

   bool heartbeat(1: NodeUsage nodeUsage),

   bool registerNode(1: ChainNodeInformation nodeInformation),

   list<ChainNodeInformation> getChain(1: optional i32 chainLength = 3),

}