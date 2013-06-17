package pt.ist.longtx.bench;

import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;

public class Account extends Account_Base {

    public Account(Double initialBalance) {
        super();
        this.setBalance(initialBalance);
        this.setOpened(new DateTime());
    }

    @Atomic(mode = TxMode.WRITE)
    public void withdraw(Double amount) {
        if (amount > this.getBalance()) {
            throw new IllegalStateException("Not enough money!");
        }
        this.setBalance(this.getBalance() - amount);
    }

    @Atomic(mode = TxMode.WRITE)
    public void deposit(Double amount) {
        this.setBalance(this.getBalance() + amount);
    }

    @Atomic(mode = TxMode.WRITE)
    public void transfer(Double amount, Account to) {
        this.withdraw(amount);
        to.deposit(amount);
    }

}
