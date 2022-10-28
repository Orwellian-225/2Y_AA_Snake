import za.ac.wits.snake.DevelopmentAgent;

import javax.swing.plaf.synth.SynthTextAreaUI;
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
    public ArrayList<Integer> s_lengths = new ArrayList<>();
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

        	try {
	            //Parse Input ==========================================================================================
                board = new char[b_width][b_height];
	            s_heads = new ArrayList<>(n_snakes);
                s_lengths = new ArrayList<>(n_snakes);

                String apple_str = in.nextLine();
                double parse_st = System.nanoTime();
                double time_s = System.nanoTime();
                String[] apple_split = apple_str.split(" ");
                Tuple apple = new Tuple(Integer.parseInt(apple_split[0]), Integer.parseInt(apple_split[1]));


                for(int i = 0; i < n_zombies; i++) {
                    String zombie_str = in.nextLine();
                    String[] zombie_split = zombie_str.split(" ");

                    z_heads[i] = new Tuple(zombie_split[0]);
                    mark_barriers(zombie_split);
                    board[z_heads[i].x][z_heads[i].y] = 'Z';

                    if(z_heads[i].x + 1 < b_width) { board[z_heads[i].x + 1][z_heads[i].y] = 'B'; }
                    if(z_heads[i].x - 1 > -1) { board[z_heads[i].x - 1][z_heads[i].y] = 'B'; }
                    if(z_heads[i].y + 1 < b_height) { board[z_heads[i].x][z_heads[i].y + 1] = 'B'; }
                    if(z_heads[i].y - 1 > -1) { board[z_heads[i].x][z_heads[i].y - 1] = 'B'; }
                }

                int ms_idx = Integer.parseInt(in.nextLine());

                for(int i = 0; i < n_snakes; i++) {
                    String snake_str = in.nextLine();
                    String[] snake_split = snake_str.split(" ");
                    if(snake_split[0].equalsIgnoreCase("dead")) {
                        if(i < ms_idx) { ms_idx--; }
                        continue;
                    }

                    s_lengths.add(Integer.parseInt(snake_split[1]));
                    s_heads.add(new Tuple(snake_split[3]));

                    String[] snake_body = Arrays.copyOfRange(snake_split, 3, snake_split.length);
                    mark_barriers(snake_body);

                    board[s_heads.get(s_heads.size() - 1).x][s_heads.get(s_heads.size() - 1).y] = 'S';
                }
                double parse_t = (System.nanoTime() - parse_st) / 1e6;
	            //======================================================================================================

	            //A* ===================================================================================================
                double nav_st = System.nanoTime();
	            Tuple[][] path_tree = a_star(s_heads.get(ms_idx), apple);
	            Tuple next = backtrace(path_tree, s_heads.get(ms_idx), apple);
                double nav_t = (System.nanoTime() - nav_st) / 1e6;
	            //======================================================================================================

	            //Move Direction Calculation ===========================================================================
	            if(next == null) { System.out.println(5); }
	            else if(next.x > s_heads.get(ms_idx).x) { System.out.println(3); }
	            else if(next.x < s_heads.get(ms_idx).x) { System.out.println(2); }
	            else if(next.y > s_heads.get(ms_idx).y) { System.out.println(1); }
	            else if(next.y < s_heads.get(ms_idx).y) { System.out.println(0); }
	            //======================================================================================================

                double time = (System.nanoTime() - time_s) / 1e6;
                if(time > 45) { System.out.println("log Time: " + time); }
        	} catch(Exception e) {
        		e.printStackTrace();
        	}
        }
    }

    public void mark_barriers(String[] barrier_coords) {
        for(int j = 0; j < barrier_coords.length - 1; j++) {
            String[] coord_1_str = barrier_coords[j].split(","), coord_2_str = barrier_coords[j + 1].split(",");
            int[] coord_1 = {Integer.parseInt(coord_1_str[0]), Integer.parseInt(coord_1_str[1])};
            int[] coord_2 = {Integer.parseInt(coord_2_str[0]), Integer.parseInt(coord_2_str[1])};

            int start_x = coord_1[0], end_x = coord_2[0];
            int start_y = coord_1[1], end_y = coord_2[1];

            if(end_x < start_x) { end_x = coord_1[0]; start_x = coord_2[0]; }
            if(end_y < start_y) { end_y = coord_1[1]; start_y = coord_2[1]; }

            for(int x = start_x; x <= end_x; x++) {
                for(int y = start_y; y <= end_y; y++) {
                    board[x][y] = 'B';
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

        double goal_distance = step_distance(start, end);
        double snake_threat_threshold = Math.ceil((goal_distance + s_lengths.get(ms_idx)) / 10);
        double zombie_threat_threshold = Math.ceil((goal_distance + s_lengths.get(ms_idx)) / 10);

        ArrayList<Tuple> threats = new ArrayList<>();

        for(int i = 0; i < s_heads.size(); i++) {
            if(i == ms_idx) { continue; }

            double s_threat = step_distance(start, s_heads.get(i));

            if(s_threat <= snake_threat_threshold) {
                threats.add(s_heads.get(i));
            }
        }

        for(int i = 0; i < n_zombies; i++) {
            double z_threat = step_distance(start, z_heads[i]);

            if(z_threat <= zombie_threat_threshold) {
                threats.add(z_heads[i]);
            }
        }


        int step_counter = 0;
        int max_steps = (b_width * b_height);

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
                    case 1 -> //South
                            neighbour.y += 1;
                    case 2 -> //East
                            neighbour.x += 1;
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

