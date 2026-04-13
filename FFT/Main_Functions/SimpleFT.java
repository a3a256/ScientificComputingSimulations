package Main_Functions;

import HelperFunctions.Display2dFT;
import HelperFunctions.DisplayDensity;
import HelperFunctions.ReadPGM;

public class SimpleFT extends Thread{

    /**
     * @param args the command line arguments
     */
    public static int N = 256;

    final static int P = 2;

    final static double[][] CRe = new double[N][N], CIm = new double[N][N];
    final static double[][] X = new double[N][N];

    final static double [] [] reconstructed = new double [N] [N] ;

    public static void main(String[] args) throws Exception {

        // double[][] X = new double[N][N];
        ReadPGM.read(X, "wolf.pgm", N);

        DisplayDensity display
                = new DisplayDensity(X, N, "Original Image");

        // double[][] CRe = new double[N][N], CIm = new double[N][N];

        int i;

        SimpleFT[] threads = new SimpleFT[P];

        long startTime = System.currentTimeMillis();

        for(i=0; i<P; i++){
            threads[i] = new SimpleFT(i, 1);
            threads[i].start();
        }

        for(i=0; i<P; i++){
            threads[i].join();
        }

        Display2dFT display2
                = new Display2dFT(CRe, CIm, N, "Discrete FT");

        long endTime = System.currentTimeMillis();

        long dft = endTime - startTime;

        SimpleFT[] reverse_threads = new SimpleFT[P];

        startTime = System.currentTimeMillis();

        for(i=0; i<P; i++){
            reverse_threads[i] = new SimpleFT(i, 0);
            reverse_threads[i].start();
        }

        for(i=0; i<P; i++){
            reverse_threads[i].join();
        }

        endTime = System.currentTimeMillis();

        System.out.println("Parallel DFT calculation completed in "
                + (dft) + " milliseconds");

        System.out.println("Parallel Reverse DFT calculation completed in "
                + (endTime - startTime) + " milliseconds");

        DisplayDensity display3 =
                  new DisplayDensity(reconstructed, N, "Reconstructed Image") ;
    }

    int k, forward;

    public SimpleFT(int k, int forward){
        this.k = k;
        this.forward = forward;
    }

    public void run(){

        int step = N/P;
        int k_start = k*step;
        int k_end = k_start + step;

        int i, j, r, t;


        if(forward == 1){

            for (i = k_start; i < k_end; i++) {
                for (j = 0; j < N; j++) {
                    double sumRe = 0, sumIm = 0;
                    // Nested for loops performing sum over X elements
                    for (r = 0; r < N; r++) {
                        for (t = 0; t < N; t++) {
                            double arg = ((2 * Math.PI) * (i * r + t * j)) / ((double)N);
                            double cos = Math.cos(arg);
                            double sin = Math.sin(arg);
                            sumRe += cos * X[r][t];
                            sumIm += sin * X[r][t];
                        }
                    }
                    CRe[i][j] = sumRe;
                    CIm[i][j] = sumIm;
                }
                System.out.println("Completed FT line " + i + " out of " + N);
            }
        }else{

            for (i = k_start; i < k_end; i++) {
                for (j = 0; j < N; j++) {
                    double sumRe = 0, sumIm = 0;
    
                    for (r = 0; r < N; r++) {
                        for (t = 0; t < N; t++) {
                            double arg = ((2 * Math.PI) * (r * i + j * t)) / ((double)N);
                            double cos = Math.cos(arg);
                            double sin = Math.sin(arg);
                            sumRe += (CRe[r][t] * cos) + (CIm[r][t] * sin);
                            sumIm += (CRe[r][t] * sin) - (CIm[r][t] * cos);
                        }
                    }
    
                    reconstructed[i][j] = (sumRe + sumIm) / (N * N);
    
                }
                System.out.println("Completed reversed FT line " + i + " out of " + N);
            }

        }
    }
}
