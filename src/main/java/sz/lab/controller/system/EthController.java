package sz.lab.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sz.lab.controller.BaseController;
import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.orga.user.UserEntity;
import sz.lab.mapper.orga.user.UserMapper;
import sz.lab.service.system.ethnode.ISystemNodeinfoService;
import sz.lab.service.trace.AssetTraceService;
import sz.lab.service.web3j.Web3Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;


@RestController
@RequestMapping("/eth")
public class EthController extends BaseController {

    @Resource
    private Web3Service web3Service;

    @Resource
    private ISystemNodeinfoService systemNodeinfoService;
    @Autowired
    private UserMapper userMapper;

    @Resource
    private AssetTraceService assetTraceService;

    @RequestMapping("/start")
    public boolean startMining() {
        try {
            boolean result = web3Service.startMining(32);
            return result;
        } catch (NullPointerException e) {
            // 可以添加日志记录：logger.warn("Start mining failed: null pointer", e);
            return true;
        } catch (Exception e) {
            // 可选：处理其它异常，防止服务报错崩溃
            return false;
        }
    }


    @RequestMapping("/stop")
    public boolean stopMining() throws IOException {
        try {
            boolean result = web3Service.stopMining();
            return result;
        } catch (NullPointerException e) {
            // 可以添加日志记录：logger.warn("Start mining failed: null pointer", e);
            return true;
        } catch (Exception e) {
            // 可选：处理其它异常，防止服务报错崩溃
            return false;
        }
    }

    @RequestMapping("/balance")
    public BigDecimal getBalance() throws IOException {
        UserEntity user = userMapper.selectUserById(userId.get());
        String address = user.getEthAccount();
        BigDecimal balance = web3Service.getBalance(address);
        //保留小数点后1位
        balance = balance.setScale(1, BigDecimal.ROUND_HALF_UP);
        return balance;
    }

    @RequestMapping("/get/BlockNumber")
    public BigInteger getBlockNumber() throws IOException {
        BigInteger blockNumber =  systemNodeinfoService.getBlockNumber();
        return blockNumber;
    }

    //获取并节点信息
    @RequestMapping("/get/MinerList")
    public OperateResultDTO getMinerList() throws IOException {
        return systemNodeinfoService.getMinerList();
    }

    //获取数据库节点信息
    @RequestMapping("/get/NodeList")
    public OperateResultDTO getNodeList(@RequestParam("nodeName") String nodeName) throws IOException {
        return systemNodeinfoService.getNodeList(nodeName);
    }

    //确认用户资金是否足够
    @RequestMapping("/check/balance")
    public OperateResultDTO checkBalance(@RequestParam("assetId") String assetId) throws Exception {
        Integer userid = userId.get();
        return assetTraceService.checkBalance(assetId,userid);
    }

    @RequestMapping("/add/nodeInfo")
    public OperateResultDTO addNodeInfo(@RequestBody String nodeEnode) {
        return systemNodeinfoService.addNodeInfo(nodeEnode);
    }
}
