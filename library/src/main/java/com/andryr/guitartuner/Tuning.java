/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.guitartuner;

import android.content.Context;

import java.lang.ref.PhantomReference;
import java.util.ArrayList;

/**
 * Created by andry on 24/04/16.
 */
public class Tuning {
    String name;
    Notes.Pitch[] pitches;

    public Tuning(String name, Notes.Pitch[] pitches) {
        this.name = name;
        this.pitches = pitches;
    }

    public Notes.Pitch closestPitch(double freq) {
        Notes.Pitch closest = null;
        double dist = Double.MAX_VALUE;
        for (Notes.Pitch pitch : pitches) {
            double d = Math.abs(freq - pitch.frequency);
            if (d < dist) {
                closest = pitch;
                dist = d;
            }
        }
        return closest;
    }

    public int closestPitchIndex(double freq) {
        int index = -1;
        double dist = Double.MAX_VALUE;
        for (int i = 0; i < pitches.length; i++) {
            Notes.Pitch pitch = pitches[i];
            double d = Math.abs(freq - pitch.frequency);
            if (d < dist) {
                index = i;
                dist = d;
            }
        }
        return index;
    }

    private static String[] STANDARD_TUNING = {"E-3","A-3","D-4","G-4","B-4","E-5"};
    private static String[] OPEN_A_TUNING = {"E-3","A-3","E-4","A-4","C-5","E-5"};

    private static Notes.Pitch[] getPitches(String[] name) {
        ArrayList<Notes.Pitch> result = new ArrayList<>();
        for(String key :name) {
            result.add(Notes.getInstance().get(key));
        }
        return result.toArray(new Notes.Pitch[0]);
    }

    public static Tuning getTuning(Context context, String name) {
        if(name.equals(context.getString(R.string.common))) return new Tuning(name,Notes.getInstance().getAllTuning());
        else if (name.equals(context.getString(R.string.standard_tuning))) return new Tuning(name,getPitches(STANDARD_TUNING));
        else if (name.equals(context.getString(R.string.open_a_tuning))) return new Tuning(name,getPitches(OPEN_A_TUNING));
        return null;
    }


}
