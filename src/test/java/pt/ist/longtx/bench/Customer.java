package pt.ist.longtx.bench;

import java.util.Iterator;
import java.util.Random;

public class Customer extends Customer_Base {

    private static final Random random = new Random();

    public Customer(String name) {
        super();
        this.setName(name);
    }

    public double getTotalMoney() {
        double money = 0;
        for (Account account : getAccountSet()) {
            money += account.getBalance();
        }
        return money;
    }

    public void shuffle() {
        Account firstAccount = getRandomAccount();
        for (Account account : getAccountSet()) {
            if (!firstAccount.equals(account)) {
                account.transfer(account.getBalance(), firstAccount);
            }
        }
    }

    private Account getRandomAccount() {
        int pos = random.nextInt(getAccountSet().size());
        Iterator<Account> accs = getAccountSet().iterator();
        while (pos > 0) {
            accs.next();
            pos--;
        }
        return accs.next();
    }
}
