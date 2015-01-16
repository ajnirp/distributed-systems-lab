
import org.acplt.oncrpc.*;
import java.net.InetAddress;
import java.io.IOException;
import java.io.*;

public class Bank {

    static int opt;
    public static void main(String [] args) {
        //create an rpc client
        getAccTypeClient client = null;
        try {
            client = new getAccTypeClient(InetAddress.getByName(args[0]),
                                    OncRpcProtocols.ONCRPC_TCP);
        }
        catch (Exception e) {
            System.out.println("infoline: error when creating RPC client:");
            e.printStackTrace(System.out);
        }

        client.getClient().setTimeout(300*1000);

        System.out.println("Making request to server...");

    	// make request object
    	acc_id_num account_id = new acc_id_num ();
        String requested_method = args[1];

        String output_filename = "output.txt";
        try {
            switch (requested_method) {
                case "GET_ACC_TYPE":
                    account_id.value = args[2];
                    byte res = client.GET_ACC_TYPE_1(account_id);
                    System.out.println("Account type is:"+ (char)res);
                    break;
                case "GET_BALANCE":
                    account_id.value = args[2];
                    int balance = client.GET_BALANCE_1(account_id).value;
                    if (balance < 0) {
                        System.err.println("no account found");
                    } else {
                        System.out.println(account_id.value + " " + balance);
                    }
                    break;
                case "SET_BALANCE":
                    account_id.value = args[2];
                    acc_balance new_amount = new acc_balance();
                    new_amount.value = Integer.parseInt(args[3]);
                    acc_balance old_balance = client.SET_BALANCE_1(account_id, new_amount);
                    if (old_balance.value < 0) {
                        System.err.println("error setting the balance of " + account_id.value);
                    } else {
                        System.out.println(account_id.value + " " + old_balance.value + " " + new_amount.value);
                    }
                    break;
                case "TRANSACTION":
                    acc_id_num src_acc_id = new acc_id_num();
                    acc_id_num dst_acc_id = new acc_id_num();

                    src_acc_id.value = args[2];
                    dst_acc_id.value = args[3];

                    acc_balance x = new acc_balance();
                    x.value = Integer.parseInt(args[4]);

                    transaction_pair result = client.TRANSACTION_1(src_acc_id, dst_acc_id, x);

                    if (x.value != 0 && result.src_old == result.src_new) {
                        System.err.println("error!");
                    } else {
                        System.out.println(src_acc_id.value + " " + result.src_old + " " + result.src_new);
                        System.out.println(dst_acc_id.value + " " + result.dst_old + " " + result.dst_new);
                    }

                    break;
                case "GET_TRANSACTION_HISTORY":
                    account_id.value = args[2];
                    history hist = new history();
                    hist = client.GET_TRANSACTION_HISTORY_1(account_id);
                    for (int i = 0 ; i < 50 ; i++) {
                        if (hist.value[i] == 0) {
                            break;
                        }
                        System.out.println(account_id.value + " " + hist.value[i]);
                    }
                    break;
            }
        }
        catch (Exception e) {
            System.out.println("Error contacting server");
            e.printStackTrace(System.out);
            return;
        }

        try {
            client.close();
        } catch ( Exception e ) {
            System.out.println("infoline: error when closing client:");
            e.printStackTrace(System.out);
        }
        client = null;
    }
}
