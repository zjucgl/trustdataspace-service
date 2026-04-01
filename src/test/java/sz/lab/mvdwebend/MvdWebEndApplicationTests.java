package sz.lab.mvdwebend;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.web3j.tx.gas.StaticGasProvider;
import sz.lab.entity.system.IAssetEntity;
import sz.lab.entity.system.IpFileEntity;
import sz.lab.mapper.system.asset.IAssetMapper;
import sz.lab.mapper.system.file.IpFileMapper;
import sz.lab.service.system.file.IpFileService;
import sz.lab.service.system.qrcode.QRService;
import sz.lab.service.trace.AssetTraceService;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;

@SpringBootTest
class MvdWebEndApplicationTests {

    @Resource
    private IpFileMapper baseMapper;
    @Resource
    private IAssetMapper ibaseMapper;
    @Resource
    private QRService qrService;
    @Resource
    private AssetTraceService assetTraceService;
    @Resource
    private IpFileService ipFileService;


    StaticGasProvider gasProvider = new StaticGasProvider(BigInteger.valueOf(20_000_000_000L), BigInteger.valueOf(8_000_000L));
    String contractAddress = "0x95405bacc296ac42b8DbFB37201aF4316c9d9983";
    @Test
    void contextLoads() {
       // System.out.println(ipFileService.getIpFile());
    }
    // 批量导入资产
    @Test
    public void batchImportAsset() {
//        List<IpFileEntity> fileEntityList = baseMapper.selectList(Wrappers.lambdaQuery(IpFileEntity.class));
//        for (IpFileEntity fileEntity : fileEntityList) {
//            IAssetEntity entity= new IAssetEntity();
//            entity.setAssId(fileEntity.getName());
//            entity.setAssPrice(60000l);
//            entity.setAssQuantity(100l);
//            entity.setAssDescription("测试");
//            entity.setUserId(3);
//            entity.setIsFirst(1);
//            entity.setPath(fileEntity.getPath());
//            ibaseMapper.insert(entity);
//        }
    }

    @Test
    public void testpdf() throws Exception{
//        //读取resources目录下input.pdf文件
//        String inputFile = URLDecoder.decode("E:\\Users\\master\\Desktop\\题目1\\input.pdf", "UTF-8");
//        PDDocument pdDocument = PDDocument.load(new File(inputFile));
//        PDFTextStripper pdfTextStripper = new PDFTextStripper();
//        //读取pdf中所有的文件
//        String fullText = pdfTextStripper.getText(pdDocument);
//        System.out.println(fullText);

//        qrService.generateCertificate("http://211.91.61.25:9007/index.html", "E:\\Users\\master\\Desktop\\题目1\\output.pdf");
    }
    //注册资产测试
    @Test
    public void testqr() throws Exception {
//        assetTraceService.purchaseAsset("双尾楼帆船 -莫高窟第468窟-晚唐", 2);
//        assetTraceService.registerAsset("asset1230000", "测试", "测试", BigInteger.valueOf(1000), "0xe176ba6bcec3761fe9834df8b9e3c6f90faac1a9");
//        AssetTraceability.Asset asset = assetTraceService.getAsset("asset1230000");
//        System.out.println(asset);
//        List receipt =assetTraceService.getAssetHistory("asset-1",3);
//        System.out.println(receipt);
    }

//    //查询资产
//    @Test
//    public void testqr2() throws Exception {
////        AssetTraceability.Asset asset = assetTraceService.getAsset("灯轮-莫高窟第146窟-五代1743589630192");
////        System.out.println(asset);
////        AssetTraceability contract = reloadContract(contractAddress, "http://192.168.3.115:8545", " ");
////        TransactionReceipt receipt = contract.purchaseAsset("灯轮-莫高窟第146窟-五代1743589630192", BigInteger.valueOf(1000L *1000000000)).sendAsync().get();
//        List receipt = assetTraceService.getAssetHistory("灯轮-莫高窟第146窟-五代1743589630192");
////        System.out.println("Ownership Transferred: " + receipt.getTransactionHash());
//    }
//
//    private AssetTraceability reloadContract(String contractAddress, String web3j, String ethAccount) throws Exception {
//        // 重新加载合约
//        Web3j web3java = Web3j.build(new HttpService(web3j));
//        //搜索密码库中搜索用户密钥
//        Credentials credential = Credentials.create("c1fbad0f50cf065ea76bdec0a315a0132d5ee43dba5874ae3b21c0e808980836");
////        Credentials credential = Credentials.create(credentials);
//        return AssetTraceability.load(contractAddress, web3java, credential, gasProvider);
//    }

    /**
     * 区块链创建钱包账户，离线
     * @throws Exception
     */
    @Test
    public void testqr3() throws Exception {
//        AssetTraceability contract = reloadContract(contractAddress, "http://192.168.3.115:8545", " ");
//        TransactionReceipt receipt = contract.purchaseAsset()
    }
}
