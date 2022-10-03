import java.util.*;

public class Snake {

    private boolean state;
    private int length;
    private int kills;
    private Tuple head;
    private ArrayList<Tuple> turns = new ArrayList<>();
    private Tuple tail;

    public Snake(String description, boolean zombie) {
        int start_idx = zombie ? 0 : 3;

        String[] split_description = description.split(" ");

        if(!zombie) {
            state = split_description[0].charAt(0) == 'a';
            length = Integer.parseInt(split_description[1]);
            kills = Integer.parseInt(split_description[2]);
        }

        head = new Tuple(split_description[start_idx]);
        tail = new Tuple(split_description[split_description.length - start_idx - 1]);

        for(int i = start_idx + 1; i < split_description.length - start_idx - 1; i++) {
            turns.add(new Tuple(split_description[i]));
        }

    }

}
