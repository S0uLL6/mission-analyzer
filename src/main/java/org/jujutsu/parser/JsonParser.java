package org.jujutsu.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jujutsu.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsonParser implements MissionParser {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".json");
    }

    @Override
    public Mission parse(File file) throws Exception {
        JsonNode root = mapper.readTree(file);

        Mission mission = new Mission();
        mission.setMissionId(root.path("missionId").asText(""));
        mission.setDate(root.path("date").asText(""));
        mission.setLocation(root.path("location").asText(""));
        mission.setOutcome(root.path("outcome").asText(""));
        mission.setDamageCost(root.path("damageCost").asLong(0));
        String note = root.path("note").asText(null);
        mission.setNote(note.isBlank() ? null : note);

        JsonNode curseNode = root.path("curse");
        Curse curse = new Curse();
        curse.setName(curseNode.path("name").asText(""));
        curse.setThreatLevel(curseNode.path("threatLevel").asText(""));
        mission.setCurse(curse);

        List<Sorcerer> sorcerers = new ArrayList<>();
        for (JsonNode sNode : root.path("sorcerers")) {
            Sorcerer s = new Sorcerer();
            s.setName(sNode.path("name").asText(""));
            s.setRank(sNode.path("rank").asText(""));
            sorcerers.add(s);
        }
        mission.setSorcerers(sorcerers);

        List<Technique> techniques = new ArrayList<>();
        for (JsonNode tNode : root.path("techniques")) {
            Technique t = new Technique();
            t.setName(tNode.path("name").asText(""));
            t.setType(tNode.path("type").asText(""));
            t.setOwner(tNode.path("owner").asText(""));
            t.setDamage(tNode.path("damage").asLong(0));
            techniques.add(t);
        }
        mission.setTechniques(techniques);

        return mission;
    }
}
