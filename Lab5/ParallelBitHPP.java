
import java.awt.* ;
import java.util.concurrent.BrokenBarrierException ;
import java.util.concurrent.CyclicBarrier;
import javax.swing.*;

public class ParallelBitHPP extends Thread {


    final static int NX = 80, NY = 60 ;  // Lattice dimensions
    final static int q = 4 ;  // population
     
    final static int NITER = 10000 ;
    final static int DELAY = 500 ;

    final static double DENSITY = 1.0 ;  // initial state, between 0 and 1.0.

    static Display display = new Display() ;

    static int [] [] fin = new int [NX] [NY] ;
    static int [] [] fout = new int [NX] [NY] ;

    final static int P = 4;

    public static CyclicBarrier newBarrier = new CyclicBarrier(P);

    public static void main(String args []) throws Exception {

        // initialize - populate a subblock of grid
        for(int i = 0; i < NX/4 ; i++) { 
            for(int j = 0; j < NY/4 ; j++) { 
                int total = 0;
                for(int d = 0 ; d < q ; d++) {
                    if(Math.random() < DENSITY) {
                        total += Math.pow(2, d);
                    }
                }
                fin[i][j] = total;
            }
        }


        display.repaint() ;
        Thread.sleep(DELAY) ;

        ParallelBitHPP[] threads = new ParallelBitHPP[P];

        long startTime = System.currentTimeMillis();

        for(int i=0; i<P; i++){
            threads[i] = new ParallelBitHPP(i);
            threads[i].start();
        }

        for(int i=0; i<P; i++){
            threads[i].join();
        }

        long endTime = System.currentTimeMillis();

        System.out.println("Calculation completed in "
                + (endTime - startTime) + " milliseconds");
    }

    int me;

    public ParallelBitHPP(int thread) {
        this.me = thread;
    }

    public void run(){
        int step = NITER/P;
        int start = me*step;
        int end = start + step;
        for(int iter = start; iter < end; iter++){
            // Collision

            for(int i = 0; i < NX ; i++) { 
                for(int j = 0; j < NY ; j++) { 

                    if(fin[i][j] == 12){
                        fin[i][j] = 3;
                    }else if(fin[i][j] == 3){
                        fin[i][j] = 12;
                    }
                    fout[i][j] = fin[i][j];
                    // please add collisions as per lecture!
                }
            }

            synch();

            // Streaming

            for(int i = 0; i < NX ; i++) { 
                int iP1 = (i + 1) % NX ;
                int iM1 = (i - 1 + NX) % NX ;
                for(int j = 0; j < NY ; j++) { 
                    int jP1 = (j + 1) % NY ;
                    int jM1 = (j - 1 + NY) % NY ;

                    // no streaming case:
                    int total = 0, n = fout[i][jM1];
                    total += (n%2)*Math.pow(2, 0);
                    n = fout[i][jP1];
                    n /= 2;
                    total += (n%2)*Math.pow(2, 1);
                    n = fout[iM1][j];
                    n /= 2;
                    n /= 2;
                    total += (n%2)*Math.pow(2, 2);
                    n = fout[iP1][j];
                    n /= 2;
                    n /= 2;
                    n /= 2;
                    total += (n%2)*Math.pow(2, 3);

                    fin[i][j] = total;
                    // please add streaming as per lecture!
                }
            }

            synch();

            if(me == 0){
                System.out.println("iter = " + iter) ;
                display.repaint() ;
            }

        }
    }

    public void synch() {
        try {
            newBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    
    static class Display extends JPanel {

        final static int CELL_SIZE = 14 ; 

        public static final int ARROW_START = 2 ;
        public static final int ARROW_END   = 7 ;
        public static final int ARROW_WIDE  = 3 ;

        Display() {

            setPreferredSize(new Dimension(CELL_SIZE * NX, CELL_SIZE * NY)) ;

            JFrame frame = new JFrame("HPP");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {

            g.setColor(Color.WHITE) ;
            g.fillRect(0, 0, CELL_SIZE * NX, CELL_SIZE * NY) ;

            g.setColor(Color.PINK) ;
            //g.setColor(Color.LIGHT_GRAY) ;
            for(int i = 0 ; i < NX ; i++) {
                for(int j = 0 ; j < NY ; j++) {
                    int originX = CELL_SIZE * i + CELL_SIZE/2 ;
                    int originY = CELL_SIZE * j + CELL_SIZE/2 ;
                    g.fillOval(originX - 2, originY - 2, 4, 4) ;
                }
            } 

            g.setColor(Color.BLUE) ;
            int [] tri_x = new int [3], tri_y = new int [3] ;
            for(int i = 0 ; i < NX ; i++) {
                for(int j = 0 ; j < NY ; j++) {
                    int val = fin[i][j];
                    int first, second, third, fourth;
                    first = val%2;
                    val /= 2;
                    second = val%2;
                    val /= 2;
                    third = val%2;
                    val /= 2;
                    fourth = val%2;
                    int originX = CELL_SIZE * i + CELL_SIZE/2 ;
                    int originY = CELL_SIZE * j + CELL_SIZE/2 ;
                    if(fourth == 1) {
                        tri_x [0] = originX - ARROW_START ;
                        tri_x [1] = originX - ARROW_START ;
                        tri_x [2] = originX - ARROW_END ;
                        tri_y [0] = originY - ARROW_WIDE ;
                        tri_y [1] = originY + ARROW_WIDE ;
                        tri_y [2] = originY ;
                        //g.setColor(Color.BLUE) ;
                        g.fillPolygon(tri_x, tri_y, 3) ;
                    }
                    if(third == 1) {
                        tri_x [0] = originX + ARROW_START ;
                        tri_x [1] = originX + ARROW_START ;
                        tri_x [2] = originX + ARROW_END ;
                        tri_y [0] = originY - ARROW_WIDE ;
                        tri_y [1] = originY + ARROW_WIDE ;
                        tri_y [2] = originY ;
                        //g.setColor(Color.RED) ;
                        g.fillPolygon(tri_x, tri_y, 3) ;
                    }
                    if(second == 1) {
                        tri_x [0] = originX - ARROW_WIDE ;
                        tri_x [1] = originX + ARROW_WIDE ;
                        tri_x [2] = originX  ;
                        tri_y [0] = originY - ARROW_START ;
                        tri_y [1] = originY - ARROW_START ;
                        tri_y [2] = originY - ARROW_END ;
                        //g.setColor(Color.GREEN) ;
                        g.fillPolygon(tri_x, tri_y, 3) ;
                    }
                    if(first == 1) {
                        tri_x [0] = originX - ARROW_WIDE ;
                        tri_x [1] = originX + ARROW_WIDE ;
                        tri_x [2] = originX  ;
                        tri_y [0] = originY + ARROW_START ;
                        tri_y [1] = originY + ARROW_START ;
                        tri_y [2] = originY + ARROW_END ;
                        //g.setColor(Color.YELLOW) ;
                        g.fillPolygon(tri_x, tri_y, 3) ;
                    }
                }
            } 
        }
    }
}

