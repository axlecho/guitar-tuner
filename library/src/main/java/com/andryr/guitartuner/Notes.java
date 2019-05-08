package com.andryr.guitartuner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Notes {

   public  static class Pitch {
        public double frequency;
        public String name;
        public int level;
    }

    private Map<String,Pitch> table  =new HashMap<>();
    private static final double STANDARD_A = 440.0f; // A4
    private static final String[] NODE_NAME = {"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
    private static final int MAX_LEVEL = 9;
    private static final double step = Math.pow(2,1.0 / 12.0);
    private Notes() {
        this.initTable();
    }

    private void initTable() {
        double A0 = STANDARD_A / Math.pow(2,4);
        double C0 = A0 / Math.pow(step,9);
        for(int level = 0;level < MAX_LEVEL;level ++) {
            for(int i = 0;i < NODE_NAME.length;i ++) {
                Pitch pitch = new Pitch();
                pitch.name = NODE_NAME[i];
                pitch.level = level;
                pitch.frequency = C0 * Math.pow(step,i + level * 12);

                table.put(NODE_NAME[i] + "-" + level,pitch);
            }
        }
    }

    public Pitch get(String node) {
        return table.get(node);
    }

    public Pitch get(String node,int level) {
        return table.get(node + "-" + level);
    }

    public Pitch[] getAllTuning() {
        List<Pitch> result = new ArrayList<>();
        for(int level = 0;level < MAX_LEVEL;level ++) {
            for (int i = 0; i < NODE_NAME.length; i++) {
                result.add(this.get(NODE_NAME[i],level));
            }
        }
        return result.toArray(new Pitch[0]);
    }


    private static Notes singleton;
    public static Notes getInstance() {
        if (singleton == null) {
            singleton = new Notes();
        }
        return singleton;
    }

}
