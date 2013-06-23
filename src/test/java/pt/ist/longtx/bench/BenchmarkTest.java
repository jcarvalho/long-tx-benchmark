package pt.ist.longtx.bench;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;
import pt.ist.fenixframework.Atomic.TxMode;
import pt.ist.fenixframework.backend.jvstm.pstm.VBox;
import pt.ist.fenixframework.longtx.LongTransaction;
import pt.ist.fenixframework.longtx.TransactionalContext;

@RunWith(JUnit4.class)
public class BenchmarkTest {

    private static Bank bank;

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    private static final int ACCOUNTS = 10;

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

    @Atomic(mode = TxMode.WRITE)
    private void createCustomers() {
        for (int i = 0; i < ACCOUNTS; i++) {
            Customer customer = new Customer("Customer " + customerCounter++);
            customer.addAccount(new Account(200d));
            customer.addAccount(new Account(100d));
            bank.addCustomer(customer);
        }
    }

    @Atomic(mode = TxMode.READ)
    private void printTotal() {
        logger.info("Bank has {} money.", bank.getTotalMoney());
    }

    @Atomic(mode = TxMode.WRITE)
    private void shuffleMoney() {
        for (Customer customer : bank.getCustomerSet()) {
            customer.shuffle();
        }
    }

    @Test
    public void doRegular() {
        doScript("regular");
    }

    @Test
    public void doLongTx() {
        TransactionalContext context = createContext();
        LongTransaction.setContextForThread(context);
        doScript("long");
        LongTransaction.removeContextFromThread();
        commitContext(context);
    }

    public void doScript(String name) {
        clearIt();
        createCustomers();
        printTotal();
        shuffleMoney();
        shuffleMoney();
        printTotal();
        createCustomers();
        shuffleMoney();
        printTotal();
        dumpCSV(name + ".csv");
    }

    @Atomic(mode = TxMode.WRITE)
    private void clearIt() {
        bank.getCustomerSet().clear();
    }

    public static void dumpCSV(String path) {
        StringBuilder builder = new StringBuilder();
        for (Long value : VBox.getBoxValueTimes) {
            builder.append(value);
            builder.append('\n');
        }
        VBox.getBoxValueTimes.clear();
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
