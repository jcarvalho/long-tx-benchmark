package pt.ist.longtx.bench;

import pt.ist.fenixframework.FenixFramework;

public class Bank extends Bank_Base {

    public Bank(String name) {
        super();
        this.setName(name);
        this.setDomainRoot(FenixFramework.getDomainRoot());
    }

    public double getTotalMoney() {
        double money = 0;
        for (Customer customer : getCustomerSet()) {
            money += customer.getTotalMoney();
        }
        return money;
    }

}
