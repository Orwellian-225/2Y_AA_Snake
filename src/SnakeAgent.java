import za.ac.wits.snake.DevelopmentAgent;

import java.util.*;

public class SnakeAgent extends DevelopmentAgent {

    //Scaling coefficients for cost evaluation
    final private float mew_snake = 0;
    final private float mew_zombie = 0;
    final private float mew_barrier = 0;
    final private float mew_apple = 1;

    private Tuple[] snakes;
    private ArrayList<Tuple> barriers = new ArrayList<>();
    private Tuple[] zombies;
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

        snakes = new Tuple[snake_count];
        zombies = new Tuple[zombie_count];

        for (int i = 0; i < snake_count; i++) {
            snakes[i] = new Tuple();
        }

        for (int i = 0; i < zombie_count; i++) {
            zombies[i] = new Tuple();
        }

        while(true) {
            //Parsing Input ========================================================================================
            barriers.clear();
            String[] input = new String[12];

            for( int i = 0; i < 12; i++) {
                input[i] = in.nextLine();
            }

            apple.update(input[0].replace(" ", ","));

            for(int i = 0; i < zombie_count; i++) {
                String[] zombie = input[i + 1].split(" ");

                zombies[i].update(zombie[0]);

                for(int j = 0; j < zombie.length - 1; j++) {
                    mark_barriers(new Tuple(zombie[j]), new Tuple(zombie[j + 1]));
                }
            }

            int me_idx = Integer.parseInt(input[7]);

            for (int i = 0; i < snake_count; i++) {
                String[] snake = input[i + 8].split(" ");

                if(snake[0].equals("dead")) { continue; }

                snakes[i].update(snake[3]);

                for(int j = 3; j < snake.length - 1; j++) {
                    mark_barriers(new Tuple(snake[j]), new Tuple(snake[j + 1]));
                }
            }

            snake_me = snakes[me_idx];
            //======================================================================================================

            //A* ===================================================================================================
            //Tuple test = new Tuple(apple.x - 1, apple.y-2);
            Tuple next = a_star(snake_me);

            if(next == null) { moveStraight(); }
            else if(next.x < snake_me.x) { moveWest(); }
            else if(next.x > snake_me.x) { moveEast(); }
            else if(next.y < snake_me.y) { moveNorth(); }
            else if(next.y > snake_me.y) { moveSouth(); }
            //======================================================================================================

            //printBoard();

        }

    }

    private void moveNorth() { System.out.println(0); }
    private void moveEast() { System.out.println(3); }
    private void moveSouth() { System.out.println(1); }
    private void moveWest() { System.out.println(2); }

    private void moveStraight() { System.out.println(5); }
    //private void moveLeft() { System.out.println(4); }
    //private void moveRight() { System.out.println(6); }

    private double step_distance(Tuple p1, Tuple p2) { return Math.ceil(Math.sqrt( (p1.x - p2.x)*(p1.x - p2.x) + (p1.y - p2.y)*(p1.y - p2.y) )); }
    private double h_cost(Tuple point) {
        double val = 0;
        val += mew_apple * step_distance(point, apple);
        val += mew_snake * array_distance_sum(snakes, point);
        val += mew_zombie * array_distance_sum(zombies, point);
        val += mew_barrier * array_distance_sum(barriers, point);
        return val;
    }

    private double array_distance_sum(Tuple[] points, Tuple point) {
        double val = 0;
        for (Tuple arr_point : points) {
            val += step_distance(point, arr_point);
        }
        return val;
    }
    private double array_distance_sum(ArrayList<Tuple> points, Tuple point) {
        double val = 0;
        for (Tuple arr_point : points) {
            val += step_distance(point, arr_point);
        }
        return val;
    }
    private void mark_barriers(Tuple p1, Tuple p2) {
        if(p1.x > p2.x) {

            for (int i = p1.x; i >= p2.x; i--) {
                barriers.add(new Tuple(i, p1.y));
            }

        } else if(p1.x < p2.x) {

            for (int i = p1.x; i <= p2.x; i++) {
                barriers.add(new Tuple(i, p1.y));
            }

        } else if(p1.y > p2.y) {

            for (int i = p1.y; i >= p2.y; i--) {
                barriers.add(new Tuple(p1.x, i));
            }

        } else if(p1.y < p2.y) {

            for (int i = p1.y; i <= p2.y; i++) {
                barriers.add(new Tuple(p1.x, i));
            }

        }
    }

    private Tuple a_star(Tuple start) {
        PriorityQueue<Vertex> open_list = new PriorityQueue<>((o1, o2) -> {
            if(o1.f < o2.f) {
                return -1;
            } else if (o1.f > o2.f) {
                return 1;
            } else if (o1.f == o2.f) {
                if(o1.h < o2.h) {
                    return -1;
                } else if (o1.h > o2.h) {
                    return 1;
                } else if (o1.h == o2.h) {
                    return Double.compare(o1.g, o2.g);
                }
            }

            return 0;
        });
        ArrayList<Vertex> closed_list = new ArrayList<>();

        HashMap<Tuple, Tuple> path = new HashMap<>();

        open_list.add(new Vertex(start, 0, 0));

        while(!open_list.isEmpty()) {
            //logQueue(open_list);
            Vertex current = open_list.poll();
            ArrayList<Vertex> neighbours = new ArrayList<>();

            for(int i = 0; i < 4; i++) {
                Tuple neighbour_point = new Tuple(current.point.x, current.point.y);

                switch (i) {
                    case 0 -> //North
                            neighbour_point.y -= 1;
                    case 1 -> //East
                            neighbour_point.x += 1;
                    case 2 -> //South
                            neighbour_point.y += 1;
                    case 3 -> //West
                            neighbour_point.x -= 1;
                }

                if (
                        neighbour_point.x < 0 ||
                                neighbour_point.x >= board_width ||
                                neighbour_point.y < 0 ||
                                neighbour_point.y >= board_height ||
                                barriers.contains(neighbour_point) ||
                                closed_list.contains(new Vertex(neighbour_point, 0, 0))
                ) {
                    continue;
                }

                double neighbour_h = h_cost(neighbour_point);
                double neighbour_g = current.g + 1;
                neighbours.add(new Vertex(neighbour_point, neighbour_h, neighbour_g));

                path.put(neighbour_point, current.point);
            }

            boolean found_apple = false;

            for(Vertex neighbour : neighbours) {
                if(neighbour.point.equals(apple)) { found_apple = true; break; }

                Vertex[] open_vertices = new Vertex[open_list.size()];
                open_list.toArray(open_vertices);

                boolean ov_found = false;
                boolean cv_found = false;
                for(Vertex ov: open_vertices) { if(ov.equals(neighbour) && ov.f < neighbour.f) { ov_found = true; } }
                if(ov_found) { continue; }
                for(Vertex cv: closed_list) { if(cv.equals(neighbour) && cv.f < neighbour.f) { cv_found = true; } }
                if(cv_found) { continue; }

                open_list.add(neighbour);
            }

            closed_list.add(current);

            if (found_apple) { break; }

        }

        Tuple backtrace_tuple = apple;
        while(!path.get(backtrace_tuple).equals(start)) { backtrace_tuple = path.get(backtrace_tuple); }
        return backtrace_tuple;
    }
}
