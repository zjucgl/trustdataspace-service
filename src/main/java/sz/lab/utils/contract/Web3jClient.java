package sz.lab.utils.contract;

import org.springframework.beans.factory.annotation.Value;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.http.HttpService;

public class Web3jClient {

//    @Value("${gethAddress}")
    private static String NODE_URL = "http://192.168.14.3:8545";

    private static Web3j web3j;
    private static Admin admin;
    private static HttpService httpService; // 新增

    public static Web3j getWeb3jInstance() {
        if (web3j == null) {
            web3j = Web3j.build(new HttpService(NODE_URL));
        }
        return web3j;
    }

    public static Admin getAdmin() {
        if (admin == null) {
            admin = Admin.build(new HttpService(NODE_URL));
        }
        return admin;
    }

    // 新增方法

//    Geth geth = Geth.build(new HttpService("http://localhost:8545/"));
//    geth.minerStart(1).send();
//    System.out.println("挖矿完成");


}
