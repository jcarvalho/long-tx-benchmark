package pt.ist.longtx.bench;

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

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    private static final int NUM_ITERS = 1000 * 1000;

    private static final int ITERS_PER_TX = Integer.parseInt(System.getProperty("ITERS_PER_TX", "1"));

    private static Customer jack;
    private static Customer jill;

    @Atomic
    @BeforeClass
    public static void setup() {
        Bank a = new Bank("Bank A");

        jack = new Customer("Jack", a);
        jill = new Customer("Jill", a);

        jack.addAccount(new Account(Double.MAX_VALUE));
        jill.addAccount(new Account(Double.MAX_VALUE));

        //Account shared = new Account(200d);
        //jack.addAccount(shared);
        //jill.addAccount(shared);
    }

    @Test
    public void timeWithRegularTransaction() {
        long start = System.nanoTime();

        for (int i = 0; i < NUM_ITERS; i++) {
            if (i % 2 == 0) {
                doTransfer(jill, jack);
            } else {
                doTransfer(jack, jill);
            }
        }

        double total = System.nanoTime() - start;
        logger.info("Test with regular transaction took {} ms", total / (NUM_ITERS * 1e6));
    }

    @Test
    public void testWithinTransactionalContext() {

        TransactionalContext context = createContext();

        LongTransaction.setContextForThread(context);

        long start = System.nanoTime();

        for (int i = 0; i < NUM_ITERS; i++) {
            if (i % 2 == 0) {
                doTransfer(jill, jack);
            } else {
                doTransfer(jack, jill);
            }
        }

        double total = System.nanoTime() - start;

        commitContext(context);

        LongTransaction.removeContextFromThread();

        logger.info("Test within transactional context took {} ms", total / (NUM_ITERS * 1e6));
    }

    @Atomic(mode = TxMode.WRITE)
    private void commitContext(TransactionalContext context) {
        context.commit(false);
    }

    @Atomic(mode = TxMode.WRITE)
    private TransactionalContext createContext() {
        return new TransactionalContext("context");
    }

    @Atomic(mode = TxMode.WRITE)
    private void doTransfer(Customer from, Customer to) {
        for (int i = 0; i < ITERS_PER_TX; i++) {
            from.getAccountSet().iterator().next().transfer(200d, to.getAccountSet().iterator().next());
        }
    }

}
