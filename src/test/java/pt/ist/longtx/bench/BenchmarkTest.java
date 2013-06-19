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

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    private static final int ITERS_PER_TX = Integer.parseInt(System.getProperty("ITERS_PER_TX", "1")) * 1000;

    private static Customer jack;
    private static Customer jill;

    private static double regularTx;
    private static double longTx;

    @Atomic
    @BeforeClass
    public static void setup() {
        Bank a = new Bank("Bank A");

        jack = new Customer("Jack", a);
        jill = new Customer("Jill", a);

        jack.addAccount(new Account(1000d));
        jill.addAccount(new Account(1000d));

        logger.info("Running with {}k iterations", ITERS_PER_TX / 1000);
    }

    @Test
    public void measureRunimeWithinRegularTransaction() {
        long start = System.nanoTime();
        double timeInside = doTransfer();
        double total = regularTx = System.nanoTime() - start;
        logger.info("Test with regular transaction took {} ms, inside: {} ms. Percentage of time inside: {}", total / 1e6,
                timeInside / 1e6, (timeInside / total) * 100);
    }

    @Test
    public void measureRuntimeWithinTransactionalContext() {

        TransactionalContext context = createContext();

        LongTransaction.setContextForThread(context);

        long start = System.nanoTime();

        double timeInside = doTransfer();

        double total = longTx = System.nanoTime() - start;

        commitContext(context);

        LongTransaction.removeContextFromThread();

        logger.info("Test within transactional context took {} ms, inside: {} ms. Percentage of time inside: {}", total / 1e6,
                timeInside / 1e6, (timeInside / total) * 100);
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
    private double doTransfer() {
        long start = System.nanoTime();
        for (int i = 0; i < ITERS_PER_TX; i++) {
            if (i % 2 == 0) {
                jack.getAccountSet().iterator().next().transfer(200d, jill.getAccountSet().iterator().next());
            } else {
                jill.getAccountSet().iterator().next().transfer(200d, jack.getAccountSet().iterator().next());
            }
        }
        return System.nanoTime() - start;
    }

    @AfterClass
    public static void printDifference() {
        logger.info("Time proportion: {}", longTx / regularTx);
    }

}
