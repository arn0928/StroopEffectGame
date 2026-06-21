package stroopeffectgame;

public class Game {
    private Player player;
    private int currentLevel;
    private boolean isEndless;
    private String mode;
    private boolean noLimits; // 練習/多人模式無限制
    private java.util.Random random;
    
    private int hp;
    private double remainingTime;
    private double lastTimeDiff = 0.0;
    private int lastCoinGained = 0;
    private int totalCoinChange = 0;
    private boolean endlessNoCoinLoss = false;
    private boolean forbiddenJutsuActive = false;

    // --- 本次遊玩的統計變數 ---
    private int qCount = 0;
    private int correctCount = 0;
    private int[] colorCorrect = new int[6];
    private int[] colorTotal = new int[6];
    private double[] colorTime = new double[6];
    private int reverseCorrect = 0;
    private int reverseTotal = 0;
    private double reverseTime = 0;
    private double totalPlayTime = 0;

    public Game(Player player, int startLevel, boolean isEndless, String mode, Long seed) {
        this.player = player;
        this.currentLevel = startLevel;
        this.isEndless = isEndless;
        this.mode = mode;
        this.noLimits = mode.equals("PRACTICE") || mode.equals("MULTIPLAYER");
        
        // 核心：若有提供種子碼，則初始化固定的亂數產生器
        if (seed != null) {
            this.random = new java.util.Random(seed);
        } else {
            this.random = new java.util.Random();
        }
        
        this.hp = 5 + (player.hasArmor ? 5 : 0);
        this.remainingTime = 15.0 + (player.hasWaterVideo ? 20.0 : 0.0);
        
        // 無限制模式不消耗道具
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

    public void start() {
        while ((hp > 0 || noLimits) && (isEndless || currentLevel <= 100)) {
            
            if (currentLevel == 21 && !player.seenTutorial21) { showTutorial(21); player.seenTutorial21 = true; player.save(); }
            else if (currentLevel == 41 && !player.seenTutorial41) { showTutorial(41); player.seenTutorial41 = true; player.save(); }
            else if (currentLevel == 61 && !player.seenTutorial61) { showTutorial(61); player.seenTutorial61 = true; player.save(); }
            else if (currentLevel == 81 && !player.seenTutorial81) { showTutorial(81); player.seenTutorial81 = true; player.save(); }
            
            Question q = new Question(currentLevel, isEndless, random);
            boolean isQuestionActive = true;

            while (isQuestionActive) {
                UI.clearScreen();
                
                System.out.print(UI.BLUE + "第 " + currentLevel + (isEndless ? " 關 (無盡)" : " 關") + UI.RESET + " | ");
                
                // 動態切換無限制與普通顯示
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
                System.out.println("操作: 相同直接按 Enter | 不同按 ' 再按 Enter");
                String functionText = "功能: 輸入 [ 暫停遊戲 | 輸入 ] 儲存並退出" + ((player.skips > 0 && !noLimits) ? " | 按 \\ 使用全對" : "");
                if (player.hasForbiddenJutsu && !noLimits) {
                    functionText += forbiddenJutsuActive ? " | 封印之書(禁術)已啟動" : " | 輸入 = 啟動封印之書(禁術)";
                }
                System.out.println(functionText);
                System.out.println("--------------------------------------------------\n");

                UI.displayQuestion(q); 

                long startTime = System.currentTimeMillis();
                String input = Main.scanner.nextLine().trim().toLowerCase();
                long endTime = System.currentTimeMillis();
                
                if (input.equals("[")) {
                    boolean continueGame = showPauseMenu();
                    if (continueGame) {
                        q = new Question(currentLevel, isEndless, random);
                        continue; 
                    } else {
                        finishRun(); return;
                    }
                }
                
                if (input.equals("]")) {
                    finishRun(); return;
                }

                double elapsed = (endTime - startTime) / 1000.0;

                if (input.equals("=")) {
                    if (noLimits) {
                        System.out.println(UI.PURPLE + "無限制模式不需要啟動封印之書(禁術)。" + UI.RESET);
                    } else if (!player.hasForbiddenJutsu) {
                        System.out.println(UI.PURPLE + "尚未購買封印之書(禁術)。" + UI.RESET);
                    } else if (forbiddenJutsuActive) {
                        System.out.println(UI.PURPLE + "封印之書(禁術)已經在本輪遊戲中啟動。" + UI.RESET);
                    } else {
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
                    System.out.println("請按 Enter 繼續...");
                    Main.scanner.nextLine();
                    continue;
                }
                
                // --- 數據統計紀錄 ---
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

                boolean isTrue = input.equals("");
                boolean isFalse = input.equals("'");
                boolean useSkip = input.equals("\\") && player.skips > 0 && !noLimits;

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

    private void finishRun() {
        endPlaythrough();
        saveRunStats();
        boolean newlyQualified = revealForbiddenBookIfQualified();
        printRunStats(newlyQualified);
        player.save();
    }

    private boolean revealForbiddenBookIfQualified() {
        if (!player.forbiddenBookRevealed && !player.hasForbiddenJutsu && player.coins >= Player.FORBIDDEN_BOOK_PRICE) {
            player.forbiddenBookRevealed = true;
            return true;
        }
        return false;
    }

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

    private static final int RESULT_LEFT_COLUMN = 32;

    private void printRunStats(boolean newlyQualified) {
        UI.clearScreen();
        System.out.println(UI.CYAN + "=== 本次遊玩結果 ===" + UI.RESET);

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

        System.out.println("請按 Enter 繼續...");
        Main.scanner.nextLine();
        System.out.println("再按一次 Enter 返回主選單...");
        Main.scanner.nextLine();
    }

    private void showTutorial(int level) {
        // [原有內容保持不變]
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
            System.out.println("例如：" + exampleText + " (顯示為" + words[cIdx] + "色的「" + words[wIdx] + "」字)，需按下Enter");
        } else if (level == 61) {
            System.out.println("【難度提升】視覺干擾！\n文字後方開始有機率出現隨機背景顏色，請專注文字，不要被干擾！");
        } else if (level == 81) {
            System.out.println("【難度提升】時間緊迫！\n通關獎勵時間減少！每通過一關的獎勵時間將降為 +2 秒。");
        }
        System.out.println("\n按下Enter進入 " + level + " 關");
        Main.scanner.nextLine();
    }

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
            String choice = Main.scanner.nextLine().trim();
            if (choice.equals("1")) return true;
            else if (choice.equals("2")) return false;
        }
    }

    private void handleCorrect(double elapsed) {
        UI.playCorrectEffect();
        if (noLimits) return; // 無限制模式不計算獎勵
        
        double timeReward = (isEndless || currentLevel >= 80) ? 2.0 : 3.0;
        if (!forbiddenJutsuActive) { remainingTime += timeReward; lastTimeDiff = timeReward - elapsed; }
        else { lastTimeDiff = 0.0; }

        int coinReward = isEndless ? 5 : 1;
        if (!isEndless && (currentLevel == 20 || currentLevel == 40 || currentLevel == 60 || currentLevel == 80 || currentLevel == 100)) coinReward = 10;
        player.coins += coinReward; lastCoinGained = coinReward;
        totalCoinChange += coinReward;
    }

    private void handleWrong() {
        UI.playWrongEffect();
        if (noLimits) return; // 無限制模式不計算懲罰
        
        hp--; lastTimeDiff = 0.0;
        if (isEndless && !endlessNoCoinLoss) {
            int loss = Math.min(5, player.coins);
            player.coins -= loss; lastCoinGained = -loss;
            totalCoinChange -= loss;
        } else { lastCoinGained = 0; }
    }
    
    private void endPlaythrough() { if (player.skips > 0) player.skips = 0; }
}
