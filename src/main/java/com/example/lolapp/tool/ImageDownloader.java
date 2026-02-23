package com.example.lolapp.tool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;

/**
 * Data DragonのAPIから最新バージョンを自動取得し、
 * 'championFull.json' と必要な画像をすべて自動ダウンロードする完全自動化ツール。
 */
public class ImageDownloader {

    private static final String VERSIONS_URL = "https://ddragon.leagueoflegends.com/api/versions.json";
    private static final String INPUT_JSON = "championFull.json";

    // 保存先ディレクトリ
    private static final String OUTPUT_CHAMPION_DIR = "src/main/resources/static/img/champion/";
    private static final String OUTPUT_SPELL_DIR = "src/main/resources/static/img/spell/";
    private static final String OUTPUT_SPLASH_DIR = "src/main/resources/static/img/splash/";
    private static final String OUTPUT_LOADING_DIR = "src/main/resources/static/img/loading/";
    private static final String OUTPUT_TILES_DIR = "src/main/resources/static/img/tiles/";

    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            // 1. 最新バージョンの自動取得
            System.out.println("Fetching latest Data Dragon version...");
            String latestVersion = getLatestVersion(mapper);
            System.out.println("Latest version: " + latestVersion);

            // 2. 最新の championFull.json を自動ダウンロード
            System.out.println("Downloading latest championFull.json...");
            downloadChampionFullJson(latestVersion);

            File file = new File(INPUT_JSON);
            if (!file.exists()) {
                System.err.println("Error: " + INPUT_JSON + " not found after download attempt.");
                return;
            }

            // 画像の保存先ディレクトリの作成
            new File(OUTPUT_CHAMPION_DIR).mkdirs();
            new File(OUTPUT_SPELL_DIR).mkdirs();
            new File(OUTPUT_SPLASH_DIR).mkdirs();
            new File(OUTPUT_LOADING_DIR).mkdirs();
            new File(OUTPUT_TILES_DIR).mkdirs();

            // 3. ダウンロードしたJSONを読み込んで画像をダウンロード
            JsonNode root = mapper.readTree(file);
            JsonNode data = root.get("data");

            Iterator<String> fieldNames = data.fieldNames();
            int count = 0;

            // URLのプレフィックス
            String championUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/" + latestVersion + "/img/champion/";
            String spellUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/" + latestVersion + "/img/spell/";
            String passiveUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/" + latestVersion + "/img/passive/";
            String splashUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/";
            String loadingUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/img/champion/loading/";
            String tilesUrlPrefix = "https://ddragon.leagueoflegends.com/cdn/img/champion/tiles/";

            System.out.println("Starting image download...");

            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode node = data.get(key);

                // チャンピオン画像のダウンロード
                String champImageFile = key + ".png"; // 例: Ahri.png
                downloadFile(championUrlPrefix + champImageFile, OUTPUT_CHAMPION_DIR + champImageFile, false);

                // --- FiddlesticksのData Dragon仕様（大文字S）対策 ---
                String splashUrl = splashUrlPrefix + key + "_0.jpg";
                String loadingUrl = loadingUrlPrefix + key + "_0.jpg";
                String tileUrl = tilesUrlPrefix + key + "_0.jpg";
                boolean forceOverride = false;

                if ("Fiddlesticks".equals(key)) {
                    // Data Dragonのアセットファイル名はSが大文字になっている
                    String assetKey = "FiddleSticks";
                    splashUrl = splashUrlPrefix + assetKey + "_0.jpg";
                    loadingUrl = loadingUrlPrefix + assetKey + "_0.jpg";
                    tileUrl = tilesUrlPrefix + assetKey + "_0.jpg";
                    forceOverride = true; // PC内に残っているかもしれない古い画像を強制的に上書きする
                }

                String imageFileName = key + "_0.jpg"; // ローカルの保存名は小文字のsに統一

                // スプラッシュアート、ローディング画面、タイル画像のダウンロード
                downloadFile(splashUrl, OUTPUT_SPLASH_DIR + imageFileName, forceOverride);
                downloadFile(loadingUrl, OUTPUT_LOADING_DIR + imageFileName, forceOverride);
                downloadFile(tileUrl, OUTPUT_TILES_DIR + imageFileName, forceOverride);

                // パッシブスキルのダウンロード
                if (node.has("passive")) {
                    String passiveImageFile = node.get("passive").get("image").get("full").asText();
                    downloadFile(passiveUrlPrefix + passiveImageFile, OUTPUT_SPELL_DIR + passiveImageFile, false);
                }

                // 通常スキル(Q, W, E, R)のダウンロード
                if (node.has("spells")) {
                    for (JsonNode spellNode : node.get("spells")) {
                        String spellImageFile = spellNode.get("image").get("full").asText();
                        downloadFile(spellUrlPrefix + spellImageFile, OUTPUT_SPELL_DIR + spellImageFile, false);
                    }
                }

                count++;
                if (count % 10 == 0) {
                    System.out.println("Downloaded data for " + count + " champions...");
                }
            }

            System.out.println("All downloads completed successfully! You are on the latest patch.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getLatestVersion(ObjectMapper mapper) throws Exception {
        URL url = new URI(VERSIONS_URL).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");

        try (InputStream in = conn.getInputStream()) {
            JsonNode versions = mapper.readTree(in);
            if (versions.isArray() && versions.size() > 0) {
                return versions.get(0).asText();
            }
        }
        throw new RuntimeException("Failed to fetch or parse versions.json");
    }

    private static void downloadChampionFullJson(String version) {
        String jsonUrl = "https://ddragon.leagueoflegends.com/cdn/" + version + "/data/ja_JP/championFull.json";
        downloadFile(jsonUrl, INPUT_JSON, true);
    }

    private static void downloadFile(String strUrl, String outputPath, boolean forceOverwrite) {
        File outputFile = new File(outputPath);

        if (!forceOverwrite && outputFile.exists()) {
            return;
        }

        if (forceOverwrite && outputFile.exists()) {
            boolean deleted = outputFile.delete();
            if (!deleted) {
                System.err.println("Warning: 既存のファイル " + outputPath + " の削除に失敗しました。IDE等で開いている場合は閉じてください。");
            }
        }

        try {
            URL url = new URI(strUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (InputStream in = conn.getInputStream();
                        FileOutputStream out = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                System.err.println("Failed to download: " + strUrl + " (HTTP " + conn.getResponseCode() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error downloading: " + strUrl + " - " + e.getMessage());
        }
    }
}