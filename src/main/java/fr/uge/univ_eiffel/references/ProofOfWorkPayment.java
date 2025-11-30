package fr.uge.univ_eiffel.references;

import fr.uge.univ_eiffel.FactoryClient;

public class ProofOfWorkPayment implements PaymentMethod {

    private final FactoryClient client;

    public ProofOfWorkPayment(FactoryClient client) {
        this.client = client;
    }

    @Override
    public BigDecimal getBalance() throws Exception {
        return client.getBalance();
    }

    @Override
    public void topUpAccount() throws Exception {
        BillingChallenge challenge = client.getBillingChallenge();
        byte[] solution = solvePow(challenge.dataPrefix(), challenge.hashPrefix());
        client.submitBillingAnswer(challenge, solution);
    }

    private byte[] solvePow(byte[] prefix, byte[] hashPrefix) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] attempt = Arrays.copyOf(prefix, prefix.length + 1);

        for (int i = 0; i < 256; i++) {
            attempt[attempt.length - 1] = (byte) i;
            byte[] hash = md.digest(attempt);
            if (startsWith(hash, hashPrefix)) return attempt;
        }
        throw new RuntimeException("No solution found in 1-byte search space.");
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++)
            if (data[i] != prefix[i]) return false;
        return true;
    }
}

