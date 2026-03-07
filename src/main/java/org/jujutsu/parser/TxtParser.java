package org.jujutsu.parser;

import org.jujutsu.model.*;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class TxtParser implements MissionParser {

    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".txt");
    }

    @Override
    public Mission parse(File file) throws Exception {
        List<String> lines = Files.readAllLines(file.toPath());
        Map<String, String> props = new LinkedHashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            int idx = line.indexOf(':');
            if (idx < 0) continue;
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            props.put(key, value);
        }

        Mission mission = new Mission();
        mission.setMissionId(props.getOrDefault("missionId", ""));
        mission.setDate(props.getOrDefault("date", ""));
        mission.setLocation(props.getOrDefault("location", ""));
        mission.setOutcome(props.getOrDefault("outcome", ""));
        mission.setDamageCost(parseLong(props.getOrDefault("damageCost", "0")));
        mission.setNote(props.getOrDefault("note", null));

        Curse curse = new Curse();
        curse.setName(props.getOrDefault("curse.name", ""));
        curse.setThreatLevel(props.getOrDefault("curse.threatLevel", ""));
        mission.setCurse(curse);

        // Parse sorcerers: sorcerer[0].name, sorcerer[0].rank, ...
        List<Sorcerer> sorcerers = new ArrayList<>();
        for (int i = 0; props.containsKey("sorcerer[" + i + "].name"); i++) {
            Sorcerer s = new Sorcerer();
            s.setName(props.get("sorcerer[" + i + "].name"));
            s.setRank(props.getOrDefault("sorcerer[" + i + "].rank", ""));
            sorcerers.add(s);
        }
        mission.setSorcerers(sorcerers);

        // Parse techniques: technique[0].name, etc.
        List<Technique> techniques = new ArrayList<>();
        for (int i = 0; props.containsKey("technique[" + i + "].name"); i++) {
            Technique t = new Technique();
            t.setName(props.get("technique[" + i + "].name"));
            t.setType(props.getOrDefault("technique[" + i + "].type", ""));
            t.setOwner(props.getOrDefault("technique[" + i + "].owner", ""));
            t.setDamage(parseLong(props.getOrDefault("technique[" + i + "].damage", "0")));
            techniques.add(t);
        }
        mission.setTechniques(techniques);

        return mission;
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
