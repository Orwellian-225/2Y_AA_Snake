import za.ac.wits.snake.DevelopmentAgent;

import java.util.*;
import java.io.*;

public class SnakeAgent extends DevelopmentAgent {

    final private double mew_apple = 1;
    private Tuple snake_me = new Tuple();
    private Tuple apple = new Tuple();

    private int board_width;
    private int board_height;

    public static void main(String[] args) {
        SnakeAgent agent = new SnakeAgent();
        SnakeAgent.start(agent, args);
    }

    @Override
    public void run() {

        Scanner in = new Scanner(System.in);
        String gameInitialisation = in.nextLine();

        String[] gameSplit = gameInitialisation.split(" ");
        board_width = Integer.parseInt(gameSplit[1]);
        board_height = Integer.parseInt(gameSplit[2]);
        final int snake_count = Integer.parseInt(gameSplit[0]);
        final int zombie_count = 6;

        int[][] board = new int[board_height][board_width];

        while(true) {
            try {
                //Parsing Input ========================================================================================
                String[] input = new String[12];

                for (int i = 0; i < 7 + snake_count; i++) {
                    input[i] = in.nextLine();
                }

                apple.update(input[0].replace(" ", ","));
                board[apple.x][apple.x] = 9;

                for (int i = 0; i < zombie_count; i++) {
                    String[] zombie = input[i + 1].split(" ");

                    for (int j = 0; j < zombie.length - 1; j++) {
                        String[] zj = zombie[j].split(",");
                        String[] zj1 = zombie[j + 1].split(",");
                        int zj_x = Integer.parseInt(zj[0]), zj_y = Integer.parseInt(zj[1]);
                        int zj1_x = Integer.parseInt(zj1[0]), zj1_y = Integer.parseInt(zj1[1]);
                        mark_barriers(board, zj_x, zj_y, zj1_x, zj1_y);

                        String[] vals = zombie[0].split(",");
                        board[Integer.parseInt(vals[0])][Integer.parseInt(vals[1])] = 2;
                    }
                }

                int me_idx = Integer.parseInt(input[7]);

                for (int i = 0; i < snake_count; i++) {
                    String[] snake = input[i + 8].split(" ");

                    if (snake[0].equals("dead")) {
                        continue;
                    }

                    if (i == me_idx) {
                        snake_me = new Tuple(snake[3]);
                    }

                    for (int j = 3; j < snake.length - 1; j++) {
                        String[] sj = snake[j].split(",");
                        String[] sj1 = snake[j + 1].split(",");
                        int sj_x = Integer.parseInt(sj[0]), sj_y = Integer.parseInt(sj[1]);
                        int sj1_x = Integer.parseInt(sj1[0]), sj1_y = Integer.parseInt(sj1[1]);
                        mark_barriers(board, sj_x, sj_y, sj1_x, sj1_y);
                    }

                    String[] snake_head = snake[3].split(",");
                    board[Integer.parseInt(snake_head[0])][Integer.parseInt(snake_head[1])] = 3;
                }
                //======================================================================================================

                //A* ===================================================================================================
                Tuple next;
                Tuple Test = new Tuple(apple.x - 1, apple.y - 1);

                try {
                    next = a_star(board, snake_me);
                } catch (NullPointerException npe) {
                    next = null;
                }

                if (next == null) {
                    moveStraight();
                } else if (next.x < snake_me.x) {
                    moveWest();
                } else if (next.x > snake_me.x) {
                    moveEast();
                } else if (next.y < snake_me.y) {
                    moveNorth();
                } else if (next.y > snake_me.y) {
                    moveSouth();
                }
                //======================================================================================================
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void moveNorth() { System.out.println(0); }
    private void moveEast() { System.out.println(3); }
    private void moveSouth() { System.out.println(1); }
    private void moveWest() { System.out.println(2); }

    private void moveStraight() { System.out.println(5); }

    private double step_distance(Tuple p1, Tuple p2) { return Math.ceil(Math.sqrt( (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y) )); }

    private void mark_barriers(int[][] board, int x1, int y1, int x2, int y2) {
        if(x1 > x2) {

            for (int i = x1; i >= x2; i--) {
                board[i][y1] = 1;
            }

        } else if(x1 < x2) {

            for (int i = x1; i <= x2; i++) {
                board[i][y1] = 1;
            }

        } else if(y1 > y2) {

            for (int i = y1; i >= y2; i--) {
                board[x1][i] = 1;
            }

        } else if(y1 < y2) {

            for (int i = y1; i <= y2; i++) {
                board[x1][i] = 1;
            }

        }
    }

    private Tuple a_star(int[][] board, Tuple start) {

        int[][] closed_set = new int[board_height][board_width];
        double[][] h_map = new double[board_height][board_height];
        double[][] g_map = new double[board_height][board_width];
        double[][] f_map = new double[board_height][board_width];

        PriorityQueue<Vertex> open_set = new PriorityQueue<>(new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                if (o1.f < o2.f) {
                    return -1;
                } else if (o1.f > o2.f) {
                    return 1;
                } else {
                    if (o1.h < o2.h) {
                        return -1;
                    } else if (o1.h > o2.h) {
                        return 1;
                    } else {
                        if (o1.g < o2.g) {
                            return -1;
                        } else if (o1.g > o2.g) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                }
            }
        });

        HashMap<Tuple, Tuple> path = new HashMap<>();

        open_set.add(new Vertex(start, 0, 0));
        h_map[start.x][start.y] = 0;
        g_map[start.x][start.y] = 0;
        f_map[start.x][start.y] = g_map[start.x][start.y] + h_map[start.x][start.y];

        while(!open_set.isEmpty()) {
            Tuple current = open_set.poll().point;

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

                if(
                    neighbour.x < 0 ||
                    neighbour.x >= board_width ||
                    neighbour.y < 0 ||
                    neighbour.y >= board_height ||
                    board[neighbour.x][neighbour.y] == 1 ||
                    closed_set[neighbour.x][neighbour.y] == 8
                ) {
                    continue;
                }

                path.put(neighbour, current);
                neighbours.add(neighbour);
            }

            boolean apple_found = false;

            for(Tuple neighbour: neighbours) {
                if(neighbour.equals(apple)) { apple_found = true; break; }

                double neighbour_h = mew_apple * step_distance(neighbour, apple);
                double neighbour_g = g_map[current.x][current.y] + 1;
                double neighbour_f = neighbour_h + neighbour_g;

                boolean ov_found = false;
                for (Vertex ov: open_set) { if (ov.point.equals(neighbours) && f_map[ov.point.x][ov.point.y] < neighbour_f) { ov_found = true; } }
                if(ov_found) { continue; }
                if(closed_set[neighbour.x][neighbour.y] == 8 && f_map[neighbour.x][neighbour.y] < neighbour_f) { continue; }

                open_set.add(new Vertex(neighbour, neighbour_h, neighbour_g));
                h_map[neighbour.x][neighbour.y] = neighbour_h;
                g_map[neighbour.x][neighbour.y] = neighbour_g;
                f_map[neighbour.x][neighbour.y] = neighbour_f;
            }

            closed_set[current.x][current.y] = 8;

            if (apple_found) { break; }
        }

        Tuple backtrace_tuple = apple;
        while(!path.get(backtrace_tuple).equals(start)) { backtrace_tuple = path.get(backtrace_tuple); }
        return backtrace_tuple;
    }
}
