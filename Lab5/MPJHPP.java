import mpi.* ;

import java.awt.* ;
import javax.swing.*;


public class MPJHPP {
    final static int NX = 80, NY = 60 ;  // Lattice dimensions
    final static int q = 4 ;  // population
     
    final static int NITER = 10000 ;
    final static int DELAY = 500 ;

    final static double DENSITY = 1.0 ;  // initial state, between 0 and 1.0.

    static Display display;

    static int [] [] all_fin; // array for display
    static int [] [] fin, fout;

    static int P, me, B ;

    public static void main(String args []) throws Exception {
        MPI.Init(args) ;
		  
        me = MPI.COMM_WORLD.Rank() ;
        P = MPI.COMM_WORLD.Size() ;

        B = NX / P ;

        fin  = new int [B+2] [NY] ;
        fout = new int [B+2] [NY] ;

        if(me == 0){
            all_fin = new int [NX][NY];
            display = new Display();
        }

        // Make voltage non-zero on left and right edges

        int begin = 1 ;
        int end = B+1 ;

        // if(me == 0) {
        //     begin = 2 ;
        // }
    
        // if (me == P-1) {
        //     end = B ;
        // }

        int last = me*(B);
        for(int i=begin; i<end; i++){
            for(int j=0; j<NY/4; j++){
                if(last < NX/4){
                    int total = 0;
                    for(int d = 0 ; d < q ; d++) {
                        if(Math.random() < DENSITY) {
                            total += Math.pow(2, d);
                        }
                    }
                    fin[i][j] = total;
                }else{
                    break;
                }
            }
            if(last < NX/4){
                last++;
            }else{
                break;
            }
        }

        begin = 1 ;
        end = B + 1 ;

        // if(me == 0){displayFin();}

        displayFin();

        Thread.sleep(DELAY + 100) ;

        long startTime = System.currentTimeMillis();

        for(int iter = 0 ; iter < NITER ; iter++) {

            for(int i = 0; i < B+2 ; i++) { 
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

            // Edge swap

            int next = (me + 1) % P ;
            int prev = (me - 1 + P) % P ;
            MPI.COMM_WORLD.Sendrecv(fin [B], 0, NY, MPI.INT, next, 0,
                                    fin [0], 0, NY, MPI.INT, prev, 0) ;
            MPI.COMM_WORLD.Sendrecv(fin [1], 0, NY, MPI.INT, prev, 0,
                                    fin [B+1], 0, NY, MPI.INT, next, 0) ;

            for(int i = begin; i < end ; i++) { 
                // int iP1 = (i + 1) % (B+2) ;
                // int iM1 = (i - 1 + (B+2)) % (B+2) ;
                int iP1 = i+1;
                int iM1 = i - 1 ;
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

            displayFin();
            Thread.sleep(DELAY) ;
        }
    }

    public static void displayFin() {
          
        if(me > 0) {
            MPI.COMM_WORLD.Send(fin, 1, B, MPI.OBJECT, 0, 0) ;
        }
        else {  // me == 0
            for(int i = 1 ; i <= B ; i++) {
                for(int j = 0 ; j < NY ; j++) {
                    all_fin [i-1] [j] = fin [i] [j] ;
                }
            }
          for(int src = 1 ; src < P ; src++) {
            MPI.COMM_WORLD.Recv(all_fin, src * B, B, MPI.OBJECT, src, 0) ;
          }
                
          display.repaint() ;
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
                    int val = all_fin[i][j];
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
