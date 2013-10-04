package pt.ist.longtx.bench;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.longtx.LongTransaction;
import pt.ist.fenixframework.longtx.TransactionalContext;

@RunWith(JUnit4.class)
public class BenchmarkTest {

    private static Bank bank;

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    private static final int ACCOUNTS = Integer.parseInt(System.getProperty("ACCOUNTS", "10"));

    private static final int NUM_TIMES = Integer.parseInt(System.getProperty("ITERATIONS", "200"));

    @Atomic
    @BeforeClass
    public static void setup() {
        bank = new Bank("Bank A");
    }

    @Atomic(mode = TxMode.WRITE)
    private void commitContext(TransactionalContext context) {
        context.commit(false);
    }

    @Atomic(mode = TxMode.WRITE)
    private TransactionalContext createContext() {
        return new TransactionalContext("context");
    }

    private static int customerCounter = 0;

    private void createCustomers() {
        for (int i = 0; i < ACCOUNTS; i++) {
            Customer customer = new Customer("Customer " + customerCounter++);
            customer.addAccount(new Account(200d));
            customer.addAccount(new Account(100d));
            bank.addCustomer(customer);
        }
    }

    private void printTotal() {
        logger.info("Bank has {} money.", bank.getTotalMoney());
    }

    private void shuffleMoney() {
        for (Customer customer : bank.getCustomerSet()) {
            customer.shuffle();
        }
    }

    @Test
    public void doIt() {
        if (System.getProperty("REG") != null) {
            doRegular();
        } else {
            doLongTx();
        }
    }

    public void doRegular() {
        clearIt();
        doScript("regular");
    }

    public void doLongTx() {
        clearIt();
        TransactionalContext context = createContext();
        LongTransaction.setContextForThread(context);
        doScript("long");
        LongTransaction.removeContextFromThread();
        commitContext(context);
    }

    @Atomic(mode = TxMode.WRITE)
    public void doOperation() {
        createCustomers();
        printTotal();
        shuffleMoney();
        shuffleMoney();
        printTotal();
        createCustomers();
        shuffleMoney();
        printTotal();
    }

    public void doScript(String name) {
        logger.info("Starting {} benchmark with {} iterations", name, NUM_TIMES);
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_TIMES; i++) {
            doOperation();
        }
        logger.info("Script took {} ms", System.currentTimeMillis() - start);
    }

    @Atomic(mode = TxMode.WRITE)
    private void clearIt() {
        bank.getCustomerSet().clear();
    }

}
