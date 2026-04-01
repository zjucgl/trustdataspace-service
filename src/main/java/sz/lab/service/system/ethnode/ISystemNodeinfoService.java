package sz.lab.service.system.ethnode;

import sz.lab.dto.system.OperateResultDTO;
import sz.lab.entity.system.SystemNodeinfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.math.BigInteger;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author master
 * @since 2025-04-15
 */
public interface ISystemNodeinfoService extends IService<SystemNodeinfo> {
    /**
     * 获取区块高度
     *
     * @return {@link BigInteger }
     * @throws IOException
     */
    BigInteger getBlockNumber() throws IOException;

    /**
     * 获取并更新节点列表
     * @return {@link String }
     * @throws IOException
     */
    OperateResultDTO getMinerList() throws IOException;

    /**
     *  查询返回数据库列表
     * @param nodeName 查询名字
     * @return {@link OperateResultDTO }
     * @throws IOException
     */
    OperateResultDTO getNodeList(String nodeName) throws IOException;

    /**
     * 新增节点
     * @param nodeEnode
     * @return {@link OperateResultDTO }
     */
    OperateResultDTO addNodeInfo(String nodeEnode);
}
