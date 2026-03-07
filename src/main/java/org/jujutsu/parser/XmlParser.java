package org.jujutsu.parser;

import org.jujutsu.model.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XmlParser implements MissionParser {

    @Override
    public boolean supports(File file) {
        return file.getName().toLowerCase().endsWith(".xml");
    }

    @Override
    public Mission parse(File file) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .parse(file);
        doc.getDocumentElement().normalize();

        Element root = doc.getDocumentElement();

        Mission mission = new Mission();
        mission.setMissionId(getText(root, "missionId"));
        mission.setDate(getText(root, "date"));
        mission.setLocation(getText(root, "location"));
        mission.setOutcome(getText(root, "outcome"));
        mission.setDamageCost(parseLong(getText(root, "damageCost")));
        String note = getText(root, "note");
        mission.setNote(note.isBlank() ? null : note);

        NodeList curseNodes = root.getElementsByTagName("curse");
        if (curseNodes.getLength() > 0) {
            Element curseEl = (Element) curseNodes.item(0);
            Curse curse = new Curse();
            curse.setName(getText(curseEl, "name"));
            curse.setThreatLevel(getText(curseEl, "threatLevel"));
            mission.setCurse(curse);
        }

        List<Sorcerer> sorcerers = new ArrayList<>();
        NodeList sorcererNodes = root.getElementsByTagName("sorcerer");
        for (int i = 0; i < sorcererNodes.getLength(); i++) {
            Element el = (Element) sorcererNodes.item(i);
            Sorcerer s = new Sorcerer();
            s.setName(getText(el, "name"));
            s.setRank(getText(el, "rank"));
            sorcerers.add(s);
        }
        mission.setSorcerers(sorcerers);

        List<Technique> techniques = new ArrayList<>();
        NodeList techniqueNodes = root.getElementsByTagName("technique");
        for (int i = 0; i < techniqueNodes.getLength(); i++) {
            Element el = (Element) techniqueNodes.item(i);
            Technique t = new Technique();
            t.setName(getText(el, "name"));
            t.setType(getText(el, "type"));
            t.setOwner(getText(el, "owner"));
            t.setDamage(parseLong(getText(el, "damage")));
            techniques.add(t);
        }
        mission.setTechniques(techniques);

        return mission;
    }

    private String getText(Element parent, String tag) {
        NodeList nl = parent.getElementsByTagName(tag);
        if (nl.getLength() == 0) return "";
        Node node = nl.item(0);
        return node.getTextContent().trim();
    }

    private long parseLong(String s) {
        try { return Long.parseLong(s); }
        catch (NumberFormatException e) { return 0; }
    }
}
