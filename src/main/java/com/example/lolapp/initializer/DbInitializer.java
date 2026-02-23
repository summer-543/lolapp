package com.example.lolapp.initializer;

import com.example.lolapp.model.*;
import com.example.lolapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

//CSVファイルからデータベースに初期データをインポートするクラス
@Component
public class DbInitializer implements CommandLineRunner {

    private final ChampionRepository championRepository;
    private final SpellRepository spellRepository;
    private final RoleRepository roleRepository;
    private final LaneRepository laneRepository;
    private final RegionRepository regionRepository;
    private final VisualRepository visualRepository;
    private final CombatStyleRepository combatStyleRepository;

    public DbInitializer(
            ChampionRepository championRepository, SpellRepository spellRepository,
            RoleRepository roleRepository, LaneRepository laneRepository,
            RegionRepository regionRepository, VisualRepository visualRepository,
            CombatStyleRepository combatStyleRepository) {
        this.championRepository = championRepository;
        this.spellRepository = spellRepository;
        this.roleRepository = roleRepository;
        this.laneRepository = laneRepository;
        this.regionRepository = regionRepository;
        this.visualRepository = visualRepository;
        this.combatStyleRepository = combatStyleRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 既にデータがあればスキップ
        if (championRepository.count() > 0)
            return;

        System.out.println("Starting data import...");
        try {
            loadAllData();
            System.out.println("=== CSV Import Completed Successfully! ===");
        } catch (Exception e) {
            System.err.println("Import failed!");
            e.printStackTrace();
        }
    }

    private void loadAllData() throws Exception {
        // マスタデータ
        roleRepository.saveAll(readCsv("data/roles.csv", data -> {
            Role r = new Role();
            r.setId(Long.parseLong(data[0]));
            r.setRoleName(Objects.requireNonNull(data[1]));
            return r;
        }));

        laneRepository.saveAll(readCsv("data/lanes.csv", data -> {
            Lane l = new Lane();
            l.setId(Long.parseLong(data[0]));
            l.setLaneName(Objects.requireNonNull(data[1]));
            return l;
        }));

        regionRepository.saveAll(readCsv("data/regions.csv", data -> {
            Region r = new Region();
            r.setId(Long.parseLong(data[0]));
            r.setRegionName(Objects.requireNonNull(data[1]));
            return r;
        }));

        visualRepository.saveAll(readCsv("data/visuals.csv", data -> {
            Visual v = new Visual();
            v.setId(Long.parseLong(data[0]));
            v.setVisualName(Objects.requireNonNull(data[1]));
            v.setCategory(data.length > 2 ? Objects.requireNonNull(data[2]) : "");
            return v;
        }));

        combatStyleRepository.saveAll(readCsv("data/combat_styles.csv", data -> {
            CombatStyle s = new CombatStyle();
            s.setId(Long.parseLong(data[0]));
            s.setStyleName(Objects.requireNonNull(data[1]));
            return s;
        }));

        // チャンピオン基本データ
        List<Champion> champs = readCsv("data/champions.csv", data -> {
            Champion c = new Champion();
            c.setId(Long.parseLong(data[0]));
            c.setChampKey(Objects.requireNonNull(data[1]));
            c.setName(Objects.requireNonNull(data[2]));
            c.setTitle(Objects.requireNonNull(data[3]));
            c.setDifficulty(Integer.parseInt(data[4]));
            c.setRangeType(Objects.requireNonNull(data[5]));
            c.setDamagePrimary(Objects.requireNonNull(data[6]));
            c.setResourceType(data[7].isEmpty() ? null : data[7]);
            c.setImagePath(Objects.requireNonNull(data[8]));
            c.setSplashPath(data.length > 9 ? Objects.requireNonNull(data[9]) : "");
            c.setLoadingPath(data.length > 10 ? Objects.requireNonNull(data[10]) : "");
            c.setTilePath(data.length > 11 ? Objects.requireNonNull(data[11]) : "");
            return c;
        });
        championRepository.saveAll(champs);

        // スキル
        Map<Long, Champion> champMap = championRepository.findAll().stream()
                .collect(Collectors.toMap(Champion::getId, c -> c));
        spellRepository.saveAll(readCsv("data/spells.csv", data -> {
            Spell s = new Spell();
            s.setId(Long.parseLong(data[0]));
            s.setChampion(champMap.get(Long.parseLong(data[1])));
            s.setSlot(Objects.requireNonNull(data[2]));
            s.setName(Objects.requireNonNull(data[3]));
            s.setDescription(data.length > 4 ? Objects.requireNonNull(data[4]) : "");
            s.setImagePath(data.length > 5 ? Objects.requireNonNull(data[5]) : "");
            return s;
        }));

        // 紐付け (多対多)
        processMapping("data/champion_roles.csv", (c, id) -> {
            roleRepository.findById(id).ifPresent(r -> c.getRoles().add(r));
        });
        processMapping("data/champion_lanes.csv", (c, id) -> {
            laneRepository.findById(id).ifPresent(l -> c.getLanes().add(l));
        });
        processMapping("data/champion_regions.csv", (c, id) -> {
            regionRepository.findById(id).ifPresent(r -> c.getRegions().add(r));
        });
        processMapping("data/champion_visuals.csv", (c, id) -> {
            visualRepository.findById(id).ifPresent(v -> c.getVisuals().add(v));
        });
        processMapping("data/champion_combat_styles.csv", (c, id) -> {
            combatStyleRepository.findById(id).ifPresent(cs -> c.getCombatStyles().add(cs));
        });
    }

    private void processMapping(String path, java.util.function.BiConsumer<Champion, Long> mapper) throws Exception {
        Map<Long, Champion> champMap = championRepository.findAll().stream()
                .collect(Collectors.toMap(Champion::getId, c -> c));

        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists())
            return;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                String[] d = line.split(",", -1);
                Champion c = champMap.get(Long.parseLong(d[0]));
                if (c != null)
                    mapper.accept(c, Long.parseLong(d[1]));
            }
        }
        championRepository.saveAll(new ArrayList<>(champMap.values()));
    }

    private <T> List<T> readCsv(String path, java.util.function.Function<String[], T> mapper) throws Exception {
        List<T> result = new ArrayList<>();
        ClassPathResource res = new ClassPathResource(path);
        if (!res.exists())
            return result;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8))) {
            br.readLine(); // skip header
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue;
                result.add(mapper.apply(line.split(",", -1)));
            }
        }
        return result;
    }
}