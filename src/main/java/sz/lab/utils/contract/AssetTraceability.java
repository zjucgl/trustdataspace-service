package sz.lab.utils.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicStruct;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple7;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the
 * <a href="https://github.com/hyperledger-web3j/web3j/tree/main/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 1.6.3.
 */
@SuppressWarnings("rawtypes")
public class AssetTraceability extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50612187806100206000396000f3fe60806040526004361061007b5760003560e01c80634a432a461161004e5780634a432a461461012b57806374458553146101545780638593622814610193578063cd5286d0146101d65761007b565b806303cfc76d146100805780631d5c54ab146100bd578063347047cc146100e657806342372a0f1461010f575b600080fd5b34801561008c57600080fd5b506100a760048036038101906100a291906115af565b610213565b6040516100b49190611bda565b60405180910390f35b3480156100c957600080fd5b506100e460048036038101906100df919061165c565b610382565b005b3480156100f257600080fd5b5061010d600480360381019061010891906115f0565b6106be565b005b610129600480360381019061012491906115af565b6108c8565b005b34801561013757600080fd5b50610152600480360381019061014d919061171b565b610c8e565b005b34801561016057600080fd5b5061017b6004803603810190610176919061171b565b610da0565b60405161018a93929190611b9c565b60405180910390f35b34801561019f57600080fd5b506101ba60048036038101906101b591906115af565b610ea5565b6040516101cd9796959493929190611cc4565b60405180910390f35b3480156101e257600080fd5b506101fd60048036038101906101f891906115af565b61113d565b60405161020a9190611e3f565b60405180910390f35b60606001826040516102259190611b85565b9081526020016040518091039020805480602002602001604051908101604052809291908181526020016000905b8282101561037757838290600052602060002090600302016040518060600160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820180546102dc90612053565b80601f016020809104026020016040519081016040528092919081815260200182805461030890612053565b80156103555780601f1061032a57610100808354040283529160200191610355565b820191906000526020600020905b81548152906001019060200180831161033857829003601f168201915b5050505050815260200160028201548152505081526020019060010190610253565b505050509050919050565b600080866040516103939190611b85565b908152602001604051809103902060000180546103af90612053565b9050146103f1576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016103e890611dff565b60405180910390fd5b6040518060e001604052808681526020018581526020018481526020018273ffffffffffffffffffffffffffffffffffffffff1681526020014281526020016040518060400160405280600781526020017f437265617465640000000000000000000000000000000000000000000000000081525081526020018381525060008660405161047f9190611b85565b908152602001604051809103902060008201518160000190805190602001906104a9929190611427565b5060208201518160010190805190602001906104c6929190611427565b5060408201518160020190805190602001906104e3929190611427565b5060608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506080820151816004015560a0820151816005019080519060200190610551929190611427565b5060c0820151816006015590505060018560405161056f9190611b85565b908152602001604051809103902060405180606001604052808373ffffffffffffffffffffffffffffffffffffffff1681526020016040518060400160405280600781526020017f4372656174656400000000000000000000000000000000000000000000000000815250815260200142815250908060018154018082558091505060019003906000526020600020906003020160009091909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550602082015181600101908051906020019061066f929190611427565b506040820151816002015550507fa80b61829ced791d8911bbc0e07a059c1fc5c3642d169260d1d7194f3792b2818585836040516106af93929190611c7f565b60405180910390a15050505050565b3373ffffffffffffffffffffffffffffffffffffffff166000836040516106e59190611b85565b908152602001604051809103902060030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161461076d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161076490611e1f565b60405180910390fd5b8060008360405161077e9190611b85565b908152602001604051809103902060050190805190602001906107a2929190611427565b506001826040516107b39190611b85565b908152602001604051809103902060405180606001604052803373ffffffffffffffffffffffffffffffffffffffff16815260200183815260200142815250908060018154018082558091505060019003906000526020600020906003020160009091909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550602082015181600101908051906020019061087e929190611427565b506040820151816002015550507fb75b1fec1bc9156569ae9f72fb29c6e6727b297e191d358465fff4c09d8f7d2682826040516108bc929190611c48565b60405180910390a15050565b600080826040516108d99190611b85565b9081526020016040518091039020905060008160000180546108fa90612053565b9050141561093d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161093490611d9f565b60405180910390fd5b633b9aca0081600601546109519190611f33565b3414610992576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040161098990611ddf565b60405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff168160030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff161415610a25576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610a1c90611d7f565b60405180910390fd5b60008160030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff169050338260030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055508073ffffffffffffffffffffffffffffffffffffffff166108fc349081150290604051600060405180830381858888f19350505050158015610ad7573d6000803e3d6000fd5b50600183604051610ae89190611b85565b908152602001604051809103902060405180606001604052803373ffffffffffffffffffffffffffffffffffffffff168152602001846005018054610b2c90612053565b80601f0160208091040260200160405190810160405280929190818152602001828054610b5890612053565b8015610ba55780601f10610b7a57610100808354040283529160200191610ba5565b820191906000526020600020905b815481529060010190602001808311610b8857829003601f168201915b5050505050815260200142815250908060018154018082558091505060019003906000526020600020906003020160009091909190915060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001019080519060200190610c3f929190611427565b506040820151816002015550507fe9bf8dbaa7eb0b6910401512e60afcae7734db09df0b7291b4a16e630d796e5683823334604051610c819493929190611bfc565b60405180910390a1505050565b3373ffffffffffffffffffffffffffffffffffffffff16600083604051610cb59190611b85565b908152602001604051809103902060030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614610d3d576040517f08c379a0000000000000000000000000000000000000000000000000000000008152600401610d3490611dbf565b60405180910390fd5b80600083604051610d4e9190611b85565b9081526020016040518091039020600601819055507f159e83f4712ba2552e68be9d848e49bf6dd35c24f19564ffd523b6549450a2f48282604051610d94929190611d4f565b60405180910390a15050565b6001828051602081018201805184825260208301602085012081835280955050505050508181548110610dd257600080fd5b9060005260206000209060030201600091509150508060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690806001018054610e1c90612053565b80601f0160208091040260200160405190810160405280929190818152602001828054610e4890612053565b8015610e955780601f10610e6a57610100808354040283529160200191610e95565b820191906000526020600020905b815481529060010190602001808311610e7857829003601f168201915b5050505050908060020154905083565b600081805160208101820180518482526020830160208501208183528095505050505050600091509050806000018054610ede90612053565b80601f0160208091040260200160405190810160405280929190818152602001828054610f0a90612053565b8015610f575780601f10610f2c57610100808354040283529160200191610f57565b820191906000526020600020905b815481529060010190602001808311610f3a57829003601f168201915b505050505090806001018054610f6c90612053565b80601f0160208091040260200160405190810160405280929190818152602001828054610f9890612053565b8015610fe55780601f10610fba57610100808354040283529160200191610fe5565b820191906000526020600020905b815481529060010190602001808311610fc857829003601f168201915b505050505090806002018054610ffa90612053565b80601f016020809104026020016040519081016040528092919081815260200182805461102690612053565b80156110735780601f1061104857610100808354040283529160200191611073565b820191906000526020600020905b81548152906001019060200180831161105657829003601f168201915b5050505050908060030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16908060040154908060050180546110b490612053565b80601f01602080910402602001604051908101604052809291908181526020018280546110e090612053565b801561112d5780601f106111025761010080835404028352916020019161112d565b820191906000526020600020905b81548152906001019060200180831161111057829003601f168201915b5050505050908060060154905087565b6111456114ad565b6000826040516111559190611b85565b90815260200160405180910390206040518060e001604052908160008201805461117e90612053565b80601f01602080910402602001604051908101604052809291908181526020018280546111aa90612053565b80156111f75780601f106111cc576101008083540402835291602001916111f7565b820191906000526020600020905b8154815290600101906020018083116111da57829003601f168201915b5050505050815260200160018201805461121090612053565b80601f016020809104026020016040519081016040528092919081815260200182805461123c90612053565b80156112895780601f1061125e57610100808354040283529160200191611289565b820191906000526020600020905b81548152906001019060200180831161126c57829003601f168201915b505050505081526020016002820180546112a290612053565b80601f01602080910402602001604051908101604052809291908181526020018280546112ce90612053565b801561131b5780601f106112f05761010080835404028352916020019161131b565b820191906000526020600020905b8154815290600101906020018083116112fe57829003601f168201915b505050505081526020016003820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016004820154815260200160058201805461139490612053565b80601f01602080910402602001604051908101604052809291908181526020018280546113c090612053565b801561140d5780601f106113e25761010080835404028352916020019161140d565b820191906000526020600020905b8154815290600101906020018083116113f057829003601f168201915b505050505081526020016006820154815250509050919050565b82805461143390612053565b90600052602060002090601f016020900481019282611455576000855561149c565b82601f1061146e57805160ff191683800117855561149c565b8280016001018555821561149c579182015b8281111561149b578251825591602001919060010190611480565b5b5090506114a99190611500565b5090565b6040518060e00160405280606081526020016060815260200160608152602001600073ffffffffffffffffffffffffffffffffffffffff1681526020016000815260200160608152602001600081525090565b5b80821115611519576000816000905550600101611501565b5090565b600061153061152b84611e92565b611e61565b90508281526020810184848401111561154857600080fd5b611553848285612011565b509392505050565b60008135905061156a81612123565b92915050565b600082601f83011261158157600080fd5b813561159184826020860161151d565b91505092915050565b6000813590506115a98161213a565b92915050565b6000602082840312156115c157600080fd5b600082013567ffffffffffffffff8111156115db57600080fd5b6115e784828501611570565b91505092915050565b6000806040838503121561160357600080fd5b600083013567ffffffffffffffff81111561161d57600080fd5b61162985828601611570565b925050602083013567ffffffffffffffff81111561164657600080fd5b61165285828601611570565b9150509250929050565b600080600080600060a0868803121561167457600080fd5b600086013567ffffffffffffffff81111561168e57600080fd5b61169a88828901611570565b955050602086013567ffffffffffffffff8111156116b757600080fd5b6116c388828901611570565b945050604086013567ffffffffffffffff8111156116e057600080fd5b6116ec88828901611570565b93505060606116fd8882890161159a565b925050608061170e8882890161155b565b9150509295509295909350565b6000806040838503121561172e57600080fd5b600083013567ffffffffffffffff81111561174857600080fd5b61175485828601611570565b92505060206117658582860161159a565b9150509250929050565b600061177b8383611a66565b905092915050565b61178c81611fdb565b82525050565b61179b81611f9f565b82525050565b6117aa81611f9f565b82525050565b6117b981611f8d565b82525050565b6117c881611f8d565b82525050565b60006117d982611ed2565b6117e38185611ef5565b9350836020820285016117f585611ec2565b8060005b858110156118315784840389528151611812858261176f565b945061181d83611ee8565b925060208a019950506001810190506117f9565b50829750879550505050505092915050565b600061184e82611edd565b6118588185611f06565b9350611868818560208601612020565b61187181612112565b840191505092915050565b600061188782611edd565b6118918185611f17565b93506118a1818560208601612020565b6118aa81612112565b840191505092915050565b60006118c082611edd565b6118ca8185611f28565b93506118da818560208601612020565b80840191505092915050565b60006118f3601983611f17565b91507f43616e6e6f742062757920796f7572206f776e206173736574000000000000006000830152602082019050919050565b6000611933601483611f17565b91507f417373657420646f6573206e6f742065786973740000000000000000000000006000830152602082019050919050565b6000611973601f83611f17565b91507f4f6e6c7920746865206f776e65722063616e20757064617465207072696365006000830152602082019050919050565b60006119b3601483611f17565b91507f496e73756666696369656e74207061796d656e740000000000000000000000006000830152602082019050919050565b60006119f3601483611f17565b91507f417373657420616c7265616479206578697374730000000000000000000000006000830152602082019050919050565b6000611a33602083611f17565b91507f4f6e6c7920746865206f776e65722063616e20757064617465207374617475736000830152602082019050919050565b6000606083016000830151611a7e60008601826117b0565b5060208301518482036020860152611a968282611843565b9150506040830151611aab6040860182611b67565b508091505092915050565b600060e0830160008301518482036000860152611ad38282611843565b91505060208301518482036020860152611aed8282611843565b91505060408301518482036040860152611b078282611843565b9150506060830151611b1c6060860182611792565b506080830151611b2f6080860182611b67565b5060a083015184820360a0860152611b478282611843565b91505060c0830151611b5c60c0860182611b67565b508091505092915050565b611b7081611fd1565b82525050565b611b7f81611fd1565b82525050565b6000611b9182846118b5565b915081905092915050565b6000606082019050611bb160008301866117bf565b8181036020830152611bc3818561187c565b9050611bd26040830184611b76565b949350505050565b60006020820190508181036000830152611bf481846117ce565b905092915050565b60006080820190508181036000830152611c16818761187c565b9050611c256020830186611783565b611c3260408301856117bf565b611c3f6060830184611b76565b95945050505050565b60006040820190508181036000830152611c62818561187c565b90508181036020830152611c76818461187c565b90509392505050565b60006060820190508181036000830152611c99818661187c565b90508181036020830152611cad818561187c565b9050611cbc6040830184611783565b949350505050565b600060e0820190508181036000830152611cde818a61187c565b90508181036020830152611cf2818961187c565b90508181036040830152611d06818861187c565b9050611d1560608301876117a1565b611d226080830186611b76565b81810360a0830152611d34818561187c565b9050611d4360c0830184611b76565b98975050505050505050565b60006040820190508181036000830152611d69818561187c565b9050611d786020830184611b76565b9392505050565b60006020820190508181036000830152611d98816118e6565b9050919050565b60006020820190508181036000830152611db881611926565b9050919050565b60006020820190508181036000830152611dd881611966565b9050919050565b60006020820190508181036000830152611df8816119a6565b9050919050565b60006020820190508181036000830152611e18816119e6565b9050919050565b60006020820190508181036000830152611e3881611a26565b9050919050565b60006020820190508181036000830152611e598184611ab6565b905092915050565b6000604051905081810181811067ffffffffffffffff82111715611e8857611e876120e3565b5b8060405250919050565b600067ffffffffffffffff821115611ead57611eac6120e3565b5b601f19601f8301169050602081019050919050565b6000819050602082019050919050565b600081519050919050565b600081519050919050565b6000602082019050919050565b600082825260208201905092915050565b600082825260208201905092915050565b600082825260208201905092915050565b600081905092915050565b6000611f3e82611fd1565b9150611f4983611fd1565b9250817fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0483118215151615611f8257611f81612085565b5b828202905092915050565b6000611f9882611fb1565b9050919050565b6000611faa82611fb1565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b6000611fe682611fed565b9050919050565b6000611ff882611fff565b9050919050565b600061200a82611fb1565b9050919050565b82818337600083830152505050565b60005b8381101561203e578082015181840152602081019050612023565b8381111561204d576000848401525b50505050565b6000600282049050600182168061206b57607f821691505b6020821081141561207f5761207e6120b4565b5b50919050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052602260045260246000fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6000601f19601f8301169050919050565b61212c81611f9f565b811461213757600080fd5b50565b61214381611fd1565b811461214e57600080fd5b5056fea264697066735822122018a42b0440fb7bf2ed7eac7a60d2856c44e9ab8ff3f3e0550d0bf5bfb901fc6c64736f6c63430008000033\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_ASSETHISTORY = "assetHistory";

    public static final String FUNC_ASSETS = "assets";

    public static final String FUNC_REGISTERASSET = "registerAsset";

    public static final String FUNC_UPDATEPRICE = "updatePrice";

    public static final String FUNC_PURCHASEASSET = "purchaseAsset";

    public static final String FUNC_UPDATESTATUS = "updateStatus";

    public static final String FUNC_GETASSET = "getAsset";

    public static final String FUNC_GETASSETHISTORY = "getAssetHistory";

    public static final Event ASSETREGISTERED_EVENT = new Event("AssetRegistered",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}));
    ;

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event PRICEUPDATED_EVENT = new Event("PriceUpdated",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event STATUSUPDATED_EVENT = new Event("StatusUpdated",
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}));
    ;

    @Deprecated
    protected AssetTraceability(String contractAddress, Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected AssetTraceability(String contractAddress, Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected AssetTraceability(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected AssetTraceability(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<AssetRegisteredEventResponse> getAssetRegisteredEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(ASSETREGISTERED_EVENT, transactionReceipt);
        ArrayList<AssetRegisteredEventResponse> responses = new ArrayList<AssetRegisteredEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            AssetRegisteredEventResponse typedResponse = new AssetRegisteredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.name = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static AssetRegisteredEventResponse getAssetRegisteredEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(ASSETREGISTERED_EVENT, log);
        AssetRegisteredEventResponse typedResponse = new AssetRegisteredEventResponse();
        typedResponse.log = log;
        typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.name = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.owner = (String) eventValues.getNonIndexedValues().get(2).getValue();
        return typedResponse;
    }

    public Flowable<AssetRegisteredEventResponse> assetRegisteredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getAssetRegisteredEventFromLog(log));
    }

    public Flowable<AssetRegisteredEventResponse> assetRegisteredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(ASSETREGISTERED_EVENT));
        return assetRegisteredEventFlowable(filter);
    }

    public static List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.from = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.to = (String) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static OwnershipTransferredEventResponse getOwnershipTransferredEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
        OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
        typedResponse.log = log;
        typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.from = (String) eventValues.getNonIndexedValues().get(1).getValue();
        typedResponse.to = (String) eventValues.getNonIndexedValues().get(2).getValue();
        typedResponse.price = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
        return typedResponse;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getOwnershipTransferredEventFromLog(log));
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public static List<PriceUpdatedEventResponse> getPriceUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PRICEUPDATED_EVENT, transactionReceipt);
        ArrayList<PriceUpdatedEventResponse> responses = new ArrayList<PriceUpdatedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            PriceUpdatedEventResponse typedResponse = new PriceUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.newPrice = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PriceUpdatedEventResponse getPriceUpdatedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PRICEUPDATED_EVENT, log);
        PriceUpdatedEventResponse typedResponse = new PriceUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.newPrice = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PriceUpdatedEventResponse> priceUpdatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPriceUpdatedEventFromLog(log));
    }

    public Flowable<PriceUpdatedEventResponse> priceUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PRICEUPDATED_EVENT));
        return priceUpdatedEventFlowable(filter);
    }

    public static List<StatusUpdatedEventResponse> getStatusUpdatedEvents(
            TransactionReceipt transactionReceipt) {
        List<EventValuesWithLog> valueList = staticExtractEventParametersWithLog(STATUSUPDATED_EVENT, transactionReceipt);
        ArrayList<StatusUpdatedEventResponse> responses = new ArrayList<StatusUpdatedEventResponse>(valueList.size());
        for (EventValuesWithLog eventValues : valueList) {
            StatusUpdatedEventResponse typedResponse = new StatusUpdatedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.status = (String) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static StatusUpdatedEventResponse getStatusUpdatedEventFromLog(Log log) {
        EventValuesWithLog eventValues = staticExtractEventParametersWithLog(STATUSUPDATED_EVENT, log);
        StatusUpdatedEventResponse typedResponse = new StatusUpdatedEventResponse();
        typedResponse.log = log;
        typedResponse.assetId = (String) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.status = (String) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<StatusUpdatedEventResponse> statusUpdatedEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getStatusUpdatedEventFromLog(log));
    }

    public Flowable<StatusUpdatedEventResponse> statusUpdatedEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(STATUSUPDATED_EVENT));
        return statusUpdatedEventFlowable(filter);
    }

    public RemoteFunctionCall<Tuple3<String, String, BigInteger>> assetHistory(String param0,
            BigInteger param1) {
        final Function function = new Function(FUNC_ASSETHISTORY,
                Arrays.<Type>asList(new Utf8String(param0),
                new Uint256(param1)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple3<String, String, BigInteger>>(function,
                new Callable<Tuple3<String, String, BigInteger>>() {
                    @Override
                    public Tuple3<String, String, BigInteger> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, BigInteger>(
                                (String) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (BigInteger) results.get(2).getValue());
                    }
                });
    }

    public RemoteFunctionCall<Tuple7<String, String, String, String, BigInteger, String, BigInteger>> assets(
            String param0) {
        final Function function = new Function(FUNC_ASSETS,
                Arrays.<Type>asList(new Utf8String(param0)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Utf8String>() {}, new TypeReference<Uint256>() {}));
        return new RemoteFunctionCall<Tuple7<String, String, String, String, BigInteger, String, BigInteger>>(function,
                new Callable<Tuple7<String, String, String, String, BigInteger, String, BigInteger>>() {
                    @Override
                    public Tuple7<String, String, String, String, BigInteger, String, BigInteger> call(
                            ) throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple7<String, String, String, String, BigInteger, String, BigInteger>(
                                (String) results.get(0).getValue(),
                                (String) results.get(1).getValue(),
                                (String) results.get(2).getValue(),
                                (String) results.get(3).getValue(),
                                (BigInteger) results.get(4).getValue(),
                                (String) results.get(5).getValue(),
                                (BigInteger) results.get(6).getValue());
                    }
                });
    }

    public RemoteFunctionCall<TransactionReceipt> registerAsset(String _assetId, String _name,
            String _description, BigInteger _price, String _provider) {
        final Function function = new Function(
                FUNC_REGISTERASSET,
                Arrays.<Type>asList(new Utf8String(_assetId),
                new Utf8String(_name),
                new Utf8String(_description),
                new Uint256(_price),
                new Address(160, _provider)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> updatePrice(String _assetId,
            BigInteger _newPrice) {
        final Function function = new Function(
                FUNC_UPDATEPRICE,
                Arrays.<Type>asList(new Utf8String(_assetId),
                new Uint256(_newPrice)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> purchaseAsset(String _assetId,
            BigInteger weiValue) {
        final Function function = new Function(
                FUNC_PURCHASEASSET,
                Arrays.<Type>asList(new Utf8String(_assetId)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteFunctionCall<TransactionReceipt> updateStatus(String _assetId, String _newStatus) {
        final Function function = new Function(
                FUNC_UPDATESTATUS,
                Arrays.<Type>asList(new Utf8String(_assetId),
                new Utf8String(_newStatus)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<Asset> getAsset(String _assetId) {
        final Function function = new Function(FUNC_GETASSET,
                Arrays.<Type>asList(new Utf8String(_assetId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<Asset>() {}));
        return executeRemoteCallSingleValueReturn(function, Asset.class);
    }

    public RemoteFunctionCall<List> getAssetHistory(String _assetId) {
        final Function function = new Function(FUNC_GETASSETHISTORY,
                Arrays.<Type>asList(new Utf8String(_assetId)),
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<AssetHistory>>() {}));
        return new RemoteFunctionCall<List>(function,
                new Callable<List>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    @Deprecated
    public static AssetTraceability load(String contractAddress, Web3j web3j,
            Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new AssetTraceability(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static AssetTraceability load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new AssetTraceability(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static AssetTraceability load(String contractAddress, Web3j web3j,
            Credentials credentials, ContractGasProvider contractGasProvider) {
        return new AssetTraceability(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static AssetTraceability load(String contractAddress, Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new AssetTraceability(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<AssetTraceability> deploy(Web3j web3j, Credentials credentials,
            ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AssetTraceability.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AssetTraceability> deploy(Web3j web3j, Credentials credentials,
            BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AssetTraceability.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<AssetTraceability> deploy(Web3j web3j,
            TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(AssetTraceability.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<AssetTraceability> deploy(Web3j web3j,
            TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(AssetTraceability.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

//    public static void linkLibraries(List<Contract.LinkReference> references) {
//        librariesLinkedBinary = linkBinaryWithReferences(BINARY, references);
//    }

    private static String getDeploymentBinary() {
        if (librariesLinkedBinary != null) {
            return librariesLinkedBinary;
        } else {
            return BINARY;
        }
    }

    public static class Asset extends DynamicStruct {
        public String assetId;

        public String name;

        public String description;

        public String currentOwner;

        public BigInteger createdAt;

        public String status;

        public BigInteger price;

        public Asset(String assetId, String name, String description, String currentOwner,
                BigInteger createdAt, String status, BigInteger price) {
            super(new Utf8String(assetId),
                    new Utf8String(name),
                    new Utf8String(description),
                    new Address(160, currentOwner),
                    new Uint256(createdAt),
                    new Utf8String(status),
                    new Uint256(price));
            this.assetId = assetId;
            this.name = name;
            this.description = description;
            this.currentOwner = currentOwner;
            this.createdAt = createdAt;
            this.status = status;
            this.price = price;
        }

        public Asset(Utf8String assetId, Utf8String name, Utf8String description,
                Address currentOwner, Uint256 createdAt, Utf8String status, Uint256 price) {
            super(assetId, name, description, currentOwner, createdAt, status, price);
            this.assetId = assetId.getValue();
            this.name = name.getValue();
            this.description = description.getValue();
            this.currentOwner = currentOwner.getValue();
            this.createdAt = createdAt.getValue();
            this.status = status.getValue();
            this.price = price.getValue();
        }
    }

    public static class AssetHistory extends DynamicStruct {
        public String owner;

        public String status;

        public BigInteger timestamp;

        public AssetHistory(String owner, String status, BigInteger timestamp) {
            super(new Address(160, owner),
                    new Utf8String(status),
                    new Uint256(timestamp));
            this.owner = owner;
            this.status = status;
            this.timestamp = timestamp;
        }

        public AssetHistory(Address owner, Utf8String status, Uint256 timestamp) {
            super(owner, status, timestamp);
            this.owner = owner.getValue();
            this.status = status.getValue();
            this.timestamp = timestamp.getValue();
        }
    }

    public static class AssetRegisteredEventResponse extends BaseEventResponse {
        public String assetId;

        public String name;

        public String owner;
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String assetId;

        public String from;

        public String to;

        public BigInteger price;
    }

    public static class PriceUpdatedEventResponse extends BaseEventResponse {
        public String assetId;

        public BigInteger newPrice;
    }

    public static class StatusUpdatedEventResponse extends BaseEventResponse {
        public String assetId;

        public String status;
    }
}
