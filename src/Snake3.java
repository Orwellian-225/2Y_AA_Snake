import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Snake3 extends DevelopmentAgent {

    //Game Data
    private int b_width;
    private int b_height;
    private Character[][] board;

    private ArrayList<Tuple> s_heads;
    private ArrayList<Integer> s_lengths;
    private ArrayList<Tuple> z_heads;
    private ArrayList<Tuple> barriers;
    private int idx;

    final char zombie_c = 'Z';//(char) 0x25BC;
    final char snake_c = 'S';//(char) 0x25C6;
    final char barrier_c = 'B';//(char) 0x25A6;
    final char open_c = 'X';//(char) 0x25A3;
    final char closed_c = 'O';//(char) 0x25A0;
    final char path_c = (char) 0x25CF;
    final char apple_c = 'A';//(char) 0x25CE;
    final char empty_c = (char) 0x25A2;

    public static void main(String[] args) {
        Snake3 agent = new Snake3();
        Snake3.start(agent, args);
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            //Game State Input
            int s_n;
            String init_in_data = input.readLine();
            String[] init_data = init_in_data.split(" ");
            s_n = Integer.parseInt(init_data[0]);
            b_width = Integer.parseInt(init_data[1]);
            b_height = Integer.parseInt(init_data[2]);

            board = new Character[b_width][b_height];

            int frame_count = 0;
            double ave_t = 0;
            int ave_score = 0;

            while (true) {
                frame_count++;
                s_heads = new ArrayList<>(s_n);
                s_lengths = new ArrayList<>(s_n);
                int z_n = 6;
                z_heads = new ArrayList<>(z_n);
                barriers = new ArrayList<>(s_n * 5 + z_n * 6);

                board = new Character[b_width][b_height];

                double t_net_s;
                Tuple apple;

                //Frame State Input
                String apple_in = input.readLine();

                t_net_s = System.nanoTime();

                apple_in = apple_in.replace(" ", ",");
                apple = new Tuple(apple_in);

                for (int i = 0; i < z_n; i++) {
                    String zombie_in = input.readLine();
                    String[] z_tokens = zombie_in.split(" ");
                    z_heads.add(new Tuple(z_tokens[0]));
                    mark_barriers(z_tokens);
                    update_map(board, zombie_c, z_heads.get(z_heads.size() - 1));
                    barriers.remove(z_heads.get(z_heads.size() - 1));

                    update_map(board, barrier_c, new Tuple(z_heads.get(z_heads.size() - 1).x + 1, z_heads.get(z_heads.size() - 1).y));
                    update_map(board, barrier_c, new Tuple(z_heads.get(z_heads.size() - 1).x - 1, z_heads.get(z_heads.size() - 1).y));
                    update_map(board, barrier_c, new Tuple(z_heads.get(z_heads.size() - 1).x, z_heads.get(z_heads.size() - 1).y + 1));
                    update_map(board, barrier_c, new Tuple(z_heads.get(z_heads.size() - 1).x, z_heads.get(z_heads.size() - 1).y - 1));

                }

                idx = Integer.parseInt(input.readLine());

                for (int i = 0; i < s_n; i++) {
                    String snake_in = input.readLine();
                    String[] s_tokens = snake_in.split(" ");

                    if (s_tokens[0].equalsIgnoreCase("dead")) {
                        if (i < idx) {
                            idx--;
                        }
                        continue;
                    }

                    s_heads.add(new Tuple(s_tokens[3]));
                    s_lengths.add(Integer.parseInt(s_tokens[1]));
                    mark_barriers(Arrays.copyOfRange(s_tokens, 3, s_tokens.length));
                    barriers.remove(s_heads.get(s_heads.size() - 1));
                    update_map(board, snake_c, s_heads.get(s_heads.size() - 1));
                }

                update_map(board, 'M', s_heads.get(idx));
                update_map(board, apple_c, apple);

                //Navigation
                Tuple[][] path = a_star(s_heads.get(idx), apple);
                Tuple next = next_move(path, s_heads.get(idx), apple);


                int move = calc_move(next);
                System.out.println(move);

                double frame_t = (System.nanoTime() - t_net_s) / 1e6;
                ave_t += frame_t;
                ave_score += s_lengths.get(idx);
                if(frame_t >= 10.0) log("Time Spike: " + frame_t);
                log(frame_count + " Average Time: " + ave_t/frame_count + " Average Score: " + ave_score/frame_count + " Longest: " + this.getLongest() + " Score: " + s_lengths.get(idx));

            }

        } catch(IOException ioe)  {
            ioe.printStackTrace();

        }
    }

    private void log(String input) {
        System.out.println("log " + input);
    }

    private void mark_barriers(String[] turns) {
        for(int i = 0; i < turns.length - 1; i++) {

            Tuple t1 = new Tuple(turns[i]);
            Tuple t2 = new Tuple(turns[i + 1]);

            Tuple start = new Tuple( Integer.min(t1.x, t2.x), Integer.min(t1.y, t2.y) );
            Tuple end = new Tuple( Integer.max(t1.x, t2.x), Integer.max(t1.y, t2.y) );

            for(int x = start.x; x <= end.x; x++) {
                for(int y = start.y; y <= end.y; y++) {
                    Tuple b = new Tuple(x, y);
                    update_map(board, barrier_c, b);
                    barriers.add(b);
                }
            }

        }
    }

    private Tuple[][] a_star(Tuple start, Tuple goal) {
        Tuple[][] tree = new Tuple[b_width][b_height];

        Double[][] f = new Double[b_width][b_height];
        Double[][] h = new Double[b_width][b_height];
        Double[][] g = new Double[b_width][b_height];

        for(Character[] row: board) {
            for(Character col: row) {
                if (col == null || col.equals(closed_c) || col.equals(open_c)) {
                    col = empty_c;
                }
            }
        }

        PriorityQueue<Tuple> open_set = new PriorityQueue<>((o1, o2) -> {
            if(
                    read_map(f, o1) < read_map(f, o2)  ||
                    (Objects.equals(read_map(f, o1), read_map(f, o2)) && read_map(h, o1) < read_map(h, o2))
            ) {
                return -1;
            } else if(
                    !(read_map(f, o1) < read_map(f, o2)  ||
                    (read_map(f, o1) == read_map(f, o2) && read_map(h, o1) < read_map(h, o2)))
            ) {
                return 1;
            } else {
                return 0;
            }
        });

        update_map(f, 0.0, start);
        update_map(g, 0.0, start);
        update_map(h, 0.0, start);
        open_set.add(start);
        open_set.add(start);

        while(!open_set.isEmpty()) {
            Tuple current = open_set.poll();

            ArrayList<Tuple> neighbours = new ArrayList<>(4);
            for (int i = 0; i < 4; i++) {
                Tuple neighbour = new Tuple(current);

                switch(i) {
                    case 0 -> neighbour.y -= 1;
                    case 1 -> neighbour.y += 1;
                    case 2 -> neighbour.x -= 1;
                    case 3 -> neighbour.x += 1;
                }

                if( !invalid_point(neighbour) ) {
                    update_map(tree, current, neighbour);
                    neighbours.add(neighbour);
                }
            } //Generate Neighbours

            boolean goal_found = false;
            for(Tuple neighbour: neighbours) {
                if( neighbour.equals(goal) ) { goal_found = true; break; }

                double n_h = mhn_heuristic(neighbour, goal);

                for(Tuple s: s_heads) {
                    n_h += sigmoid(mhn_heuristic(neighbour, s), 10);
                }


                double n_g = read_map(g, current) + 1;
                double n_f = n_h + n_g;

                Character map_val = read_map(board, neighbour);
                if ( map_val != null && (( open_set.contains(neighbour) || map_val.equals(closed_c)))) {
                    continue;
                }

                update_map(h, n_h, neighbour);
                update_map(g, n_g, neighbour);
                update_map(f, n_f, neighbour);
                update_map(board, open_c, neighbour);
                open_set.add(neighbour);

            } //Eval Neighbours

            update_map(board, closed_c, current);
            if( goal_found ) break;
        }

        return tree;
    }

    private Tuple next_move(Tuple[][] path_tree, Tuple start, Tuple end) {
        Tuple current = end;

        try {
            while(!read_map(path_tree, current).equals(start)) {
                current = read_map(path_tree, current);
            }
         } catch (NullPointerException npe) {
            return null;
        }

        return current;
    }

    private Tuple safe_move(Tuple t1) {
        Random r = new Random();
        Tuple random_point = new Tuple(r.nextInt(0, 50), r.nextInt(0, 50));

        while(invalid_point(random_point)) {
            random_point = new Tuple(r.nextInt(0, 50), r.nextInt(0, 50));
        }

        Tuple[][] path = a_star(s_heads.get(idx), random_point);
        return next_move(path, s_heads.get(idx), random_point);
    }

    private int calc_move(Tuple next) {
        if(next == null) { return 5; }
        else if(next.x > s_heads.get(idx).x) { return 3; }
        else if(next.x < s_heads.get(idx).x) { return 2; }
        else if(next.y > s_heads.get(idx).y) { return 1; }
        else if(next.y < s_heads.get(idx).y) { return 0; }
        else { return 5; }
    }

    private double mhn_heuristic(Tuple t1, Tuple t2) {
        double x_abs = Math.abs(t1.x - t2.x);
        double y_abs = Math.abs(t1.y - t2.y);

        double scale_1 =  10;
        double scale_2 = 8;

        double dist;
        if( x_abs >= y_abs ) {
            dist = scale_1 * x_abs + scale_2 * y_abs;
        } else {
            dist = scale_2 * x_abs + scale_1 * y_abs;
        }

        return dist;
    }

    private double mhn_distance(Tuple t1, Tuple t2) {
        try {
            return Math.abs(t1.y - t2.y) + Math.abs(t1.x - t2.x);
        } catch (NullPointerException ignored) {
            return Double.MAX_VALUE;
        }
    }

    private boolean can_reach(Tuple t1, Tuple t2, int steps) {
        return mhn_distance(t1, t2) <= steps;
    }

    private <T> void update_map(T[][] map, T value, Tuple t) {
        if (t.x >= map.length || t.x < 0 || t.y >= map[t.x].length || t.y < 0) { return; }
        map[t.y][t.x] = value;
    }

    private <T> T read_map(T[][] map, Tuple t) {
        try {
            return map[t.y][t.x];
        } catch (NullPointerException npe) {
            return null;
        }
    }

    private <T> String print_map(T[][] map) {
        StringBuilder result = new StringBuilder();
        for(T[] row: map) {
            for(T col: row) {
                try {
                    result.append(col.toString());
                } catch (NullPointerException npe) {
                    result.append(empty_c);
                }
            }
            result.append("\n");
        }

        return result.toString();
    }

    private boolean invalid_point(Tuple point) {
        try {
            return read_map(board, point) == barrier_c ||
                    read_map(board, point) == zombie_c||
                    read_map(board, point) == snake_c ||
                    read_map(board, point) == closed_c;
        } catch (IndexOutOfBoundsException iobe) {
            return true;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    private boolean invalid_point_std(Tuple point) {
        try {
            return read_map(board, point) == barrier_c ||
                    read_map(board, point) == zombie_c||
                    read_map(board, point) == snake_c;
        } catch (IndexOutOfBoundsException iobe) {
            return true;
        } catch (NullPointerException npe) {
            return false;
        }
    }

    public double sigmoid(double x) {
        double e = Math.exp(2.0*x-6.0);
        return -1 * e/(1.0 + e) + 1.0;
    }

    public double sigmoid(double x, double t) {
        double e = Math.exp(2/t *x-6.0);
        return -1 * e/(1.0 + e) + 1.0;
    }
}
