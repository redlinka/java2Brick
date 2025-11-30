package fr.uge.univ_eiffel.references;

import java.io.IOException;

public class AccountRefillerMain {
    public static void main(String[] args) throws IOException {
        // we fetch the email and key in LEGOFACTORY_EMAIL and LEGOFACTORY_KEY environment vars
        var email = System.getenv("LEGOFACTORY_EMAIL");
        var key = System.getenv("LEGOFACTORY_KEY");
        if (email == null || key == null) {
            System.err.println("LEGOFACTORY_EMAIL and LEGOFACTORY_KEY environment vars must be provided");
            System.exit(1);
        }
        var refiller = new AccountRefiller(email, key);
        var newBalance = refiller.refill();
        System.out.println(newBalance);
    }
}