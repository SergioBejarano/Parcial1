package edu.eci.arsw.math;

public class BBPThread extends Thread {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    private byte[] digits;
    private int start;
    private int count;
    private Boolean running;
    public Boolean alive;

    public BBPThread(int start, int count) {
        this.start = start;
        this.count = count;
        this.digits = new byte[count];
        this.running = true;
        this.alive = true;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        double sum = 0;
        for (int i = 0; i < count; i++) {
            if (System.currentTimeMillis() - startTime >= 5000) {
                running = false;
                synchronized (this) {
                    while (!running) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            if (i % DigitsPerSum == 0) {
                sum = 4 * sum(1, start)
                        - 2 * sum(4, start)
                        - sum(5, start)
                        - sum(6, start);

                start += DigitsPerSum;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[i] = (byte) sum;

        }
        alive = false;
    }

    private static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    private static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

    public synchronized void resumeThread() {
        running = true;
        notify();
    }

    public byte[] getDigits() {
        return digits;
    }

    public int getProcessedDigits() {
        // Contar cuántos dígitos han sido procesados
        int processed = 0;
        for (int i = 0; i < digits.length; i++) {
            if (digits[i] != 0 || i == 0) {
                processed = i + 1;
            }
        }
        return processed;
    }

}