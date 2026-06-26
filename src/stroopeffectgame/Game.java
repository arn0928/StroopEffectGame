package stroopeffectgame;

/**
 * 控制單局遊戲的完整流程：逐關出題、讀取玩家輸入、判斷對錯、
 * 套用道具效果，並於遊戲結束後彙整統計數據與顯示結算畫面。
 */
public class Game {
    // 遊戲基本設定
    private Player player;
    private int currentLevel;
    private boolean isEndless;
    private String mode;
    private boolean noLimits; // 練習模式與多人模式不計血量、時間與金幣增減
    private java.util.Random random;

    // 本局即時狀態
    private int hp;
    private double remainingTime;
    private double lastTimeDiff = 0.0;
    private int lastCoinGained = 0;
    private int totalCoinChange = 0;
    private boolean endlessNoCoinLoss = false;
    private boolean forbiddenJutsuActive = false;

    // 本局統計數據，遊戲結束後會併入玩家存檔的歷史統計
    private int qCount = 0;
    private int correctCount = 0;
    private int[] colorCorrect = new int[6];
    private int[] colorTotal = new int[6];
    private double[] colorTime = new double[6];
    private int reverseCorrect = 0;
    private int reverseTotal = 0;
    private double reverseTime = 0;
    private double totalPlayTime = 0;

    /**
     * 建立一局新遊戲，並依玩家持有的道具套用初始狀態。
     *
     * @param player     玩家存檔資料，遊戲過程中會直接修改其欄位
     * @param startLevel 起始關卡
     * @param isEndless  是否套用無盡模式規則
     * @param mode       遊戲模式代稱，用於存檔分類與規則判斷
     * @param seed       多人模式使用的同步種子碼；其餘模式可傳入 null
     */
    public Game(Player player, int startLevel, boolean isEndless, String mode, Long seed) {
        this.player = player;
        this.currentLevel = startLevel;
        this.isEndless = isEndless;
        this.mode = mode;
        this.noLimits = mode.equals("PRACTICE") || mode.equals("MULTIPLAYER");

        // 提供種子碼時使用固定亂數序列，確保多人模式各端產生相同關卡
        if (seed != null) {
            this.random = new java.util.Random(seed);
        } else {
            this.random = new java.util.Random();
        }

        this.hp = 5 + (player.hasArmor ? 5 : 0);
        this.remainingTime = 15.0 + (player.hasWaterVideo ? 20.0 : 0.0);

        // 無限制模式（練習／多人）不消耗玩家持有的道具
        if (!noLimits) {
            boolean usedItem = false;
            if (player.hasArmor) { player.hasArmor = false; usedItem = true; }
            if (player.hasWaterVideo) { player.hasWaterVideo = false; usedItem = true; }
            if (isEndless && player.hasUnlimitedRice) {
                player.hasUnlimitedRice = false;
                this.endlessNoCoinLoss = true;
                usedItem = true;
            }
            if (usedItem) player.save();
        }
    }

    /**
     * 執行遊戲主迴圈：逐關出題、讀取輸入、判斷對錯，
     * 直到生命值耗盡、時間耗盡，或（非無盡模式時）抵達第 100 關為止。
     */
    public void start() {
        while ((hp > 0 || noLimits) && (isEndless || currentLevel <= 100)) {

            // 首次進入特定關卡時顯示一次性教學提示
            if (currentLevel == 21 && !player.seenTutorial21) { showTutorial(21); player.seenTutorial21 = true; player.save(); }
            else if (currentLevel == 41 && !player.seenTutorial41) { showTutorial(41); player.seenTutorial41 = true; player.save(); }
            else if (currentLevel == 61 && !player.seenTutorial61) { showTutorial(61); player.seenTutorial61 = true; player.save(); }
            else if (currentLevel == 81 && !player.seenTutorial81) { showTutorial(81); player.seenTutorial81 = true; player.save(); }

            Question q = new Question(currentLevel, isEndless, random);
            boolean isQuestionActive = true;

            while (isQuestionActive) {
                UI.clearScreen();

                System.out.print(UI.BLUE + "第 " + currentLevel + (isEndless ? " 關 (無盡)" : " 關") + UI.RESET + " | ");

                // 無限制模式顯示固定的「無限」狀態列，其餘模式顯示實際血量、時間與金幣
                if (noLimits) {
                    System.out.print(UI.RED + "血量: ∞" + UI.RESET + " | ");
                    System.out.printf(UI.YELLOW + "時間: ∞" + UI.RESET + " | ");
                    System.out.println("金幣: " + player.coins + " (無獎勵)");
                } else {
                    System.out.print(UI.RED + "血量: " + hp + UI.RESET + " | ");
                    if (forbiddenJutsuActive) {
                        System.out.print(UI.PURPLE + "時間: (時間暫停) " + UI.RESET);
                    } else {
                        String timeDiffStr = String.format("%s%.2f秒", lastTimeDiff >= 0 ? "+" : "", lastTimeDiff);
                        System.out.printf(UI.YELLOW + "時間: %.2f秒 (%s) " + UI.RESET, remainingTime, timeDiffStr);
                    }
                    String coinDiffStr = lastCoinGained >= 0 ? "+" + lastCoinGained : String.valueOf(lastCoinGained);
                    System.out.println("| 金幣: " + player.coins + " (" + coinDiffStr + ")");
                }

                System.out.println("--------------------------------------------------");
                System.out.println("操作: 按 1 表示相同 | 按 2 表示不同");
                String functionText = "功能: 按 3 暫停遊戲 | 按 4 儲存並退出" + ((player.skips > 0 && !noLimits) ? " | 按 5 使用全對" : "");
                if (player.hasForbiddenJutsu && !noLimits) {
                    functionText += forbiddenJutsuActive ? " | 封印之書(禁術)已啟動" : " | 按 6 啟動封印之書(禁術)";
                }
                System.out.println(functionText);
                System.out.println("--------------------------------------------------\n");

                UI.displayQuestion(q);

                long startTime = System.currentTimeMillis();
                String input = Main.readSingleKey();
                long endTime = System.currentTimeMillis();

                if (input.equals("3")) {
                    boolean continueGame = showPauseMenu();
                    if (continueGame) {
                        // 防止玩家利用暫停偷看或重抽題目，繼續遊戲時強制重新出題
                        q = new Question(currentLevel, isEndless, random);
                        continue;
                    } else {
                        finishRun(); return;
                    }
                }

                if (input.equals("4")) {
                    finishRun(); return;
                }

                double elapsed = (endTime - startTime) / 1000.0;

                if (input.equals("6")) {
                    if (noLimits) {
                        System.out.println(UI.PURPLE + "無限制模式不需要啟動封印之書(禁術)。" + UI.RESET);
                    } else if (!player.hasForbiddenJutsu) {
                        System.out.println(UI.PURPLE + "尚未購買封印之書(禁術)。" + UI.RESET);
                    } else if (forbiddenJutsuActive) {
                        System.out.println(UI.PURPLE + "封印之書(禁術)已經在本輪遊戲中啟動。" + UI.RESET);
                    } else {
                        // 啟動前的思考時間仍正常扣除，啟動後才開始凍結倒數
                        remainingTime -= elapsed;
                        if (remainingTime <= 0) {
                            System.out.println(UI.RED_BG + UI.WHITE + " 時間到！遊戲結束。 " + UI.RESET);
                            finishRun(); return;
                        }
                        forbiddenJutsuActive = true;
                        lastTimeDiff = 0.0;
                        lastCoinGained = 0;
                        System.out.println(UI.PURPLE + "封印之書(禁術)啟動！本輪遊戲時間停止流逝。" + UI.RESET);
                    }
                    System.out.println("請按任意鍵繼續...");
                    Main.readSingleKey();
                    continue;
                }

                // 記錄本題作答數據，供結束後彙整進玩家的歷史統計
                totalPlayTime += elapsed;
                qCount++;
                colorTotal[q.colorIndex]++;
                colorTime[q.colorIndex] += elapsed;
                if (q.hasExclamation) {
                    reverseTotal++;
                    reverseTime += elapsed;
                }

                if (!noLimits && !forbiddenJutsuActive) {
                    remainingTime -= elapsed;
                }

                if (!noLimits && remainingTime <= 0 && !forbiddenJutsuActive) {
                    System.out.println(UI.RED_BG + UI.WHITE + " 時間到！遊戲結束。 " + UI.RESET);
                    finishRun(); return;
                }

                boolean isTrue = input.equals("1");
                boolean isFalse = input.equals("2");
                boolean useSkip = input.equals("5") && player.skips > 0 && !noLimits;

                if (useSkip) {
                    player.skips--;
                    handleCorrect(elapsed);
                    correctCount++; colorCorrect[q.colorIndex]++; if(q.hasExclamation) reverseCorrect++;
                    isQuestionActive = false;
                } else if ((isTrue && q.expectedAnswer) || (isFalse && !q.expectedAnswer)) {
                    handleCorrect(elapsed);
                    correctCount++; colorCorrect[q.colorIndex]++; if(q.hasExclamation) reverseCorrect++;
                    isQuestionActive = false;
                } else {
                    handleWrong();
                    isQuestionActive = false;
                }
            }

            // 普通模式於存檔點關卡（20/40/60/80）通過後，解鎖對應的下一個存檔點
            if (!noLimits && !isEndless && (currentLevel == 20 || currentLevel == 40 || currentLevel == 60 || currentLevel == 80)) {
                player.maxSavePoint = Math.max(player.maxSavePoint, currentLevel + 1);
                player.save();
            }

            currentLevel++;
        }

        if (!noLimits && hp <= 0) {
            System.out.println(UI.RED + "\n血量歸零，遊戲結束！" + UI.RESET);
        } else if (!isEndless && currentLevel > 100) {
            System.out.println(UI.GREEN + "\n恭喜通關 100 關！" + (!player.cleared100 ? "無盡模式已開啟！" : "") + UI.RESET);
            if (!player.cleared100) {
                System.out.println(UI.YELLOW + "【系統通知】商店有新商品可購買！" + UI.RESET);
            }
            player.cleared100 = true;
        }

        finishRun();
    }

    /** 結束本局遊玩：彙整統計、檢查解鎖條件、顯示結算畫面並存檔。 */
    private void finishRun() {
        endPlaythrough();
        saveRunStats();
        boolean newlyQualified = revealForbiddenBookIfQualified();
        printRunStats(newlyQualified);
        player.save();
    }

    /**
     * 檢查玩家金幣是否已達封印之書(禁術)的解鎖門檻，
     * 若為首次達標則揭曉其商店效果描述。
     *
     * @return 此次結算是否為首次達標
     */
    private boolean revealForbiddenBookIfQualified() {
        if (!player.forbiddenBookRevealed && !player.hasForbiddenJutsu && player.coins >= Player.FORBIDDEN_BOOK_PRICE) {
            player.forbiddenBookRevealed = true;
            return true;
        }
        return false;
    }

    /** 將本局統計數據累加進玩家對應遊戲模式的歷史統計。 */
    private void saveRunStats() {
        Player.ModeStats ms = player.getModeStats(mode);
        ms.totalPlayTime += this.totalPlayTime;
        ms.totalQuestions += this.qCount;
        ms.correctAnswers += this.correctCount;
        for(int i=0; i<6; i++) {
            ms.colorCorrect[i] += this.colorCorrect[i];
            ms.colorTotal[i] += this.colorTotal[i];
            ms.colorTime[i] += this.colorTime[i];
        }
        ms.reverseCorrect += this.reverseCorrect;
        ms.reverseTotal += this.reverseTotal;
        ms.reverseTime += this.reverseTime;
    }

    // 結算畫面左欄固定寬度，需大於最長結果文字的顯示寬度，避免右欄跑版
    private static final int RESULT_LEFT_COLUMN = 32;

    /**
     * 顯示結算畫面，採左右雙欄排版：左欄為本局結果摘要，
     * 右欄為各顏色與反轉術式的詳細統計。
     * 透過 {@link UI#padRight} 依顯示寬度補齊欄位，確保兩欄對齊。
     *
     * @param newlyQualified 玩家是否於本局首次達到封印之書(禁術)的解鎖門檻
     */
    private void printRunStats(boolean newlyQualified) {
        UI.clearScreen();

        String modeName = "";
        switch(mode) {
            case "NORMAL": modeName = "普通模式"; break;
            case "ENDLESS": modeName = "無盡模式"; break;
            case "MULTIPLAYER": modeName = "多人模式"; break;
            case "PRACTICE": modeName = "練習模式"; break;
        }

        double acc = qCount > 0 ? (correctCount * 100.0 / qCount) : 0;
        String coinChangeStr = totalCoinChange >= 0 ? "+" + totalCoinChange : String.valueOf(totalCoinChange);

        String[] resultLines = {
            UI.CYAN + "=== 本次遊玩結果 ===" + UI.RESET,
            String.format("遊玩模式: %s", modeName),
            String.format("生存關卡: 第 %d 關", currentLevel > 100 && !isEndless ? 100 : currentLevel - 1),
            String.format("遊玩時間: %.2f 秒", totalPlayTime),
            "目前金幣: " + UI.YELLOW + player.coins + UI.RESET + " (" + UI.YELLOW + coinChangeStr + UI.RESET + ")",
            String.format("總正確率: %d/%d (%.1f%%)", correctCount, qCount, acc)
        };

        String[] colorNames = {"紅", "黃", "藍", "綠", "紫", "粉"};
        String[] colorCodes = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
        double rAcc = reverseTotal > 0 ? (reverseCorrect * 100.0 / reverseTotal) : 0;
        double rAvg = reverseTotal > 0 ? (reverseTime / reverseTotal) : 0;

        String[] statLines = new String[9];
        statLines[0] = UI.YELLOW + "--- 各顏色統計 ---" + UI.RESET;
        for (int i = 0; i < 6; i++) {
            double cAcc = colorTotal[i] > 0 ? (colorCorrect[i] * 100.0 / colorTotal[i]) : 0;
            double cAvg = colorTotal[i] > 0 ? (colorTime[i] / colorTotal[i]) : 0;
            statLines[i + 1] = String.format("%s%s%s: 正確率 %d/%d (%.1f%%) | 平均 %.2f 秒",
                colorCodes[i], colorNames[i], UI.RESET, colorCorrect[i], colorTotal[i], cAcc, cAvg);
        }
        statLines[7] = UI.PURPLE + "--- 反轉術式 (!) 統計 ---" + UI.RESET;
        statLines[8] = String.format("正確率 %d/%d (%.1f%%) | 平均 %.2f 秒", reverseCorrect, reverseTotal, rAcc, rAvg);

        int rowCount = Math.max(resultLines.length, statLines.length);
        for (int i = 0; i < rowCount; i++) {
            String left = i < resultLines.length ? resultLines[i] : "";
            String right = i < statLines.length ? statLines[i] : "";
            System.out.println(UI.padRight(left, RESULT_LEFT_COLUMN) + right);
        }
        System.out.println();

        if (newlyQualified) {
            System.out.println(UI.PURPLE + "【系統通知】你已有資格購買封印之書(禁術)！" + UI.RESET);
            System.out.println(UI.YELLOW + "商店中的封印之書效果描述已揭曉。" + UI.RESET + "\n");
        }

        // 結算畫面須按兩次 1 才會返回主選單，避免玩家連續按鍵而錯過結算內容
        System.out.println("請按 1 繼續...");
        while (!Main.readSingleKey().equals("1")) { /* 等待玩家按下 1 */ }
        System.out.println("再按一次 1 返回主選單...");
        while (!Main.readSingleKey().equals("1")) { /* 等待玩家按下 1 */ }
    }

    /**
     * 顯示指定關卡的教學提示畫面；顯示期間血量與計時皆維持不變。
     *
     * @param level 觸發教學的關卡數（21、41、61、81）
     */
    private void showTutorial(int level) {
        UI.clearScreen();
        System.out.println(UI.CYAN + "---時間倒數暫停---" + UI.RESET);
        System.out.println(UI.YELLOW + "------遊戲提示------" + UI.RESET);
        System.out.print(UI.RED + "目前狀態: 血量: " + (noLimits ? "∞" : hp) + UI.RESET + " | ");
        System.out.printf(UI.YELLOW + "剩餘時間: " + (noLimits ? "∞" : "%.2f秒") + UI.RESET + " | ", remainingTime);
        System.out.println("金幣: " + player.coins);
        System.out.println("--------------------------------\n");

        if (level == 21) {
            System.out.println("【難度提升】新顏色加入！\n接下來將會出現新顏色與文字：" + UI.PINK + "粉色" + UI.RESET);
        } else if (level == 41) {
            System.out.println(UI.PURPLE + "---！反轉術式！---" + UI.RESET);
            System.out.println("遇到 ！顏色！ 需輸入相反的判斷。");
            String[] words = {"紅", "黃", "藍", "綠", "紫", "粉"};
            String[] fgs = {UI.RED, UI.YELLOW, UI.BLUE, UI.GREEN, UI.PURPLE, UI.PINK};
            int wIdx = random.nextInt(6);
            int cIdx; do { cIdx = random.nextInt(6); } while(cIdx == wIdx);
            String exampleText = fgs[cIdx] + "！" + words[wIdx] + "！" + UI.RESET;
            System.out.println("例如：" + exampleText + " (顯示為" + words[cIdx] + "色的「" + words[wIdx] + "」字)，需按下 1");
        } else if (level == 61) {
            System.out.println("【難度提升】視覺干擾！\n文字後方開始有機率出現隨機背景顏色，請專注文字，不要被干擾！");
        } else if (level == 81) {
            System.out.println("【難度提升】時間緊迫！\n通關獎勵時間減少！每通過一關的獎勵時間將降為 +1.2 秒。");
        }
        System.out.println("\n按任意鍵進入 " + level + " 關");
        Main.readSingleKey();
    }

    /**
     * 顯示暫停選單。
     *
     * @return 選擇繼續遊戲時為 true；選擇儲存並離開時為 false
     */
    private boolean showPauseMenu() {
        while (true) {
            UI.clearScreen();
            System.out.println(UI.YELLOW + "=== 遊戲暫停 ===" + UI.RESET);
            System.out.println(UI.CYAN + "注意：為了防止作弊，解除暫停後將會「重新生成」新題目！" + UI.RESET);
            if (noLimits) {
                System.out.println(UI.RED + "(警告：多人模式下暫停會消耗亂數，導致後續題目與其他玩家不同步！)" + UI.RESET);
            }
            System.out.println("1. 繼續遊戲 (重製當前關卡題目)");
            System.out.println("2. 儲存並離開遊戲 (未使用的全對道具將會消失)");
            System.out.print("\n請選擇操作: ");
            String choice = Main.readSingleKey();
            if (choice.equals("1")) return true;
            else if (choice.equals("2")) return false;
        }
    }

    /**
     * 處理答對的獎勵：恢復計時、發放金幣，並記錄供畫面顯示用的增減量。
     *
     * @param elapsed 本題作答耗時（秒）
     */
    private void handleCorrect(double elapsed) {
        UI.playCorrectEffect();
        if (noLimits) return; // 無限制模式不計算獎勵

        // 80 關後與無盡模式的時間獎勵調降為 1.2 秒，其餘關卡為 1.4 秒
        double timeReward = (isEndless || currentLevel >= 80) ? 1.2 : 1.4;
        if (!forbiddenJutsuActive) { remainingTime += timeReward; lastTimeDiff = timeReward - elapsed; }
        else { lastTimeDiff = 0.0; } // 封印之書啟動中時間不流逝，不顯示時間差

        int coinReward = isEndless ? 5 : 1;
        // 存檔點關卡（20/40/60/80）與通關（100）額外發放 10 金幣
        if (!isEndless && (currentLevel == 20 || currentLevel == 40 || currentLevel == 60 || currentLevel == 80 || currentLevel == 100)) coinReward = 10;
        player.coins += coinReward; lastCoinGained = coinReward;
        totalCoinChange += coinReward;
    }

    /** 處理答錯的懲罰：扣除生命值，並依模式規則扣除金幣。 */
    private void handleWrong() {
        UI.playWrongEffect();
        if (noLimits) return; // 無限制模式不計算懲罰

        hp--; lastTimeDiff = 0.0;
        if (isEndless && !endlessNoCoinLoss) {
            // 扣款上限為玩家目前持有金幣，避免金幣變為負數
            int loss = Math.min(5, player.coins);
            player.coins -= loss; lastCoinGained = -loss;
            totalCoinChange -= loss;
        } else { lastCoinGained = 0; }
    }

    /** 結束遊玩時清除尚未使用的「全對」道具次數。 */
    private void endPlaythrough() { if (player.skips > 0) player.skips = 0; }
}
