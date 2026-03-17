
import java.awt.*;
import javax.swing.*;


// 0 - Resting
// 2 - Excited 
// 1 - Recovering

public class FourStateCA {

    final static int N = 50;
    final static int CELL_SIZE = 5;
    final static int DELAY = 100;

    static int[][] state = new int[N][N];

    static boolean[][] excitedNeighbour = new boolean[N][N];

    static Display display = new Display();

    public static void main(String args[]) throws Exception {

        // Define initial state - excited bottom row / resting elsewhere.
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                // starting at the center
                // if(j == N/2 + 1 & i >= N/2){
                //     state[i][j] = 3;
                // }else if(j == N/2 + 2 & i >= N/2){
                //     state[i][j] = 1;
                // }else{
                //     state[i][j] = 0;
                // }


                // starting at the corner later populate the corner one
                // state[i][j] = 0;

                state[i][j] = j == N - 1 ? 3 : 0;

            }
        }

        // state[0][0] = 2;

        // for (int i = 0; i < N; i++) {
        //     for (int j = 0; j < N; j++) {
        //         System.out.print(state[i][j] + " ");
        //     }
        //     System.out.print("\n");
        // }

        display.repaint();
        pause();

        // Main update loop.
        int iter = 0;
        while (true) {

            System.out.println("iter = " + iter++);

            // Chop wave when half-way up.
            if (iter == N / 2) {
                for (int i = 0; i < N / 2; i++) {
                    for (int j = 0; j < N; j++) {
                        state[i][j] = 0;
                    }
                }

            }

            // Calculate which cells have excited neighbnours.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {

                    // find neighbours...
                    int ip = Math.min(i + 1, N - 1);
                    int im = Math.max(i - 1, 0);

                    int jp = Math.min(j + 1, N - 1);
                    int jm = Math.max(j - 1, 0);

                    excitedNeighbour[i][j]
                            = state[i][jp] == 3
                            || state[i][jm] == 3
                            || state[ip][j] == 3
                            || state[im][j] == 3;
                }
            }

            // Update state.
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    switch (state[i][j]) {
                        case 0:
                            if (excitedNeighbour[i][j]) {
                                state[i][j] = 3;
                            }
                            break;
                        case 3:
                            state[i][j] = 2;
                            break;
                        case 2:
                            state[i][j] = 1;
                            break;
                        default: // 1
                            state[i][j] = 0;
                            break;
                    }
                }
            }

            display.repaint();
            pause();

            // break;
        }
    }

    static class Display extends JPanel {

        final static int WINDOW_SIZE = N * CELL_SIZE;

        Display() {

            setPreferredSize(new Dimension(WINDOW_SIZE, WINDOW_SIZE));

            JFrame frame = new JFrame("Minimal excitable media model");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(this);
            frame.pack();
            frame.setVisible(true);
        }

        public void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, WINDOW_SIZE, WINDOW_SIZE);
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (state[i][j] > 0) {
                        if (state[i][j] == 3) {
                            g.setColor(Color.BLACK);
                        }else if(state[i][j] == 2){
                            g.setColor(Color.DARK_GRAY);
                        }else {
                            g.setColor(Color.GRAY);
                        }
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
