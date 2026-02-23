package com.example.lolapp.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

/**
 * Community Dragonから公式のランクエンブレム画像を一括ダウンロードするツール
 */
public class RankImageDownloader {

    private static final String BASE_URL = "https://raw.communitydragon.org/latest/plugins/rcp-fe-lol-static-assets/global/default/images/ranked-emblem/";
    private static final String OUTPUT_DIR = "src/main/resources/static/img/ranks/";

    private static final String[] RANKS = {
            "iron", "bronze", "silver", "gold", "platinum",
            "emerald", "diamond", "master", "grandmaster", "challenger"
    };

    public static void main(String[] args) {
        System.out.println("=== ランクエンブレム画像のダウンロードを開始します ===");

        // 保存先ディレクトリの作成
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (String rank : RANKS) {
            String fileName = "emblem-" + rank + ".png";
            String fileUrl = BASE_URL + fileName;
            String outputPath = OUTPUT_DIR + fileName;

            System.out.print("Downloading " + fileName + " ... ");
            boolean success = downloadFile(fileUrl, outputPath);
            if (success) {
                System.out.println("OK");
            }
        }

        System.out.println("=== 全てのダウンロードが完了しました！ ===");
        System.out.println("保存先: " + OUTPUT_DIR);
    }

    /**
     * 指定されたURLからファイルをダウンロードし、ローカルに保存する
     */
    private static boolean downloadFile(String strUrl, String outputPath) {
        File outputFile = new File(outputPath);

        // 既に存在する場合は上書きして最新のものにする
        if (outputFile.exists()) {
            outputFile.delete();
        }

        try {
            URL url = new URI(strUrl).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Bot判定回避用のUser-Agent
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
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
                    return true;
                }
            } else {
                System.err.println("Failed (HTTP " + conn.getResponseCode() + ")");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}