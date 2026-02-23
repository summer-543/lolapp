package com.example.lolapp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Data Dragonの 'championFull.json' を読み込み、
 * アプリケーション用のCSVファイル群を生成するツール。
 */
public class DataDragonConverter {

    private static final String INPUT_JSON = "championFull.json";
    private static final String OUTPUT_DIR = "src/main/resources/data/";

    private static final Map<String, Integer> ROLE_MAP = Map.of(
            "Fighter", 1, "Tank", 2, "Mage", 3, "Assassin", 4, "Marksman", 5, "Support", 6);

    // 【更新】公式データと実態が乖離しているチャンピオンの個別上書きリスト
    // ※Mageタグを持つキャラ（Azir, TwistedFate等）は自動判定されるため削除済み
    private static final Map<String, String> DAMAGE_OVERRIDES = Map.ofEntries(
            // --- 元からあるもの（自動判定が難しいもの） ---
            Map.entry("Belveth", "Physical"),
            Map.entry("Diana", "Magic"), // RiotのタグにMageが無いためリストに復帰
            Map.entry("Fizz", "Magic"),
            Map.entry("Gwen", "Magic"),
            Map.entry("Qiyana", "Physical"),
            Map.entry("Teemo", "Magic"),
            Map.entry("Kaisa", "Mixed"),
            Map.entry("Kayle", "Mixed"),
            Map.entry("KogMaw", "Mixed"),
            Map.entry("Shyvana", "Mixed"),
            Map.entry("Udyr", "Mixed"),
            Map.entry("Varus", "Mixed"),
            Map.entry("Volibear", "Mixed"),
            Map.entry("Warwick", "Mixed"),
            Map.entry("Yone", "Mixed"),

            // --- 今回の「Mage優先」による副作用の修正 ---
            Map.entry("Ezreal", "Physical"),
            Map.entry("Corki", "Physical"),

            // --- Physical判定に修正（計算上Mixedになりがちなブルーザー・タンク） ---
            Map.entry("Gnar", "Physical"),
            Map.entry("Irelia", "Physical"),
            Map.entry("KSante", "Physical"),
            Map.entry("Nasus", "Physical"),
            Map.entry("Sion", "Physical"),
            Map.entry("Yorick", "Physical"),

            // --- Magic判定に修正するタンク・サポート勢 ---
            Map.entry("Alistar", "Magic"),
            Map.entry("Blitzcrank", "Magic"),
            Map.entry("Braum", "Magic"),
            Map.entry("Leona", "Magic"),
            Map.entry("Nautilus", "Magic"),
            Map.entry("Rammus", "Magic"),
            Map.entry("Rell", "Magic"),
            Map.entry("Taric", "Magic"), // サポートタンク統一のため追加
            Map.entry("Thresh", "Magic") // 念のため再指定
    );

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            File file = new File(INPUT_JSON);

            if (!file.exists()) {
                System.err.println("Error: " + INPUT_JSON + " not found. Please download it from Data Dragon.");
                return;
            }

            JsonNode root = mapper.readTree(file);
            JsonNode data = root.get("data");

            List<ChampionData> champions = new ArrayList<>();
            Iterator<String> fieldNames = data.fieldNames();

            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode node = data.get(key);
                champions.add(parseChampion(node));
            }

            // ID順（英語キー順）にソートして採番
            champions.sort(Comparator.comparing(c -> c.key));
            for (int i = 0; i < champions.size(); i++) {
                champions.get(i).id = (long) (i + 1);
            }

            new File(OUTPUT_DIR).mkdirs();
            writeChampionsCsv(champions);
            writeChampionRolesCsv(champions);
            writeSpellsCsv(champions);

            System.out.println("Conversion completed. Files generated in " + OUTPUT_DIR);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ChampionData parseChampion(JsonNode node) {
        ChampionData c = new ChampionData();
        c.key = node.get("id").asText();
        c.name = node.get("name").asText();
        c.title = node.get("title").asText();

        JsonNode info = node.get("info");
        c.difficulty = info.get("difficulty").asInt();
        // difficultyを1-3スケールに簡易変換
        if (c.difficulty <= 3)
            c.difficulty = 1;
        else if (c.difficulty <= 7)
            c.difficulty = 2;
        else
            c.difficulty = 3;

        // 主ダメージの判定ロジック
        // 公式の attack と magic の数値を比較して判定する
        int attackScore = info.get("attack").asInt();
        int magicScore = info.get("magic").asInt();

        // 差が2より大きければ特化型、それ以外はMixed（混合）とする
        if (attackScore > magicScore + 2) {
            c.damagePrimary = "Physical";
        } else if (magicScore > attackScore + 2) {
            c.damagePrimary = "Magic";
        } else {
            c.damagePrimary = "Mixed";
        }

        c.partype = node.get("partype").asText();

        double range = node.get("stats").get("attackrange").asDouble();
        c.rangeType = range >= 300 ? "Ranged" : "Melee";

        if (node.has("tags")) {
            for (JsonNode tag : node.get("tags")) {
                c.tags.add(tag.asText());
            }
        }

        // タグによる補正の優先順位（Mageを優先）
        if (c.tags.contains("Mage") && !c.damagePrimary.equals("Physical")) {
            c.damagePrimary = "Magic";
        } else if (c.tags.contains("Marksman") && !c.damagePrimary.equals("Magic")) {
            c.damagePrimary = "Physical";
        }

        // 個別上書きリストに存在する場合は、その値で上書きする
        if (DAMAGE_OVERRIDES.containsKey(c.key)) {
            c.damagePrimary = DAMAGE_OVERRIDES.get(c.key);
        }

        // スキルのパース部分は省略せずに記述
        if (node.has("spells")) {
            int index = 0;
            String[] slots = { "Q", "W", "E", "R" };
            for (JsonNode spellNode : node.get("spells")) {
                SpellData s = new SpellData();
                s.slot = slots[index++];
                s.name = spellNode.get("name").asText();
                s.description = spellNode.get("description").asText()
                        .replaceAll("\n", "").replaceAll(",", "、");
                s.imageId = spellNode.get("image").get("full").asText();
                c.spells.add(s);
            }
        }
        if (node.has("passive")) {
            SpellData p = new SpellData();
            p.slot = "P";
            p.name = node.get("passive").get("name").asText();
            p.description = node.get("passive").get("description").asText()
                    .replaceAll("\n", "").replaceAll(",", "、");
            p.imageId = node.get("passive").get("image").get("full").asText();
            c.spells.add(0, p);
        }

        return c;
    }

    private static void writeChampionsCsv(List<ChampionData> list) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_DIR + "champions.csv"))) {
            writer.println(
                    "id,champ_key,name,title,difficulty,range_type,damage_primary,resource_type,image_path,splash_path,loading_path,tile_path");
            for (ChampionData c : list) {
                writer.printf(
                        "%d,%s,%s,%s,%d,%s,%s,%s,/img/champion/%s.png,/img/splash/%s_0.jpg,/img/loading/%s_0.jpg,/img/tiles/%s_0.jpg%n",
                        c.id, c.key, c.name, c.title, c.difficulty, c.rangeType, c.damagePrimary, c.partype, c.key,
                        c.key, c.key, c.key);
            }
        }
    }

    private static void writeChampionRolesCsv(List<ChampionData> list) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_DIR + "champion_roles.csv"))) {
            writer.println("champion_id,role_id");
            for (ChampionData c : list) {
                for (String tag : c.tags) {
                    Integer roleId = ROLE_MAP.get(tag);
                    if (roleId != null) {
                        writer.printf("%d,%d%n", c.id, roleId);
                    }
                }
            }
        }
    }

    private static void writeSpellsCsv(List<ChampionData> list) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_DIR + "spells.csv"))) {
            writer.println("id,champion_id,slot,name,description,image_path");
            long spellId = 1;
            for (ChampionData c : list) {
                for (SpellData s : c.spells) {
                    writer.printf("%d,%d,%s,%s,%s,/img/spell/%s%n",
                            spellId++, c.id, s.slot, s.name, s.description, s.imageId);
                }
            }
        }
    }

    static class ChampionData {
        Long id;
        String key;
        String name;
        String title;
        int difficulty;
        String rangeType;
        String damagePrimary;
        String partype;
        List<String> tags = new ArrayList<>();
        List<SpellData> spells = new ArrayList<>();
    }

    static class SpellData {
        String slot;
        String name;
        String description;
        String imageId;
    }
}