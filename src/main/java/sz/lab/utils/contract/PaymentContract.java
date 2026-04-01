package sz.lab.utils.contract;

import io.reactivex.Flowable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
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
public class PaymentContract extends Contract {
    public static final String BINARY = "0x608060405234801561001057600080fd5b50610385806100206000396000f3fe60806040526004361061001e5760003560e01c8063935f4c1814610023575b600080fd5b61003d600480360381019061003891906101a0565b61003f565b005b803414610081576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100789061026b565b60405180910390fd5b600081116100c4576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004016100bb9061028b565b60405180910390fd5b8173ffffffffffffffffffffffffffffffffffffffff166108fc829081150290604051600060405180830381858888f1935050505015801561010a573d6000803e3d6000fd5b508173ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fdd6728a03424f6c670ed45cc0c1be3a6cbcc811a1b4ac6aa6e60558bb34e084b428460405161016a9291906102ab565b60405180910390a35050565b60008135905061018581610321565b92915050565b60008135905061019a81610338565b92915050565b600080604083850312156101b357600080fd5b60006101c185828601610176565b92505060206101d28582860161018b565b9150509250929050565b60006101e9601d836102d4565b91507f53656e7420455448206d757374206d61746368207468652070726963650000006000830152602082019050919050565b6000610229601c836102d4565b91507f5072696365206d7573742062652067726561746572207468616e2030000000006000830152602082019050919050565b61026581610317565b82525050565b60006020820190508181036000830152610284816101dc565b9050919050565b600060208201905081810360008301526102a48161021c565b9050919050565b60006040820190506102c0600083018561025c565b6102cd602083018461025c565b9392505050565b600082825260208201905092915050565b60006102f0826102f7565b9050919050565b600073ffffffffffffffffffffffffffffffffffffffff82169050919050565b6000819050919050565b61032a816102e5565b811461033557600080fd5b50565b61034181610317565b811461034c57600080fd5b5056fea26469706673582212206c47f9fcfd3f3f5fc1fff0aacc21b3eaacb8b03ca2a940b485dc56d2b48fedd764736f6c63430008000033\n";

    private static String librariesLinkedBinary;

    public static final String FUNC_SENDPAYMENT = "sendPayment";

    public static final Event PAYMENTSENT_EVENT = new Event("PaymentSent",
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected PaymentContract(String contractAddress, Web3j web3j, Credentials credentials,
                              BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected PaymentContract(String contractAddress, Web3j web3j, Credentials credentials,
                              ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected PaymentContract(String contractAddress, Web3j web3j,
                              TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected PaymentContract(String contractAddress, Web3j web3j,
                              TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static List<PaymentSentEventResponse> getPaymentSentEvents(
            TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = staticExtractEventParametersWithLog(PAYMENTSENT_EVENT, transactionReceipt);
        ArrayList<PaymentSentEventResponse> responses = new ArrayList<PaymentSentEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            PaymentSentEventResponse typedResponse = new PaymentSentEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
            typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public static PaymentSentEventResponse getPaymentSentEventFromLog(Log log) {
        Contract.EventValuesWithLog eventValues = staticExtractEventParametersWithLog(PAYMENTSENT_EVENT, log);
        PaymentSentEventResponse typedResponse = new PaymentSentEventResponse();
        typedResponse.log = log;
        typedResponse.from = (String) eventValues.getIndexedValues().get(0).getValue();
        typedResponse.to = (String) eventValues.getIndexedValues().get(1).getValue();
        typedResponse.timestamp = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
        typedResponse.amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
        return typedResponse;
    }

    public Flowable<PaymentSentEventResponse> paymentSentEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(log -> getPaymentSentEventFromLog(log));
    }

    public Flowable<PaymentSentEventResponse> paymentSentEventFlowable(
            DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(PAYMENTSENT_EVENT));
        return paymentSentEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> sendPayment(String recipient, BigInteger price,
                                                              BigInteger weiValue) {
        final Function function = new Function(
                FUNC_SENDPAYMENT,
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, recipient),
                        new org.web3j.abi.datatypes.generated.Uint256(price)),
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    @Deprecated
    public static PaymentContract load(String contractAddress, Web3j web3j, Credentials credentials,
                                       BigInteger gasPrice, BigInteger gasLimit) {
        return new PaymentContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static PaymentContract load(String contractAddress, Web3j web3j,
                                       TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new PaymentContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static PaymentContract load(String contractAddress, Web3j web3j, Credentials credentials,
                                       ContractGasProvider contractGasProvider) {
        return new PaymentContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static PaymentContract load(String contractAddress, Web3j web3j,
                                       TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new PaymentContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<PaymentContract> deploy(Web3j web3j, Credentials credentials,
                                                     ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PaymentContract.class, web3j, credentials, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PaymentContract> deploy(Web3j web3j, Credentials credentials,
                                                     BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PaymentContract.class, web3j, credentials, gasPrice, gasLimit, getDeploymentBinary(), "");
    }

    public static RemoteCall<PaymentContract> deploy(Web3j web3j,
                                                     TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return deployRemoteCall(PaymentContract.class, web3j, transactionManager, contractGasProvider, getDeploymentBinary(), "");
    }

    @Deprecated
    public static RemoteCall<PaymentContract> deploy(Web3j web3j,
                                                     TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(PaymentContract.class, web3j, transactionManager, gasPrice, gasLimit, getDeploymentBinary(), "");
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

    public static class PaymentSentEventResponse extends BaseEventResponse {
        public String from;

        public String to;

        public BigInteger timestamp;

        public BigInteger amount;
    }
}
