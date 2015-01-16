const ID_NUM_SIZE = 5;
const MAX_HISTORY_SIZE = 50;
const NO_ACC = 0;

typedef string acc_id_num<ID_NUM_SIZE>;
typedef int acc_balance;
typedef int history<MAX_HISTORY_SIZE>;

struct transaction_pair {
	int src_old; int dst_old;
	int src_new; int dst_new;
};

program BANK_ACCOUNT_PROG {
	version ACC_VERS_1 {
        char GET_ACC_TYPE(acc_id_num) = 1; //1 is the number assigned to this function
        acc_balance GET_BALANCE(acc_id_num) = 2;
        acc_balance SET_BALANCE(acc_id_num, acc_balance) = 3;
        transaction_pair TRANSACTION(acc_id_num, acc_id_num, acc_balance) = 4;
        history GET_TRANSACTION_HISTORY(acc_id_num) = 5;
    }=1; //1 is the number assigned to this version
}=9999; //9999 is the number assigned to this program
