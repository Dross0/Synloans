package com.synloans.loans.adapter.connection;

import com.synloans.loans.adapter.dto.NodeUserInfo;
import net.corda.client.rpc.CordaRPCClient;
import net.corda.client.rpc.CordaRPCConnection;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.utilities.NetworkHostAndPort;

public final class ConnectionFactory {

    private ConnectionFactory(){
    }

    public static CordaRPCConnection createConnection(NodeUserInfo nodeUserInfo){
        return createConnection(nodeUserInfo.getAddress(), nodeUserInfo.getUser(), nodeUserInfo.getPassword());
    }

    public static CordaRPCConnection createConnection(NetworkHostAndPort address, String user, String password){
        CordaRPCClient cordaRPCClient = new CordaRPCClient(address);
        return cordaRPCClient.start(user, password);
    }

    public static CordaRPCConnection createConnection(String address, String user, String password){
        return createConnection(NetworkHostAndPort.parse(address), user, password);
    }

    public static CordaRPCOps createProxy(String address, String user, String password){
        return createConnection(address, user, password).getProxy();
    }

    public static CordaRPCOps createProxy(NetworkHostAndPort address, String user, String password){
        return createConnection(address, user, password).getProxy();
    }

}
