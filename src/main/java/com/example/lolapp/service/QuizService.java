package com.example.lolapp.service;

import com.example.lolapp.dto.ChampionDto;
import com.example.lolapp.dto.QuizDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final ChampionService championService;
    private final Map<String, QuizSessionData> sessionMap = new ConcurrentHashMap<>();
    private final Random rand = new Random();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final Map<String, String> JP_REGIONS = Map.ofEntries(
            Map.entry("Demacia", "デマーシア"), Map.entry("Noxus", "ノクサス"), Map.entry("Ionia", "アイオニア"),
            Map.entry("Piltover", "ピルトーヴァー"), Map.entry("Zaun", "ゾウン"), Map.entry("Freljord", "フレヨルド"),
            Map.entry("Bilgewater", "ビルジウォーター"), Map.entry("Shurima", "シュリーマ"), Map.entry("Shadow Isles", "シャドウアイル"),
            Map.entry("Targon", "ターゴン"), Map.entry("Ixtal", "イシュタル"), Map.entry("The Void", "ヴォイド"),
            Map.entry("Bandle City", "バンドルシティ"), Map.entry("Runeterra", "ルーンテラ"));

    private static final Map<String, String> JP_LANES = Map.of(
            "Top", "トップ", "Jungle", "ジャングル", "Mid", "ミッド", "Bot", "ボット", "Support", "サポート");

    private static final Map<String, String> JP_ROLES = Map.of(
            "Fighter", "ファイター", "Tank", "タンク", "Mage", "メイジ", "Assassin", "アサシン", "Marksman", "マークスマン", "Support",
            "サポート");

    public QuizService(ChampionService championService) {
        this.championService = championService;
    }

    public QuizDto.StartResponse startQuiz(QuizDto.StartRequest request) {
        if (sessionMap.size() > 1000)
            sessionMap.clear();
        String sessionId = UUID.randomUUID().toString();
        sessionMap.put(sessionId, new QuizSessionData(request.getGenre(), request.getDifficulty()));
        return new QuizDto.StartResponse(sessionId);
    }

    public QuizDto.QuestionDto getNextQuestion(String sessionId) {
        QuizSessionData sessionData = sessionMap.get(sessionId);
        if (sessionData == null)
            throw new RuntimeException("無効なセッションです。");

        List<ChampionDto> allChamps = championService.getAllChampions();

        List<ChampionDto> availableChamps = allChamps.stream()
                .filter(c -> !sessionData.usedChampionIds.contains(c.getId()))
                .collect(Collectors.toList());

        if (availableChamps.isEmpty()) {
            sessionData.usedChampionIds.clear();
            availableChamps = allChamps;
        }

        ChampionDto target = availableChamps.get(rand.nextInt(availableChamps.size()));
        sessionData.usedChampionIds.add(target.getId());

        int type = determineQuestionType(sessionData);
        String qId = UUID.randomUUID().toString();
        QuizQuestionInternal q;

        switch (type) {
            case 0:
                q = createSplashQuestion(target, allChamps, qId, sessionData.difficulty);
                break;
            case 1:
                q = createTitleQuestion(target, allChamps, qId);
                break;
            case 4:
                q = createRegionQuestion(target, allChamps, qId);
                break;
            case 5:
                q = createLaneQuestion(target, allChamps, qId);
                break;
            case 6:
                q = createRoleQuestion(target, allChamps, qId);
                break;
            case 7:
                q = createDamageQuestion(target, allChamps, qId);
                break;
            case 10:
                q = createNormalChampionIconQuestion(target, allChamps, qId);
                break;
            case 20:
                q = createNormalSpellQuestion1(target, allChamps, qId);
                break;
            case 21:
                q = createNormalSpellQuestion2(target, allChamps, qId);
                break;
            case 30:
                q = createHardSpellQuestion1(target, allChamps, qId);
                break;
            case 31:
                q = createHardSpellQuestion2(target, allChamps, qId);
                break;
            case 32:
                q = createHardSpellQuestion3(target, allChamps, qId);
                break;
            case 33:
                q = createHardSpellQuestion4(target, allChamps, qId);
                break;
            default:
                q = createSplashQuestion(target, allChamps, qId, sessionData.difficulty);
                break;
        }

        sessionData.currentQuestion = q;
        return new QuizDto.QuestionDto(q.id, q.text, q.imageUrl, q.options, q.isSplash);
    }

    public QuizDto.CheckResponse checkAnswer(QuizDto.CheckRequest request) {
        QuizSessionData sessionData = sessionMap.get(request.getSessionId());
        if (sessionData == null || sessionData.currentQuestion == null) {
            throw new RuntimeException("無効なセッションまたは問題です。");
        }

        QuizQuestionInternal q = sessionData.currentQuestion;
        boolean isCorrect = q.correctOptionId.equals(request.getSelectedOptionId());

        if (isCorrect) {
            sessionData.score += 10;
            sessionData.streak++;
        } else {
            sessionData.streak = 0;
            sessionData.lives--;
            if (sessionData.lives <= 0) {
                sessionMap.remove(request.getSessionId());
            }
        }

        boolean isGameOver = sessionData.lives <= 0;
        return new QuizDto.CheckResponse(isCorrect, q.correctOptionId, sessionData.score, sessionData.streak,
                q.explanation, sessionData.lives, isGameOver);
    }

    private int determineQuestionType(QuizSessionData sessionData) {
        int type = 0;
        if ("ALL".equals(sessionData.genre)) {
            int category = rand.nextInt(4);
            if (category == 0)
                type = getVisualQuestionType(sessionData.difficulty);
            else if (category == 1)
                return getSpellQuestionType(sessionData.difficulty);
            else
                type = rand.nextInt(4) + 4;
        } else if ("VISUAL".equals(sessionData.genre)) {
            type = getVisualQuestionType(sessionData.difficulty);
        } else if ("SPELL".equals(sessionData.genre)) {
            return getSpellQuestionType(sessionData.difficulty);
        } else if ("KNOWLEDGE".equals(sessionData.genre)) {
            int[] kTypes = { 1, 4, 5, 6, 7 };
            type = kTypes[rand.nextInt(kTypes.length)];
        }
        return type;
    }

    private int getVisualQuestionType(String difficulty) {
        if ("HARD".equals(difficulty)) {
            return 0;
        } else {
            return rand.nextInt(2) == 0 ? 0 : 10;
        }
    }

    private int getSpellQuestionType(String difficulty) {
        if ("HARD".equals(difficulty)) {
            return 30 + rand.nextInt(4);
        } else {
            return 20 + rand.nextInt(2);
        }
    }

    // --- ヘルパーメソッド群 ---

    private Map<String, Object> getRandomSpell(ChampionDto champ) {
        if (champ.getSpells() == null || champ.getSpells().isEmpty())
            return null;
        return mapper.convertValue(champ.getSpells().get(rand.nextInt(champ.getSpells().size())),
                new TypeReference<>() {
                });
    }

    private List<Map<String, Object>> getWrongSpells(List<ChampionDto> all, ChampionDto excludeChamp, int count) {
        List<ChampionDto> available = new ArrayList<>(all);
        available.removeIf(
                c -> c.getId().equals(excludeChamp.getId()) || c.getSpells() == null || c.getSpells().isEmpty());
        Collections.shuffle(available);
        List<Map<String, Object>> wrongs = new ArrayList<>();
        for (int i = 0; i < count && i < available.size(); i++) {
            ChampionDto c = available.get(i);
            Map<String, Object> spell = mapper.convertValue(c.getSpells().get(rand.nextInt(c.getSpells().size())),
                    new TypeReference<>() {
                    });
            spell.put("champName", c.getName());
            wrongs.add(spell);
        }
        return wrongs;
    }

    private QuizQuestionInternal createNormalChampionIconQuestion(ChampionDto target, List<ChampionDto> all,
            String id) {
        String correctImg = (target.getTilePath() != null && !target.getTilePath().isEmpty()) ? target.getTilePath()
                : target.getImagePath();
        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), null, correctImg);

        List<QuizDto.OptionDto> wrongOpts = getWrongOptions(all, List.of(target), 3).stream()
                .map(c -> {
                    String img = (c.getTilePath() != null && !c.getTilePath().isEmpty()) ? c.getTilePath()
                            : c.getImagePath();
                    return new QuizDto.OptionDto(UUID.randomUUID().toString(), null, img);
                })
                .collect(Collectors.toList());

        return buildInternalOptions(id, target.getName() + " はどれ？", null, correctOpt, wrongOpts, "");
    }

    private QuizQuestionInternal createNormalSpellQuestion1(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "NORMAL");

        String correctText = target.getName() + " の " + spell.get("slot");
        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), correctText, null);
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(),
                        s.get("champName") + " の " + s.get("slot"), null))
                .collect(Collectors.toList());

        return buildInternalOptions(id, "「" + spell.get("name") + "」は、誰のどのスキル？", (String) spell.get("imagePath"),
                correctOpt, wrongOpts, "");
    }

    private QuizQuestionInternal createNormalSpellQuestion2(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "NORMAL");

        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), (String) spell.get("name"),
                (String) spell.get("imagePath"));
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(), (String) s.get("name"),
                        (String) s.get("imagePath")))
                .collect(Collectors.toList());

        return buildInternalOptions(id, target.getName() + " の " + spell.get("slot") + " はどれ？", null, correctOpt,
                wrongOpts, "");
    }

    private QuizQuestionInternal createHardSpellQuestion1(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "HARD");

        String correctText = target.getName() + " の " + spell.get("slot");
        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), correctText, null);
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(),
                        s.get("champName") + " の " + s.get("slot"), null))
                .collect(Collectors.toList());

        return buildInternalOptions(id, "このスキルアイコンは、誰のどのスキル？", (String) spell.get("imagePath"), correctOpt, wrongOpts,
                "");
    }

    private QuizQuestionInternal createHardSpellQuestion2(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "HARD");

        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), (String) spell.get("name"),
                null);
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(), (String) s.get("name"), null))
                .collect(Collectors.toList());

        return buildInternalOptions(id, "このスキルアイコンのスキル名は？", (String) spell.get("imagePath"), correctOpt, wrongOpts, "");
    }

    private QuizQuestionInternal createHardSpellQuestion3(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "HARD");

        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), null,
                (String) spell.get("imagePath"));
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(), null, (String) s.get("imagePath")))
                .collect(Collectors.toList());

        return buildInternalOptions(id, "「" + spell.get("name") + "」のアイコンはどれ？", null, correctOpt, wrongOpts, "");
    }

    private QuizQuestionInternal createHardSpellQuestion4(ChampionDto target, List<ChampionDto> all, String id) {
        Map<String, Object> spell = getRandomSpell(target);
        if (spell == null)
            return createSplashQuestion(target, all, id, "HARD");

        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), null,
                (String) spell.get("imagePath"));
        List<QuizDto.OptionDto> wrongOpts = getWrongSpells(all, target, 3).stream()
                .map(s -> new QuizDto.OptionDto(UUID.randomUUID().toString(), null, (String) s.get("imagePath")))
                .collect(Collectors.toList());

        return buildInternalOptions(id, target.getName() + " の " + spell.get("slot") + " のアイコンはどれ？", null, correctOpt,
                wrongOpts, "");
    }

    private QuizQuestionInternal createSplashQuestion(ChampionDto target, List<ChampionDto> all, String id,
            String difficulty) {
        List<ChampionDto> wrongs = getWrongOptions(all, List.of(target), 3);
        String imageUrl = (target.getSplashPath() != null && !target.getSplashPath().isEmpty()) ? target.getSplashPath()
                : target.getImagePath();
        String questionText = "HARD".equals(difficulty) ? "スプラッシュアートの一部から、チャンピオンを特定せよ。" : "このチャンピオンは誰？";
        QuizQuestionInternal q = buildInternal(id, questionText, imageUrl, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
        q.isSplash = true;
        return q;
    }

    private QuizQuestionInternal createTitleQuestion(ChampionDto target, List<ChampionDto> all, String id) {
        List<ChampionDto> wrongs = getWrongOptions(all, List.of(target), 3);
        return buildInternal(id, "二つ名が「" + target.getTitle() + "」であるチャンピオンは誰？", null, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
    }

    private QuizQuestionInternal createRegionQuestion(ChampionDto target, List<ChampionDto> all, String id) {
        if (target.getRegions() == null || target.getRegions().isEmpty())
            return createTitleQuestion(target, all, id);
        String region = target.getRegions().get(rand.nextInt(target.getRegions().size()));
        String jpRegion = JP_REGIONS.getOrDefault(region, region);
        List<ChampionDto> wrongs = getWrongOptions(
                all.stream().filter(c -> c.getRegions() == null || !c.getRegions().contains(region)).toList(),
                List.of(target), 3);
        return buildInternal(id, "「" + jpRegion + "」のチャンピオンは？", null, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
    }

    private QuizQuestionInternal createLaneQuestion(ChampionDto target, List<ChampionDto> all, String id) {
        if (target.getLanes() == null || target.getLanes().isEmpty())
            return createSplashQuestion(target, all, id, "NORMAL");
        String lane = target.getLanes().get(rand.nextInt(target.getLanes().size()));
        String jpLane = JP_LANES.getOrDefault(lane, lane);
        List<ChampionDto> wrongs = getWrongOptions(
                all.stream().filter(c -> c.getLanes() == null || !c.getLanes().contains(lane)).toList(),
                List.of(target), 3);
        return buildInternal(id, "主に「" + jpLane + "」へ行くチャンピオンはどれ？", null, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
    }

    private QuizQuestionInternal createRoleQuestion(ChampionDto target, List<ChampionDto> all, String id) {
        if (target.getRoles() == null || target.getRoles().isEmpty())
            return createDamageQuestion(target, all, id);
        String role = target.getRoles().get(rand.nextInt(target.getRoles().size()));
        String jpRole = JP_ROLES.getOrDefault(role, role);
        List<ChampionDto> wrongs = getWrongOptions(
                all.stream().filter(c -> c.getRoles() == null || !c.getRoles().contains(role)).toList(),
                List.of(target), 3);
        return buildInternal(id, "この中から「" + jpRole + "」のチャンピオンを選べ。", null, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
    }

    private QuizQuestionInternal createDamageQuestion(ChampionDto target, List<ChampionDto> all, String id) {
        if (target.getDamagePrimary() == null)
            return createSplashQuestion(target, all, id, "NORMAL");
        String type = target.getDamagePrimary();
        String displayType = type.equals("Physical") ? "物理" : (type.equals("Magic") ? "魔法" : "混合");
        List<ChampionDto> wrongs = getWrongOptions(
                all.stream().filter(c -> c.getDamagePrimary() == null || !c.getDamagePrimary().equals(type)).toList(),
                List.of(target), 3);
        return buildInternal(id, "メインダメージが「" + displayType + "」のチャンピオンはどれ？", null, target.getName(),
                wrongs.stream().map(ChampionDto::getName).toList(), "");
    }

    private List<ChampionDto> getWrongOptions(List<ChampionDto> source, List<ChampionDto> excludes, int count) {
        List<ChampionDto> available = new ArrayList<>(source);
        available.removeAll(excludes);
        if (available.size() < count) {
            Set<ChampionDto> fallback = new HashSet<>(available);
            fallback.addAll(championService.getAllChampions());
            fallback.removeAll(excludes);
            available = new ArrayList<>(fallback);
        }
        Collections.shuffle(available);
        return available.stream().limit(count).collect(Collectors.toList());
    }

    private QuizQuestionInternal buildInternal(String id, String text, String imageUrl, String correctText,
            List<String> wrongTexts, String explanation) {
        QuizDto.OptionDto correctOpt = new QuizDto.OptionDto(UUID.randomUUID().toString(), correctText, null);
        List<QuizDto.OptionDto> wrongOpts = wrongTexts.stream()
                .map(w -> new QuizDto.OptionDto(UUID.randomUUID().toString(), w, null))
                .collect(Collectors.toList());
        return buildInternalOptions(id, text, imageUrl, correctOpt, wrongOpts, explanation);
    }

    private QuizQuestionInternal buildInternalOptions(String id, String text, String imageUrl,
            QuizDto.OptionDto correctOpt, List<QuizDto.OptionDto> wrongOpts, String explanation) {
        QuizQuestionInternal q = new QuizQuestionInternal();
        q.id = id;
        q.text = text;
        q.imageUrl = imageUrl;
        q.correctOptionId = correctOpt.getOptionId();
        q.explanation = explanation;

        List<QuizDto.OptionDto> opts = new ArrayList<>();
        opts.add(correctOpt);
        opts.addAll(wrongOpts);
        Collections.shuffle(opts);
        q.options = opts;
        return q;
    }

    private static class QuizSessionData {
        String genre;
        String difficulty;
        int score = 0;
        int streak = 0;
        int lives;
        Set<Long> usedChampionIds = new HashSet<>();
        QuizQuestionInternal currentQuestion;

        QuizSessionData(String genre, String difficulty) {
            this.genre = genre;
            this.difficulty = difficulty;
            this.lives = "HARD".equals(difficulty) ? 1 : 3;
        }
    }

    private static class QuizQuestionInternal {
        String id, text, imageUrl, correctOptionId, explanation;
        boolean isSplash;
        List<QuizDto.OptionDto> options;
    }
}