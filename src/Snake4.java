import za.ac.wits.snake.DevelopmentAgent;

import java.io.*;
import java.util.*;

public class Snake4 extends DevelopmentAgent {

    private static class ga_params {
        public static double µ_hg = 10.0; //heuristic distance greater
        public static double µ_hl = 8.0; //heuristic distance lesser
        public static double µ_sm = 4.0; //snake midpoint
        public static double µ_sx = 100.0; //snake multiplier
        public static double µ_zm = 2.0; //zombie midpoint
        public static double µ_zx = 100.0; //zombie multiplier
        public static double µ_bm = 1.0; //barrier midpoint
        public static double µ_bx = 50.0; //barrier multiplier
    }

    private char[][] board;
    private boolean[][] invalid_walls;
    private ArrayList<Tuple> z;
    private ArrayList<Tuple> s;
    private ArrayList<Integer> s_lengths;
    private ArrayList<Tuple> b;
    private Tuple apple;
    int idx;

    private static class game_config {
        public static int width;
        public static int height;
        public static int num_s;
        public static int num_z = 6;
        public static int[] y_neighbours = {0, 0, 1, -1};
        public static int[] x_neighbours = {1, -1, 0, 0};
    }

    private static class game_chars {
        public static char empty = (char) 0x25a0;
        public static char apple = 'A';
        public static char snake = 'S';
        public static char me = 'M';
        public static char zombie = 'Z';
        public static char barrier = 'B';
        public static char open = 'X';
        public static char close = 'O';
    }

    public String ga_file;

    public static void main(String[] args) {


        Snake4 agent = new Snake4();
        agent.ga_file = args[1];
        args = Arrays.copyOfRange(args, 0, 1);
        Snake4.start(agent, args);
    }

    @Override
    public void run() {
        try {
            BufferedReader ga_input = new BufferedReader(new FileReader(ga_file));
            ga_params.µ_hg = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_hl = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_sm = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_sx = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_zm = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_zx = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_bm = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_params.µ_bx = Double.parseDouble(ga_input.readLine().split(": ")[1]);
            ga_input.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (NullPointerException npe) {
            ga_params.µ_hg = 10.0; //heuristic distance greater
            ga_params.µ_hl = 8.0; //heuristic distance lesser
            ga_params.µ_sm = 4.0; //snake midpoint
            ga_params.µ_sx = 100.0; //snake multiplier
            ga_params.µ_zx = 100.0; //zombie multiplier
            ga_params.µ_bm = 1.0; //barrier midpoint
            ga_params.µ_bx = 50.0; //barrier multiplier

            npe.printStackTrace();
        }

        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            //Game State Input
            String init_in_data = input.readLine();
            String[] init_data = init_in_data.split(" ");
            game_config.num_s = Integer.parseInt(init_data[0]);
            game_config.width = Integer.parseInt(init_data[1]);
            game_config.height = Integer.parseInt(init_data[2]);

            board = new char[game_config.width][game_config.height];

            int frame_count = 0;
            double ave_score = 0;
            double ave_time = 0;
            while (true) {
                ++frame_count;
                //Parse
                s = new ArrayList<>(game_config.num_s);
                s_lengths = new ArrayList<>(game_config.num_s);
                z = new ArrayList<>(game_config.num_z);
                b = new ArrayList<>(game_config.num_s * 5 + game_config.num_z * 6);
                invalid_walls = new boolean[game_config.width][game_config.height];
                board = new char[game_config.width][game_config.height];

                String apple_in = input.readLine();
                double frame_time_start = System.nanoTime();
                apple_in = apple_in.replace(' ', ',');
                apple = new Tuple(apple_in);

                for (int i = 0; i < game_config.num_z; i++) {
                    String zombie_in = input.readLine();
                    String[] z_tokens = zombie_in.split(" ");
                    z.add(new Tuple(z_tokens[0]));
                    mark_barriers(z_tokens);
                    maps.update(board, z.get(z.size() - 1), game_chars.zombie);
                    maps.update(invalid_walls, z.get(z.size() - 1), true);
                    b.remove(z.get(z.size() - 1));

                    for(int neighbour = 0; neighbour < 4; neighbour++) {
                        Tuple neighbour_t = new Tuple(z.get(z.size() - 1).x + game_config.x_neighbours[neighbour], z.get(z.size() - 1).y + game_config.y_neighbours[neighbour]);
                        if(neighbour_t.x >= 0 && neighbour_t.x < game_config.width && neighbour_t.y >= 0 && neighbour_t.y < game_config.height) {
                            maps.update(board, neighbour_t, game_chars.barrier);
                            maps.update(invalid_walls, neighbour_t, true);
                        }
                    }

                }

                idx = Integer.parseInt(input.readLine());

                for (int i = 0; i < game_config.num_s; i++) {
                    String snake_in = input.readLine();
                    String[] s_tokens = snake_in.split(" ");

                    if (s_tokens[0].equalsIgnoreCase("dead")) {
                        if (i < idx) {
                            idx--;
                        }
                        continue;
                    }

                    s.add(new Tuple(s_tokens[3]));
                    s_lengths.add(Integer.parseInt(s_tokens[1]));
                    mark_barriers(Arrays.copyOfRange(s_tokens, 3, s_tokens.length));
                    b.remove(s.get(s.size() - 1));
                    maps.update(board, s.get(s.size() - 1), game_chars.snake);
                    maps.update(invalid_walls, s.get(s.size() - 1), true);
                }

                maps.update(board, s.get(idx), game_chars.me);
                maps.update(board, apple, game_chars.apple);

                //Make decision
                Tuple next;
                next = move_apple();
                ArrayList<Tuple> s_alts = new ArrayList<>(s);
                s_alts.remove(idx);
                if (next == null || array_can_reach(s_alts, next, 1) || array_can_reach(z, next, 1)) {
                    next = move_safe(next);
                }

                System.out.println(move_direction(next));

                double frame_t = (System.nanoTime() - frame_time_start) / 1e6;
                ave_time += frame_t;
                ave_score += s_lengths.get(idx);

                if(frame_count == 2390) {
                    BufferedWriter ga_output = new BufferedWriter(new FileWriter(ga_file));
                    ga_output.write("mew heuristic greater: " + ga_params.µ_hg + "\n");
                    ga_output.write("mew heuristic lesser: " + ga_params.µ_hl + "\n");
                    ga_output.write("mew snake greater: " + ga_params.µ_sm + "\n");
                    ga_output.write("mew snake greater: " + ga_params.µ_sx + "\n");
                    ga_output.write("mew zombie greater: " + ga_params.µ_zm + "\n");
                    ga_output.write("mew zombie greater: " + ga_params.µ_zx + "\n");
                    ga_output.write("mew barrier greater: " + ga_params.µ_bm + "\n");
                    ga_output.write("mew barrier greater: " + ga_params.µ_bx + "\n");
                    ga_output.write("average time: " + ave_time / frame_count + "\n");
                    ga_output.write("average score: " + ave_score / frame_count + "\n");
                    ga_output.write("longest score: " + this.getLongest() + "\n");
                    ga_output.close();
                }
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public Tuple move_apple() { return tree_trace(a_star(s.get(idx), apple), s.get(idx), apple); }
    public Tuple move_safe(Tuple prev_move) {
        Tuple head = s.get(idx);

        for (int i = 0; i < 4; i++) {
            Tuple neighbour = new Tuple(head);

            neighbour.x += game_config.x_neighbours[i];
            neighbour.y += game_config.y_neighbours[i];

            if( !invalid_point(neighbour) && ( prev_move == null || !neighbour.equals(prev_move)) ) {
                return neighbour;
            }
        }

        return null;
    }

    public Tuple[][] a_star(Tuple start, Tuple end) {
        Tuple[][] tree = new Tuple[game_config.width][game_config.height];

        double[][] f = new double[game_config.width][game_config.height];
        double[][] h = new double[game_config.width][game_config.height];
        double[][] g = new double[game_config.width][game_config.height];

        PriorityQueue<Tuple> open_set = new PriorityQueue<>((o1, o2) -> {
            if (
                    maps.read(f, o1) < maps.read(f, o2) ||
                            (Objects.equals(maps.read(f, o1), maps.read(f, o2)) && maps.read(h, o1) < maps.read(h, o2))
            ) {
                return -1;
            } else if (
                    !(maps.read(f, o1) < maps.read(f, o2) ||
                            (maps.read(f, o1) == maps.read(f, o2) && maps.read(h, o1) < maps.read(h, o2)))
            ) {
                return 1;
            } else {
                return 0;
            }
        });
        boolean[][] closed_set = new boolean[game_config.width][game_config.height];

        maps.update(f, start, 0.0);
        maps.update(g, start, 0.0);
        maps.update(h, start, 0.0);
        open_set.add(start);

        while(!open_set.isEmpty()) {
            Tuple current = open_set.poll();

            ArrayList<Tuple> neighbours = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                Tuple neighbour = new Tuple(current);

                neighbour.x += game_config.x_neighbours[i];
                neighbour.y += game_config.y_neighbours[i];

                if( !invalid_point_closed(neighbour) ) {
                    maps.update(tree, neighbour, current);
                    neighbours.add(neighbour);
                }
            } //Generate Neighbours

            boolean goal_found = false;
            for(Tuple neighbour: neighbours) {
                if( neighbour.equals(end) ) { goal_found = true; break; }

                double n_h = point_heuristic(neighbour, end);

                for(Tuple snake: s) {
                    n_h += sigmoid(Tuple.mhn_distance(neighbour, snake), ga_params.µ_sm, ga_params.µ_sx);
                }

                for(Tuple zombie: z) {
                    n_h += sigmoid(Tuple.mhn_distance(neighbour, zombie), ga_params.µ_zm, ga_params.µ_zx);
                }

                for(Tuple barrier: b) {
                    n_h += sigmoid(Tuple.mhn_distance(neighbour, barrier), ga_params.µ_bm, ga_params.µ_bx);
                }

                double n_g = maps.read(g, current) + 1;
                double n_f = n_h + n_g;

                if ( maps.read(board, neighbour) != (char) 0x0 && (( open_set.contains(neighbour) || maps.read(closed_set, neighbour)))) {
                    continue;
                }

                maps.update(h, neighbour, n_h);
                maps.update(g, neighbour, n_g);
                maps.update(f, neighbour, n_f);
                maps.update(board, neighbour, game_chars.open);
                open_set.add(neighbour);

            } //Eval Neighbours

            maps.update(board, current, game_chars.close);
            maps.update(closed_set, current, true);
            if( goal_found ) break;
        }

        return tree;
    }

    public Tuple tree_trace(Tuple[][] tree, Tuple start, Tuple end) {
        Tuple current = end;

        try {
            while(true) {
                if(maps.read(tree, current).equals(start)) { return current; }
                current = maps.read(tree, current);
            }
        } catch (NullPointerException npe) {
            return null;
        }
    }

    public int move_direction(Tuple move) {
        if(move == null) { return 5; }
        else if(move.x > s.get(idx).x) { return 3; }
        else if(move.x < s.get(idx).x) { return 2; }
        else if(move.y > s.get(idx).y) { return 1; }
        else if(move.y < s.get(idx).y) { return 0; }
        else { return 5; }
    }

    public double point_heuristic(Tuple t1, Tuple t2) {
        double x = Tuple.mhn_x(t1, t2);
        double y = Tuple.mhn_y(t1, t2);

        if(x > y) {
            return ga_params.µ_hg*x + ga_params.µ_hl*y;
        } else {
            return ga_params.µ_hl*x + ga_params.µ_hg*y;
        }
    }

    public boolean can_reach(Tuple t1, Tuple t2, int step) { return Tuple.mhn_distance(t1, t2) <= step; }

    public boolean array_can_reach(ArrayList<Tuple> list, Tuple point, int step) {
        boolean result = false;
        for(Tuple t: list) {
            result = result || can_reach(t, point, step);
        }
        return result;
    }

    public void mark_barriers(String[] turns) {
        for(int i = 0; i < turns.length - 1; i++) {

            Tuple t1 = new Tuple(turns[i]);
            Tuple t2 = new Tuple(turns[i + 1]);

            Tuple start = new Tuple( Integer.min(t1.x, t2.x), Integer.min(t1.y, t2.y) );
            Tuple end = new Tuple( Integer.max(t1.x, t2.x), Integer.max(t1.y, t2.y) );

            for(int x = start.x; x <= end.x; x++) {
                for(int y = start.y; y <= end.y; y++) {
                    Tuple barrier = new Tuple(x, y);
                    maps.update(board, barrier, game_chars.barrier);
                    maps.update(invalid_walls, barrier, true);
                    b.add(barrier);
                }
            }

        }
    }

    private static class maps {
        public static Tuple read(Tuple[][] map, Tuple point) { return map[point.x][point.y]; }
        public static double read(double[][] map, Tuple point) { return map[point.x][point.y]; }
        public static char read(char[][] map, Tuple point) { return map[point.x][point.y]; }
        public static boolean read(boolean[][] map, Tuple point) { return map[point.x][point.y]; }

        public static void update(Tuple[][] map, Tuple point, Tuple value) { map[point.x][point.y] = value; }
        public static void update(double[][] map, Tuple point, double value) { map[point.x][point.y] = value; }
        public static void update(char[][] map, Tuple point, char value) { map[point.x][point.y] = value; }
        public static void update(boolean[][] map, Tuple point, boolean value) { map[point.x][point.y] = value; }

    }

    private boolean invalid_point_closed(Tuple point) {

        //Including the closed set as part of invalid moves

        if(point.x < game_config.width && point.x >= 0 && point.y < game_config.height && point.y >= 0) {
            return maps.read(board, point) == game_chars.barrier ||
                    maps.read(board, point) == game_chars.zombie ||
                    maps.read(board, point) == game_chars.snake ||
                    maps.read(board, point) == game_chars.close;
        } else {
            return true;
        }
    }

    private boolean invalid_point(Tuple point) {

        //not including the closed set

        if(point.x < game_config.width && point.x >= 0 && point.y < game_config.height && point.y >= 0) {
            return maps.read(board, point) == game_chars.barrier ||
                    maps.read(board, point) == game_chars.zombie ||
                    maps.read(board, point) == game_chars.snake;
        } else {
            return true;
        }
    }

    public double sigmoid(double x, double midpoint, double multiplier) {
        double scale = Math.sqrt(midpoint);
        double e = Math.exp(1/scale * x - scale);
        double f = -1 * e/(1 + e) + 1;
        return multiplier * f;
    }

    private static class debug {
        public static void log(String message) {
            System.out.println("log " + message);
        }

        public static String print(char[][] map) {
            StringBuilder result = new StringBuilder();
            for(char[] row: map) {
                for(char col: row) {
                    result.append(col);
                }
                result.append("\n");
            }

            return result.toString();
        }
    }
}
