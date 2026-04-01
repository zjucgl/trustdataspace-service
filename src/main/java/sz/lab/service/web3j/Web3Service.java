package sz.lab.service.web3j;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public interface Web3Service {
    /**
     * 开启挖矿
     * @param threadCount
     * @return boolean
     * @throws IOException
     */
    boolean startMining(int threadCount) throws IOException;

    /**
     * 停止挖矿
     * @return boolean
     * @throws IOException
     */
    boolean stopMining() throws IOException;

    /**
     * 获取矿工余额
     * @param address
     * @return {@link BigDecimal }
     * @throws IOException
     */
    BigDecimal getBalance(String address) throws IOException;
}
