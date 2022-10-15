import za.ac.wits.snake.DevelopmentAgent;

import java.util.*;
import java.io.*;

public class Snake2 extends DevelopmentAgent {

    public int n_snakes;
    public final int n_zombies = 6;
    public int b_width;
    public int b_height;
    public int ms_idx;

    public Tuple[] z_heads = new Tuple[n_zombies];
    public ArrayList<Tuple> s_heads = new ArrayList<>();
    public ArrayList<Tuple> barriers = new ArrayList<>();
    public char[][] board;

    public static void main(String[] args) {
        Snake2 agent = new Snake2();
        Snake2.start(agent, args);
    }

    @Override
    public void run() {
        Scanner in = new Scanner(System.in);
        String game_init = in.nextLine();

        String[] game_split = game_init.split(" ");
        n_snakes = Integer.parseInt(game_split[0]);
        b_width = Integer.parseInt(game_split[1]);
        b_height = Integer.parseInt(game_split[2]);

        board = new char[b_width][b_height];

        while(true) {

            //double start_time = System.nanoTime();

            //Parse Input ==========================================================================================
            String[] input = new String[8 + n_snakes];

            board = new char[b_width][b_height];
            s_heads = new ArrayList<>();


            boolean exit_game = false;
            for (int i = 0; i < 8 + n_snakes; i++) {
                input[i] = in.nextLine();
                if(input[i].equalsIgnoreCase("Game Over")) { exit_game = true; break; }
            }

            if(exit_game) {
                break;
            }

            Tuple apple = new Tuple();
            String[] apple_split = input[0].split(" ");
            apple.x = Integer.parseInt(apple_split[0]);
            apple.y = Integer.parseInt(apple_split[1]);
            board[apple.x][apple.y] = 'A';

            for(int z = 0; z < n_zombies; z++) {
                String[] z_split = input[z + 1].split(" ");
                mark_barriers_zombie(z_split);
                z_heads[z] = new Tuple(z_split[0]);
                board[z_heads[z].x][z_heads[z].y] = 'Z';
                barriers.remove(z_heads[z]);
            }

            ms_idx = Integer.parseInt(input[1 + n_zombies]);

            for(int s = 0; s < n_snakes; s++) {
                int input_idx = 2 + n_zombies + s;
                final int input_head_idx = 3;

                String[] s_split = input[input_idx].split(" ");
                if(s_split[0].equalsIgnoreCase("dead")) { continue; }
                mark_barriers_snake(s_split);
                s_heads.add(new Tuple(s_split[input_head_idx]));
                barriers.remove(s_heads.get(s_heads.size() - 1));
                board[s_heads.get(s_heads.size() - 1).x][s_heads.get(s_heads.size() - 1).y] = 'S';
            }

            board[s_heads.get(ms_idx).x][s_heads.get(ms_idx).y] = 'M';
            //======================================================================================================

            //A* ===================================================================================================
            Tuple[][] path_tree = a_star(s_heads.get(ms_idx), apple);
            Tuple next = backtrace(path_tree, s_heads.get(ms_idx), apple);
            //======================================================================================================

            //double end_time = System.nanoTime();
            //double duration = (end_time - start_time) / 100000;

            //Move Direction Calculation ===========================================================================
            if(next == null) { System.out.println(5); }
            else if(next.x > s_heads.get(ms_idx).x) { System.out.println(3); }
            else if(next.x < s_heads.get(ms_idx).x) { System.out.println(2); }
            else if(next.y > s_heads.get(ms_idx).y) { System.out.println(1); }
            else if(next.y < s_heads.get(ms_idx).y) { System.out.println(0); }
            //======================================================================================================

        }
    }

    public void mark_barriers_snake(String[] s) {
        for(int i = 0; i < s.length - 4; i++) {
            Tuple p1 = new Tuple(s[3 + i]);
            Tuple p2 = new Tuple(s[3 + i + 1]);

            if (p1.x > p2.x) {
                for (int x = p2.x; x <= p1.x; x++) {
                    board[x][p1.y] = 'B';
                    barriers.add(new Tuple(x, p1.y));
                }
            } else if(p1.x < p2.x) {
                for (int x = p1.x; x <= p2.x; x++) {
                    board[x][p1.y] = 'B';
                    barriers.add(new Tuple(x, p1.y));
                }
            } else if(p1.y > p2.y) {
                for (int y = p2.y; y <= p1.y; y++) {
                    board[p1.x][y] = 'B';
                    barriers.add(new Tuple(p1.x, y));
                }
            } else if(p1.y < p2.y) {
                for (int y = p1.y; y <= p2.y; y++) {
                    board[p1.x][y] = 'B';
                    barriers.add(new Tuple(p1.x, y));
                }
            }
        }
    }

    public void mark_barriers_zombie(String[] z) {
        for(int i = 0; i < z.length - 1; i++) {
            Tuple p1 = new Tuple(z[i]);
            Tuple p2 = new Tuple(z[i + 1]);

            if (p1.x > p2.x) {
                for (int x = p2.x; x <= p1.x; x++) {
                    board[x][p1.y] = 'B';
                    barriers.add(new Tuple(x, p1.y));
                }
            } else if(p1.x < p2.x) {
                for (int x = p1.x; x <= p2.x; x++) {
                    board[x][p1.y] = 'B';
                    barriers.add(new Tuple(x, p1.y));
                }
            } else if(p1.y > p2.y) {
                for (int y = p2.y; y <= p1.y; y++) {
                    board[p1.x][y] = 'B';
                    barriers.add(new Tuple(p1.x, y));
                }
            } else if(p1.y < p2.y) {
                for (int y = p1.y; y <= p2.y; y++) {
                    board[p1.x][y] = 'B';
                    barriers.add(new Tuple(p1.x, y));
                }
            }
        }
    }

    public double step_distance(Tuple p1, Tuple p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    public Tuple[][] a_star(Tuple start, Tuple end) {
        Tuple[][] tree = new Tuple[b_width][b_height];

        double[][] f_map = new double[b_width][b_height];
        double[][] h_map = new double[b_width][b_height];
        double[][] g_map = new double[b_width][b_height];

        char[][] closed_set = new char[b_width][b_height];
        ArrayList<Tuple> open_set = new ArrayList<>();

        h_map[start.x][start.y] = 0;
        g_map[start.x][start.y] = 0;
        f_map[start.x][start.y] = h_map[start.x][start.y] + g_map[start.x][start.y];
        open_set.add(start);

        double threat_threshold = 5;

        ArrayList<Tuple> threats = new ArrayList<>();

        for(int i = 0; i < s_heads.size(); i++) {
            if(i == ms_idx) { continue; }

            double s_threat = step_distance(start, s_heads.get(i));

            if(s_threat <= threat_threshold) {
                threats.add(s_heads.get(i));
            }
        }

        for(int i = 0; i < n_zombies; i++) {
            double z_threat = step_distance(start, z_heads[i]);

            if(z_threat <= threat_threshold) {
                threats.add(z_heads[i]);
            }
        }

        int step_counter = 0;
        int max_steps = b_width + b_height;

        while(!open_set.isEmpty()) {

            if(step_counter >= max_steps) { return null; }

            step_counter++;

            Tuple current = open_set.get(0);
            open_set.remove(0);

            //Neighbours
            ArrayList<Tuple> neighbours = new ArrayList<>();

            for (int i = 0; i < 4; i++) {
                Tuple neighbour = new Tuple(current.x, current.y);
                switch (i) {
                    case 0 -> //North
                            neighbour.y -= 1;
                    case 1 -> //East
                            neighbour.x += 1;
                    case 2 -> //South
                            neighbour.y += 1;
                    case 3 -> //West
                            neighbour.x -= 1;
                }

                if (
                        neighbour.x >= b_width || neighbour.x < 0 ||
                        neighbour.y >= b_height || neighbour.y < 0 ||
                        board[neighbour.x][neighbour.y] == 'B' ||
                        board[neighbour.x][neighbour.y] == 'S' ||
                        board[neighbour.x][neighbour.y] == 'Z' ||
                        closed_set[neighbour.x][neighbour.y] == 'O'
                ) {
                    continue;
                }

                tree[neighbour.x][neighbour.y] = current;
                neighbours.add(neighbour);
            }

            //Neighbours evaluation
            boolean goal_found = false;
            for(Tuple neighbour: neighbours) {
                if(neighbour.equals(end)) { goal_found = true; break; }

                double neighbour_h = step_distance(neighbour, end);
                double neighbour_g = g_map[current.x][current.y] + 1;

                for(Tuple threat: threats) {
                    neighbour_h +=  (b_width + b_height) / step_distance(neighbour, threat);
                }

                double neighbour_f = neighbour_h + neighbour_g;

                if(board[neighbour.x][neighbour.y] == 'X' && f_map[neighbour.x][neighbour.y] < neighbour_f) { continue; }
                if(closed_set[neighbour.x][neighbour.y] == 'O' && f_map[neighbour.x][neighbour.y] < neighbour_f) { continue; }

                open_set.add(neighbour);
                h_map[neighbour.x][neighbour.y] = neighbour_h;
                g_map[neighbour.x][neighbour.y] = neighbour_g;
                f_map[neighbour.x][neighbour.y] = neighbour_f;
                board[neighbour.x][neighbour.y] = 'X';

                int i = open_set.size() - 1;
                while(
                        i > 0 &&
                                (
                                    f_map[open_set.get(i - 1).x][open_set.get(i - 1).y] > f_map[open_set.get(i).x][open_set.get(i).y] ||
                                    h_map[open_set.get(i - 1).x][open_set.get(i - 1).y] > h_map[open_set.get(i).x][open_set.get(i).y] ||
                                    g_map[open_set.get(i - 1).x][open_set.get(i - 1).y] > g_map[open_set.get(i).x][open_set.get(i).y]
                                )
                ) {
                    Collections.swap(open_set, i - 1, i);
                    i--;
                }

            }
            closed_set[current.x][current.y] = 'O';
            board[current.x][current.y] = 'O';
            if(goal_found) { break; }
        }

        ArrayList<Character> char_threats = new ArrayList<>();
        for(Tuple threat: threats) {
            char_threats.add(board[threat.x][threat.y]);
        }

        return tree;
    }

    public Tuple backtrace(Tuple[][] tree_path, Tuple start, Tuple end) {
        Tuple current = end;

        try {
            while (!tree_path[current.x][current.y].equals(start)) {
                current = tree_path[current.x][current.y];
            }
        } catch (NullPointerException npe) {
            return null;
        }
        return current;
    }

}

