package pt.ist.longtx.bench;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ist.fenixframework.Atomic;

@RunWith(JUnit4.class)
public class BenchmarkTest {

    private static final Logger logger = LoggerFactory.getLogger(BenchmarkTest.class);

    private static final int NUM_ITERS = 1000 * 10;

    private static Customer jack;
    private static Customer jill;

    @Atomic
    @BeforeClass
    public static void setup() {
        Bank a = new Bank("Bank A");

        jack = new Customer("Jack", a);
        jill = new Customer("Jill", a);

        jack.addAccount(new Account(1000d));
        jill.addAccount(new Account(700d));

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

    @Atomic
    private void doTransfer(Customer from, Customer to) {
        from.getAccountSet().iterator().next().transfer(200d, to.getAccountSet().iterator().next());
    }

}