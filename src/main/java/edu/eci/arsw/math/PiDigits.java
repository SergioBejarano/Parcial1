package edu.eci.arsw.math;

import java.util.Scanner;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    /**
     * Returns a range of hexadecimal digits of pi.
     * 
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @param N     The number of threads to use for parallel computation
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int N) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        byte[] digits = new byte[count];

        BBPThread[] threads = new BBPThread[N];

        int segmentSize = count / N;
        int remainder = count % N;
        int currentStart = start;

        for (int i = 0; i < N; i++) {
            int segmentCount = segmentSize + (i < remainder ? 1 : 0);
            threads[i] = new BBPThread(currentStart, segmentCount);
            threads[i].start();
            currentStart += segmentCount;
        }

        try (Scanner scanner = new Scanner(System.in)) {
            // Control para pausar/reanudar hilos cada 5 segundos
            while (true) {
                // Verificar si todos los hilos han terminado
                boolean allThreadsFinished = true;
                for (BBPThread thread : threads) {
                    if (thread.alive) {
                        allThreadsFinished = false;
                        break;
                    }
                }

                if (allThreadsFinished) {
                    break;
                }

                try {
                    Thread.sleep(5000);

                    System.out.println("=== Progreso de cálculo de dígitos de PI ===");
                    int totalProcessed = 0;
                    for (int i = 0; i < threads.length; i++) {
                        int processed = threads[i].getProcessedDigits();
                        totalProcessed += processed;
                        System.out.println("Hilo " + i + ": " + processed + " digitos procesados");
                    }
                    System.out.println("Total procesado: " + totalProcessed + " de " + count + " digitos");
                    System.out.println("Presione ENTER para continuar...");

                    scanner.nextLine();

                    // Reanudar todos los hilos
                    for (BBPThread thread : threads) {
                        if (thread.alive) {
                            thread.resumeThread();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        for (BBPThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int currentIndex = 0;
        for (BBPThread thread : threads) {
            byte[] threadDigits = thread.getDigits();
            System.arraycopy(threadDigits, 0, digits, currentIndex, threadDigits.length);
            currentIndex += threadDigits.length;
        }

        return digits;
    }

}