namespace java at.ac.tuwien.aic.ws14.group2.onion.directory.api.service

struct NodeUsage {
    1: string beginTime,
    2: string endTime,
    3: i64 relayMsgCount,
    4: i64 createMsgCount,
    5: i64 circuitCount,
    6: i64 chainCount,
    7: i64 targetCount,
    8: optional string signature
}

struct NodeUsageSummary {
    1: string beginTime,
    2: string endTime,
    3: i64 relayMsgCount,
    4: i64 createMsgCount,
    5: optional string signature
}

struct ChainNodeInformation {
    1: i32 port,
    2: string address,
    3: optional string instanceId,
    4: optional string instanceName,
    5: optional string region,
    6: optional string domainName,
    7: string publicRsaKey //base64?
}

service DirectoryService {

   bool heartbeat(1: i32 nodeID, 2: NodeUsage nodeUsage),

   i32 registerNode(1: ChainNodeInformation nodeInformation),

   list<ChainNodeInformation> getChain(1: optional i32 chainLength = 3),

}