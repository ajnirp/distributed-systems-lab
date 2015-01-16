import org.acplt.oncrpc.*;
import org.acplt.oncrpc.server.*;
import java.io.*;
import java.util.*;

public class getAccTypeInterface extends getAccTypeServerStub {

    private static String datafilename;
    public static FileWriter writer;

    private static boolean loggingEnabled;

    public getAccTypeInterface()
           throws OncRpcException, IOException {
        super();
    }

    public void run()
           throws OncRpcException, IOException {
     System.out.println("server unregister tports");
        unregister(transports);
     System.out.println("server register new tports");
        register(transports);
     System.out.println("run server tports");
        run(transports);
     System.out.println("server end run");
        unregister(transports);
        close(transports);
    }

    public history GET_TRANSACTION_HISTORY_1(acc_id_num id) {

    	history result = new history();

    	int temp_history[] = new int[50];
    	Arrays.fill(temp_history, 0);
    	int current_index = 0;

    	BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader("output.txt"));
			String line = null;
			while((line = in.readLine()) != null){
		     	StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() == 3){
					if(st.nextToken().equals(id.value)){
						int old_balance = Integer.parseInt(st.nextToken());
						int new_balance = Integer.parseInt(st.nextToken());
						int change = new_balance - old_balance;
						// System.out.println("old_balance " + old_balance + " new_balance " + new_balance + " change " + change);
						temp_history[current_index] = change;
						current_index++;
					}
				}
			}
		}
		catch (Exception e){
            System.out.println("error Processing request for "+ id.value );
		}

		result.value = temp_history;

    	return result;
    }

    public transaction_pair TRANSACTION_1(acc_id_num src, acc_id_num dst, acc_balance x) {
    	transaction_pair result = new transaction_pair();

    	loggingEnabled = false;
    	acc_balance src_old = this.GET_BALANCE_1(src);
    	acc_balance dst_old = this.GET_BALANCE_1(dst);
    	loggingEnabled = true;

    	if (src_old.value == -1 || dst_old.value == -1) {
    		result.src_old = -1;
    		result.src_new = -1;
    		result.dst_old = -1;
    		result.dst_new = -1;
    		return result;
    	}

		result.src_old = src_old.value;
		result.dst_old = dst_old.value;

    	if (src_old.value >= x.value) {
    		acc_balance temp = new acc_balance();

    		temp.value = src_old.value - x.value;
    		result.src_new = temp.value;
    		this.SET_BALANCE_1(src, temp);

    		temp.value = dst_old.value + x.value;
    		result.dst_new = temp.value;
    		this.SET_BALANCE_1(dst, temp);
    	}

    	return result;
    }

    public acc_balance SET_BALANCE_1(acc_id_num id, acc_balance new_amount) {
		try { writer = new FileWriter("output.txt", true); } catch (Exception e) {}

    	// System.out.println("Processing SET_BALANCE for account having id: " + id.value );
    	BufferedReader in = null;
    	StringBuilder builder = new StringBuilder();
    	acc_balance old_balance = new acc_balance();
    	old_balance.value = -1;

		try{
			in = new BufferedReader(new FileReader(datafilename));
			String line = null;
			while((line = in.readLine()) != null){
		     	StringTokenizer st = new StringTokenizer(line);
				// data file must have all 3 data fields
				if (st.countTokens() == 3){
					String token1 = st.nextToken();
					String token2 = st.nextToken();
					String token3 = st.nextToken();
					if(token1.equals(id.value)){
						old_balance.value = Integer.parseInt(token3);
						builder.append(token1 + " " + token2 + " " + Integer.toString(new_amount.value));
						writer.write(id.value + " " + old_balance.value + " " + new_amount.value + "\n");
						writer.close();
					} else {
						builder.append(line);
					}
					builder.append("\n");
				}
			}
			in.close();
			FileWriter writer = new FileWriter(datafilename);
			writer.write(builder.toString());
			writer.close();
		}
		catch (Exception e){
            System.out.println("error Processing request for "+ id.value );
		}
        return old_balance;
    }

    public acc_balance GET_BALANCE_1(acc_id_num arg1) {
		try { writer = new FileWriter("output.txt", true); } catch (Exception e) {}

    	// System.out.println("Processing GET_BALANCE for account having id: "+ arg1.value );
    	BufferedReader in = null;
    	acc_balance balance = new acc_balance();
    	balance.value = -1;
		try{
			in = new BufferedReader(new FileReader(datafilename));
			//read file
			String line = null;
			while((line = in.readLine()) != null){
		     	StringTokenizer st = new StringTokenizer(line);
				// data file must have all 3 data fields
				if (st.countTokens() == 3){
				// check the id to see if equal
					if(st.nextToken().equals(arg1.value)){
						st.nextToken();
						balance.value = Integer.parseInt(st.nextToken());
						if (loggingEnabled) {
		                	writer.write(arg1.value + " " + balance.value + "\n");
						}
		                writer.close();
					}
				}
			}
		}
		catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
		}
        return balance;
    }

	public byte GET_ACC_TYPE_1(acc_id_num arg1){
        System.out.println("Processing request for "+ arg1.value );
		BufferedReader in = null;
		try{
			in = new BufferedReader(new FileReader(datafilename));
			//read file
			String line =null;
			while((line=in.readLine())!=null){
		     	StringTokenizer st = new StringTokenizer(line);
				// data file must have all 3 data fields
				if (st.countTokens() == 3){
				// check the id to see if equal
					if(st.nextToken().equals(arg1.value)){
						return  (st.nextToken().getBytes())[0];
					}
				}
			}
			// if fall through then return error
			return 0;
		}
		catch (Exception e){
            System.out.println("error Processing request for "+ arg1.value );
		}
        return 0;
    }

    public static void main(String [] args) {
    	loggingEnabled = true;

        //check for file argument
		if (args.length >1) {
			System.out.println("usage: getAccTypeInterface [datafile]");
			System.exit(1);
		}
		if (args.length ==1) {
			datafilename = args[0];
		}
		else {
			datafilename = "acc.txt";
		}

		//test existance of datafile
		File f = new File(datafilename);
		if (!f.isFile()){
			// datafile is missing
			System.out.println(datafilename + " is not a valid file name \n Server aborting");
			System.exit(1);
		}

		try {
	           System.out.println("Starting getAccTypeInterface...");
	           getAccTypeInterface server = new getAccTypeInterface();
	           server.run();
	        } catch ( Exception e ) {
	            System.out.println("Server error:");
	            e.printStackTrace(System.out);
	        }
	        System.out.println("Server stopped.");
	    }

}
