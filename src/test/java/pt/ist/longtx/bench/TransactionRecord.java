package pt.ist.longtx.bench;

public class TransactionRecord extends TransactionRecord_Base {

    public TransactionRecord(Account to, Double amount) {
        super();
        setTo(to);
        setAmount(amount);
    }

}
