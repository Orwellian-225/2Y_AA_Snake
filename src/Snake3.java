import za.ac.wits.snake.DevelopmentAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Snake3 extends DevelopmentAgent {

    //Game Data
    private int b_width;
    private int b_height;
    private int s_n;
    private final int z_n = 6;
    private Character[][] board;

    private ArrayList<Tuple> s_heads;
    private ArrayList<Tuple> z_heads;
    private ArrayList<Tuple> barriers;
    private int idx;

    public static void main(String[] args) {
        Snake3 agent = new Snake3();
        Snake3.start(agent, args);
    }

    @Override
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            //Game State Input
            {
                String init_in_data = input.readLine();
                String[] init_data = init_in_data.split(" ");
                s_n = Integer.parseInt(init_data[0]);
                b_width = Integer.parseInt(init_data[1]);
                b_height = Integer.parseInt(init_data[2]);

                board = new Character[b_width][b_height];
            }

            int frame_count = 0;
            while (true) {
                frame_count++;
                s_heads = new ArrayList<>(s_n);
                z_heads = new ArrayList<>(z_n);
                barriers = new ArrayList<>(s_n * 5 + z_n * 6);

                board = new Character[b_width][b_height];

                double t_net_s;
                Tuple apple;

                //Frame State Input
                double t_parse_s;
                {
                    String apple_in = input.readLine();

                    if (apple_in.equalsIgnoreCase("game over")) {
                        log("Game Over");
                        break;
                    }

                    t_net_s = System.nanoTime();
                    t_parse_s = System.nanoTime();

                    apple_in = apple_in.replace(" ", ",");
                    apple = new Tuple(apple_in);
                    update_map(board, 'A', apple);

                    for (int i = 0; i < z_n; i++) {
                        String zombie_in = input.readLine();
                        String[] z_tokens = zombie_in.split(" ");
                        z_heads.add(new Tuple(z_tokens[0]));
                        mark_barriers(z_tokens);
                        update_map(board, 'Z', z_heads.get(z_heads.size() - 1));
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
                        mark_barriers(Arrays.copyOfRange(s_tokens, 3, s_tokens.length));
                        update_map(board, 'S', s_heads.get(s_heads.size() - 1));
                    }

                    update_map(board, 'M', s_heads.get(idx));
                }
                double t_parse_d = (System.nanoTime() - t_parse_s) / 1e6;

                //Navigation
                double t_nav_s = System.nanoTime();

                Tuple[][] path = a_star(s_heads.get(idx), apple);
                Tuple next = next_move(path, s_heads.get(idx), apple);
                double t_nav_d = (System.nanoTime() - t_nav_s) / 1e6;

                int move = calc_move(next);
                System.out.println(move);

                double t_net_d = (System.nanoTime() - t_net_s) / 1e6;
                log(frame_count + " Net Time: " + t_net_d + " Nav Time: " + t_nav_d + " Parse Time: " + t_parse_d);

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
                    update_map(board, 'B', new Tuple(x, y));
                }
            }

        }
    }

    private Tuple[][] a_star(Tuple start, Tuple goal) {
        Tuple[][] tree = new Tuple[b_width][b_height];

        Double[][] f = new Double[b_width][b_height];
        Double[][] h = new Double[b_width][b_height];
        Double[][] g = new Double[b_width][b_height];

        ArrayList<Tuple> open_set = new ArrayList<>();

        final int max_steps = b_width * b_height;
        int step_counter = 0;

        open_set.add(start);

        while(!open_set.isEmpty() ) {
            if(step_counter >= max_steps) { break; }
            step_counter++;

            Tuple current = open_set.get(0);
            open_set.remove(current);

            ArrayList<Tuple> neighbours = new ArrayList<>(4);
            {
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
                }
            } //Generate Neighbours

            boolean goal_found = false;
            for(Tuple neighbour: neighbours) {
                if( neighbour.equals(goal) ) { goal_found = true; break; }

                double n_h = mhn_distance(neighbour, goal);
                double n_g; try { n_g = read_map(g, current) + 1; } catch (NullPointerException npe) { n_g = 0.0; }
                double n_f = n_h + n_g;

                try {
                    if ((read_map(board, neighbour) == 'X' || read_map(board, neighbour) == '0') && read_map(f, neighbour) < n_f) {
                        continue;
                    }
                } catch (NullPointerException ignored) {

                }

                open_set.add(neighbour);
                update_map(h, n_h, neighbour);
                update_map(g, n_g, neighbour);
                update_map(f, n_f, neighbour);
                update_map(board, 'X', neighbour);

                //Sort queue
                {
                    int i = open_set.size() - 1;
                    while ( i > 0 && (
                                        read_map(f, open_set.get(i - 1)) > read_map(f, open_set.get(i)) ||
                                        read_map(h, open_set.get(i - 1)) > read_map(h, open_set.get(i)) ||
                                        read_map(g, open_set.get(i - 1)) > read_map(g, open_set.get(i))
                                    )
                    ) {
                        Collections.swap(open_set, i - 1, i);
                        i--;
                    }
                }
            }

            update_map(board, 'O', current);
            if( goal_found ) break;
        }

        return tree;
    }

    private Tuple next_move(Tuple[][] path_tree, Tuple start, Tuple end) {
        Tuple current = end;

        try {
            while(!read_map(path_tree, current).equals(start)) { current = read_map(path_tree, current); }
         } catch (NullPointerException npe) {
            return null;
        }

        return current;
    }

    private int calc_move(Tuple next) {
        if(next == null) { return 5; }
        else if(next.x > s_heads.get(idx).x) { return 3; }
        else if(next.x < s_heads.get(idx).x) { return 2; }
        else if(next.y > s_heads.get(idx).y) { return 1; }
        else if(next.y < s_heads.get(idx).y) { return 0; }
        else { return 5; }
    }

    private double mhn_distance(Tuple t1, Tuple t2) { return Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y); }

    private <T> void update_map(T[][] map, T value, Tuple t) {
        map[t.y][t.x] = value;
    }

    private <T> T read_map(T[][] map, Tuple t) throws NullPointerException {
            return map[t.y][t.x];
    }

    private <T> String print_map(T[][] map) {
        StringBuilder result = new StringBuilder();
        for(T[] row: map) {
            for(T col: row) {
                try {
                    result.append(col.toString());
                } catch (NullPointerException npe) {
                    result.append((char) 0x25A0);
                }
            }
            result.append("\n");
        }

        return result.toString();
    }

    private boolean invalid_point(Tuple point) {
        try {
            return read_map(board, point) == 'B' ||
                    read_map(board, point) == 'Z' ||
                    read_map(board, point) == 'S' ||
                    read_map(board, point) == 'O';
        } catch (IndexOutOfBoundsException iobe) {
            return true;
        } catch (NullPointerException npe) {
            return false;
        }
    }
}
