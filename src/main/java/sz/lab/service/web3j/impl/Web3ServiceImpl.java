package sz.lab.service.web3j.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.BooleanResponse;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.admin.AdminPeers;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import sz.lab.service.web3j.Web3Service;
import sz.lab.utils.BlockChainUtil;
import sz.lab.utils.contract.Web3jClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;

@Service
public class Web3ServiceImpl implements Web3Service {
    @Autowired
    private BlockChainUtil blockChain;
    @Value("${gethAddress}")
    private String ethAddress;

    // 1. 连接到 Geth 节点
    Web3j web3j = Web3jClient.getWeb3jInstance();
    Admin admin = Web3jClient.getAdmin();
    HttpService httpService = getHttpService(ethAddress); // 直接获取

    @Override
    public boolean startMining(int threadCount) throws IOException {
        System.out.println("请求启动挖矿，线程数: " + threadCount);
        Request<?, BooleanResponse> request = new Request<>(
                "miner_start",
                Collections.singletonList(threadCount),
                httpService,
                BooleanResponse.class
        );
        BooleanResponse response = request.send();
        System.out.println("挖矿启动响应: " + response);  // 打印响应内容
        return response.getResult();
    }

    @Override
    public boolean stopMining() throws IOException {
        Request<?, BooleanResponse> request = new Request<>(
                "miner_stop",
                Collections.emptyList(),
                httpService,
                BooleanResponse.class
        );
        return request.send().getResult();
    }

    @Override
    public BigDecimal getBalance(String address) throws IOException {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(new BigDecimal(balance.getBalance()), Convert.Unit.ETHER);
    }

    private HttpService getHttpService(String address) {
        if (httpService == null) {
            httpService = new HttpService(address);
        }
        return httpService;
    }
}
