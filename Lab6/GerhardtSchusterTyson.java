
import java.awt.*;
import javax.swing.*;

public class GerhardtSchusterTyson {

    final static int N = 200;
    final static int CELL_SIZE = 5;
    final static int DELAY = 100;

    final static int V_MAX = 100;	// Maximum possible value for v
    final static int V_RECO = 70;	// In order to a cell jumps to the recovery state, V_RECO < v
    final static int V_EXCI = 65;	// To get a cell excited, v <= V_EXCI
    final static int G_UP = 20;
    final static int G_DOWN = 5;
    final static int K0_EXCI = 0;	// Minimum number of excited neighbours to make a cell excited
					// Smaller values of K0_EXCI represent higher excitabilities
    final static int K0_RECO = 5;	// Minimum number of unexcited neighbours to make a cell jumps to recovery state
    final static int R = 3;		// Neighbourhood radius

    static int[][] u = new int[N][N];
    static int[][] v = new int[N][N];

    static int[][] exNeigh = new int[N][N];

    static Display display = new Display();

    public static void main(String args[]) throws Exception {

        // Define initial state - excited bottom row / resting elsewhere.
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                u[i][j] = (j > N - 5) ? 1 : 0;
                v[i][j] = 0;
            }
        }

        display.repaint();
        pause();

        // Main update loop.
        int iter = 0;
        boolean chopped = false;
        while (true) {

            System.out.println("iter = " + iter++);

            if (!chopped && u[0][0] == 1) {
                chopped = true;
                for (int i = 0; i < 1; i++) {
                    for (int j = 0; j < N; j++) {
                        u[i][j] = 0;
                        v[i][j] = 0;
                    }
                }

                // for (int i = 0; i < N; i++) {
                //     for (int j = 0; j < N; j++) {
                //         System.out.print(v[i][j]);
                //     }
                //     System.out.print("\n");
                // }
            }

            // Calculate neighbour sums.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    exNeigh[i][j] = 0;
                    for (int p = i - R; p <= i + R; p++) {
                        for (int q = j - R; q <= j + R; q++) {
                            int pp = Math.max(0, Math.min(p, N - 1));
                            int qq = Math.max(0, Math.min(q, N - 1));
                            exNeigh[i][j] += u[pp][qq];
                        }
                    }
                }
            }

            // Calculate next state.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {

                    if (u[i][j] == 1) {
                        if (v[i][j] > V_RECO && exNeigh[i][j] > kReco(i, j)) {
                            u[i][j] = 0;
                        }
                        v[i][j] = Math.min(v[i][j] + G_UP, V_MAX);
                    } else if (v[i][j] < V_EXCI && exNeigh[i][j] > kExci(i, j)) {
                        u[i][j] = 1;
                    } else {
                        v[i][j] = Math.max(v[i][j] - G_DOWN, 0);
                    }
                }
            }
            System.out.println("u = " + u[0][0] + " v = " + v[0][0] + " exc = " + exNeigh[0][0]);
            
            display.repaint();
            pause();
            
        }
    }

    private static double kExci(int i, int j) {
        return K0_EXCI + (R * (2 * R + 1) - K0_EXCI) * ((double) v[i][j] / V_EXCI);
    }

    private static double kReco(int i, int j) {
      //  return ((R + 1) * (R + 1) - 1) - (K0_RECO + (R * (2 * R + 1) - K0_RECO) * 
      //          ((double) (v[i][j] - V_MAX)) / (V_RECO - V_MAX));
      return K0_RECO + (R * (2 * R + 1) - K0_RECO) * 
                ((double) (v[i][j] - V_MAX)) / (V_RECO - V_MAX);
    
    }
    
    static class Display extends JPanel {

        final static int WINDOW_SIZE = N * CELL_SIZE;

        Display() {

            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));

            JFrame frame = new JFrame("Gerhardt, Schuster & Tyson Model");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
            g.setColor(Color.BLACK);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (u[i][j] == 1) {
                        g.fillRect(CELL_SIZE * i, CELL_SIZE * j,
                                CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }
    }

    static void pause() {
        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
