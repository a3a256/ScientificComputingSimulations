package Main_Functions;

import HelperFunctions.Display2dFT;
import HelperFunctions.DisplayDensity;
import HelperFunctions.ReadPGM;

import mpi.* ;

public class MPJFT {

    /**
     * @param args the command line arguments
    */

    public static int N = 256;

    final static double[][] CRe = new double[N][N], CIm = new double[N][N];
    final static double[][] X = new double[N][N];

    final static double [] [] reconstructed = new double [N] [N] ;

    static int P, me, B ;


    public static void main(String[] args) throws Exception{

        MPI.Init(args) ;
		  
        me = MPI.COMM_WORLD.Rank() ;
        P = MPI.COMM_WORLD.Size() ;

        B = N / P ;

        if(me == 0){

            ReadPGM.read(X, "wolf.pgm", N);

            // double[][] X = new double[N][N];

            DisplayDensity display
                    = new DisplayDensity(X, N, "Original Image");

            // double[][] CRe = new double[N][N], CIm = new double[N][N];

        }

        MPI.COMM_WORLD.Bcast(X, 0, N, MPI.OBJECT, 0);


        int k_start = me*B;
        int k_end = k_start + B;

        int i, j, r, t;

        long startTime = System.currentTimeMillis();

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

        long endTime = System.currentTimeMillis();

        long dft = endTime - startTime;

        if(me > 0){

            MPI.COMM_WORLD.Send(CRe, k_start, B, MPI.OBJECT, 0, 0) ;
            MPI.COMM_WORLD.Send(CIm, k_start, B, MPI.OBJECT, 0, 0) ;

        }else{

            for(int src = 1 ; src < P ; src++) {
                MPI.COMM_WORLD.Recv(CRe, src * B, B, MPI.OBJECT, src, 0) ;
                MPI.COMM_WORLD.Recv(CIm, src * B, B, MPI.OBJECT, src, 0) ;
            }

            Display2dFT display2
                = new Display2dFT(CRe, CIm, N, "Discrete FT");
        }

        MPI.COMM_WORLD.Bcast(CRe, 0, N, MPI.OBJECT, 0);
        MPI.COMM_WORLD.Bcast(CIm, 0, N, MPI.OBJECT, 0);

        startTime = System.currentTimeMillis();

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

        endTime = System.currentTimeMillis();

        if(me > 0){

            MPI.COMM_WORLD.Send(reconstructed, k_start, B, MPI.OBJECT, 0, 0) ;

        }else{

            for(int src = 1 ; src < P ; src++) {
                MPI.COMM_WORLD.Recv(reconstructed, src * B, B, MPI.OBJECT, src, 0) ;
            }


            System.out.println("Multicore DFT calculation completed in "
                + (dft) + " milliseconds");

            System.out.println("Parallel Reverse DFT calculation completed in "
                + (endTime - startTime) + " milliseconds");


            DisplayDensity display3 =
                  new DisplayDensity(reconstructed, N, "Reconstructed Image") ;
        }

    }
    
}
