import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages level generation, layouts, and difficulty progression.
 * Includes 10 handcrafted arcade layouts closely matching the reference images,
 * plus procedural generation for endless levels beyond Level 10.
 */
public class LevelManager {
    public static final int MAX_CAMPAIGN_LEVELS = 10;
    
    // Grid configuration for 900x600 window
    private static final int COLS = 14;
    private static final int BRICK_WIDTH = 52;
    private static final int BRICK_HEIGHT = 18;
    private static final int GAP_X = 4;
    private static final int GAP_Y = 4;
    private static final int START_X = (900 - (COLS * (BRICK_WIDTH + GAP_X) - GAP_X)) / 2; // 60px
    private static final int START_Y = 75; // Space below top HUD

    /**
     * Calculates recommended ball speed for a given level.
     * Speed scales gently from 4.2 px/frame up to 6.8 px/frame max.
     */
    public double getBallSpeedForLevel(int level) {
        double speed = 4.2 + (level - 1) * 0.22;
        return Math.min(speed, 6.8);
    }

    /**
     * Loads the brick layout for the specified level number.
     */
    public List<Brick> loadLevel(int level) {
        switch (level) {
            case 1:
                return createLevel1Wing();
            case 2:
                return createLevel2FortressRainbow();
            case 3:
                return createLevel3ButterflyX();
            case 4:
                return createLevel4Diamond();
            case 5:
                return createLevel5TwinCastles();
            case 6:
                return createLevel6Staircase();
            case 7:
                return createLevel7SpaceInvader();
            case 8:
                return createLevel8Bullseye();
            case 9:
                return createLevel9CosmicWave();
            case 10:
                return createLevel10Crown();
            default:
                return createProceduralSymmetricLevel(level);
        }
    }

    /**
     * Helper to compute pixel position of a grid cell.
     */
    private int getCellX(int col) {
        return START_X + col * (BRICK_WIDTH + GAP_X);
    }

    private int getCellY(int row) {
        return START_Y + row * (BRICK_HEIGHT + GAP_Y);
    }

    // =========================================================================
    // LEVEL 1: The Wing / Inverted Pyramid (Inspired by Reference Image 1)
    // =========================================================================
    private List<Brick> createLevel1Wing() {
        List<Brick> bricks = new ArrayList<>();
        // Row 0: Full span
        // Silver ends, red, orange, yellow, green, cyan center
        BrickColor[] row0 = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.GREEN, BrickColor.CYAN, BrickColor.GREEN, BrickColor.CYAN,
            BrickColor.GREEN, BrickColor.YELLOW, BrickColor.ORANGE, BrickColor.RED,
            BrickColor.SILVER, BrickColor.SILVER
        };
        for (int c = 0; c < COLS; c++) {
            bricks.add(new Brick(getCellX(c), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, row0[c]));
        }

        // Row 1: Wing step 1
        int[] r1Cols = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        BrickColor[] r1Colors = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.CYAN, BrickColor.GREEN, BrickColor.GREEN, BrickColor.CYAN,
            BrickColor.YELLOW, BrickColor.ORANGE, BrickColor.RED, BrickColor.SILVER
        };
        for (int i = 0; i < r1Cols.length; i++) {
            bricks.add(new Brick(getCellX(r1Cols[i]), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, r1Colors[i]));
        }

        // Row 2: Wing step 2
        int[] r2Cols = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        BrickColor[] r2Colors = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.YELLOW, BrickColor.ORANGE,
            BrickColor.YELLOW, BrickColor.YELLOW, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.RED, BrickColor.SILVER
        };
        for (int i = 0; i < r2Cols.length; i++) {
            bricks.add(new Brick(getCellX(r2Cols[i]), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, r2Colors[i]));
        }

        // Row 3: Wing step 3
        int[] r3Cols = {3, 4, 5, 6, 7, 8, 9, 10};
        BrickColor[] r3Colors = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.YELLOW, BrickColor.ORANGE,
            BrickColor.ORANGE, BrickColor.YELLOW, BrickColor.RED, BrickColor.SILVER
        };
        for (int i = 0; i < r3Cols.length; i++) {
            bricks.add(new Brick(getCellX(r3Cols[i]), getCellY(3), BRICK_WIDTH, BRICK_HEIGHT, r3Colors[i]));
        }

        // Row 4: Wing step 4
        int[] r4Cols = {4, 5, 6, 7, 8, 9};
        BrickColor[] r4Colors = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.ORANGE,
            BrickColor.ORANGE, BrickColor.RED, BrickColor.SILVER
        };
        for (int i = 0; i < r4Cols.length; i++) {
            bricks.add(new Brick(getCellX(r4Cols[i]), getCellY(4), BRICK_WIDTH, BRICK_HEIGHT, r4Colors[i]));
        }

        // Row 5: Wing step 5
        int[] r5Cols = {5, 6, 7, 8};
        BrickColor[] r5Colors = {BrickColor.SILVER, BrickColor.RED, BrickColor.RED, BrickColor.SILVER};
        for (int i = 0; i < r5Cols.length; i++) {
            bricks.add(new Brick(getCellX(r5Cols[i]), getCellY(5), BRICK_WIDTH, BRICK_HEIGHT, r5Colors[i]));
        }

        // Row 6: Wing tip
        bricks.add(new Brick(getCellX(6), getCellY(6), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(7), getCellY(6), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        return bricks;
    }

    // =========================================================================
    // LEVEL 2: Fortress Wall with Slanted Rainbow (Inspired by Reference Image 3)
    // =========================================================================
    private List<Brick> createLevel2FortressRainbow() {
        List<Brick> bricks = new ArrayList<>();
        int rows = 7;
        BrickColor[] rainbow = {
            BrickColor.RED, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.GREEN, BrickColor.CYAN, BrickColor.BLUE, BrickColor.MAGENTA
        };

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < COLS; c++) {
                // Top and bottom rows are solid silver border
                if (r == 0 || r == rows - 1 || c == 0 || c == COLS - 1) {
                    bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                } else {
                    // Slanted rainbow parallelogram in center
                    int rainbowOffset = c - (r + 1);
                    if (rainbowOffset >= 0 && rainbowOffset < rainbow.length) {
                        bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, rainbow[rainbowOffset]));
                    } else {
                        // Flanking silver bricks
                        bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                    }
                }
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 3: Butterfly / X-Wing (Inspired by Reference Image 2)
    // =========================================================================
    private List<Brick> createLevel3ButterflyX() {
        List<Brick> bricks = new ArrayList<>();
        // Row 0: Top wings
        bricks.add(new Brick(getCellX(1), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.RED));
        bricks.add(new Brick(getCellX(11), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.CYAN));
        bricks.add(new Brick(getCellX(12), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 1:
        bricks.add(new Brick(getCellX(1), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(2), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.ORANGE));
        bricks.add(new Brick(getCellX(9), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(10), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.BLUE));
        bricks.add(new Brick(getCellX(11), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 2:
        bricks.add(new Brick(getCellX(2), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(3), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.YELLOW));
        bricks.add(new Brick(getCellX(8), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.MAGENTA));
        bricks.add(new Brick(getCellX(9), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 3: Central bridge
        for (int c = 4; c <= 9; c++) {
            BrickColor colr = (c == 6 || c == 7) ? BrickColor.SILVER : BrickColor.GREEN;
            bricks.add(new Brick(getCellX(c), getCellY(3), BRICK_WIDTH, BRICK_HEIGHT, colr));
        }

        // Row 4: Central bridge lower
        for (int c = 4; c <= 9; c++) {
            BrickColor colr = (c == 6 || c == 7) ? BrickColor.SILVER : BrickColor.SILVER;
            bricks.add(new Brick(getCellX(c), getCellY(4), BRICK_WIDTH, BRICK_HEIGHT, colr));
        }

        // Row 5: Lower wing spread
        bricks.add(new Brick(getCellX(3), getCellY(5), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.MAGENTA));
        bricks.add(new Brick(getCellX(4), getCellY(5), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(8), getCellY(5), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.GREEN));
        bricks.add(new Brick(getCellX(9), getCellY(5), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 6: Lower wing step
        bricks.add(new Brick(getCellX(3), getCellY(6), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        bricks.add(new Brick(getCellX(9), getCellY(6), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.YELLOW));
        bricks.add(new Brick(getCellX(10), getCellY(6), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 7: Lower wing tips
        bricks.add(new Brick(getCellX(10), getCellY(7), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.ORANGE));
        bricks.add(new Brick(getCellX(11), getCellY(7), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        // Row 8:
        bricks.add(new Brick(getCellX(11), getCellY(8), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.RED));
        bricks.add(new Brick(getCellX(12), getCellY(8), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));

        return bricks;
    }

    // =========================================================================
    // LEVEL 4: Diamond Shield
    // =========================================================================
    private List<Brick> createLevel4Diamond() {
        List<Brick> bricks = new ArrayList<>();
        int centerRow = 4;
        int centerCol1 = 6;
        int centerCol2 = 7;

        for (int r = 0; r <= 8; r++) {
            int dist = Math.abs(r - centerRow);
            int halfSpan = 4 - dist;
            for (int c = 0; c < COLS; c++) {
                int colDist = Math.min(Math.abs(c - centerCol1), Math.abs(c - centerCol2));
                if (colDist <= halfSpan) {
                    BrickColor color;
                    if (colDist == halfSpan) {
                        color = BrickColor.SILVER;
                    } else if (colDist == halfSpan - 1) {
                        color = (r % 2 == 0) ? BrickColor.CYAN : BrickColor.BLUE;
                    } else if (colDist == halfSpan - 2) {
                        color = BrickColor.MAGENTA;
                    } else {
                        color = BrickColor.GOLD;
                    }
                    bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, color));
                }
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 5: Twin Castles
    // =========================================================================
    private List<Brick> createLevel5TwinCastles() {
        List<Brick> bricks = new ArrayList<>();
        // Left castle cols: 1, 2, 3; Right castle cols: 10, 11, 12
        for (int r = 0; r < 8; r++) {
            // Battlements on row 0
            if (r == 0) {
                bricks.add(new Brick(getCellX(1), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                bricks.add(new Brick(getCellX(3), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                bricks.add(new Brick(getCellX(10), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                bricks.add(new Brick(getCellX(12), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                continue;
            }

            // Tower bodies
            for (int c : new int[]{1, 2, 3}) {
                BrickColor col = (c == 2) ? BrickColor.RED : BrickColor.SILVER;
                bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, col));
            }
            for (int c : new int[]{10, 11, 12}) {
                BrickColor col = (c == 11) ? BrickColor.BLUE : BrickColor.SILVER;
                bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, col));
            }

            // Connecting high arch on rows 2 and 3
            if (r == 2 || r == 3) {
                for (int c = 5; c <= 8; c++) {
                    BrickColor bridgeColor = (r == 2) ? BrickColor.GOLD : BrickColor.YELLOW;
                    bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, bridgeColor));
                }
            }

            // Central floating gem target on row 5 and 6
            if (r == 5 || r == 6) {
                bricks.add(new Brick(getCellX(6), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.GREEN));
                bricks.add(new Brick(getCellX(7), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.CYAN));
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 6: Interlocking Staircase
    // =========================================================================
    private List<Brick> createLevel6Staircase() {
        List<Brick> bricks = new ArrayList<>();
        BrickColor[] colors = {
            BrickColor.RED, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.GREEN, BrickColor.CYAN, BrickColor.BLUE,
            BrickColor.MAGENTA, BrickColor.SILVER
        };

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < COLS; c++) {
                // Stepped diagonal bands
                if ((c + r) % 4 == 0 || (c - r + 8) % 4 == 0) {
                    BrickColor col = (c == 0 || c == COLS - 1) ? BrickColor.SILVER : colors[r % colors.length];
                    bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, col));
                }
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 7: Space Invader / Alien Boss
    // =========================================================================
    private List<Brick> createLevel7SpaceInvader() {
        List<Brick> bricks = new ArrayList<>();
        // 8x12 Alien layout centered in 14-col grid (cols 1..12)
        int[][] alien = {
            {0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0}, // Antennae
            {0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0}, // Eyes at 3 and 8
            {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
            {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1},
            {0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0}  // Legs
        };

        for (int r = 0; r < alien.length; r++) {
            for (int c = 0; c < alien[r].length; c++) {
                if (alien[r][c] == 1) {
                    int gridCol = c + 1;
                    BrickColor col;
                    if (r == 0 || r == 1) {
                        col = BrickColor.SILVER;
                    } else if (r == 4) {
                        col = BrickColor.CYAN;
                    } else if (r == 7) {
                        col = BrickColor.SILVER;
                    } else {
                        col = BrickColor.GREEN;
                    }
                    bricks.add(new Brick(getCellX(gridCol), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, col));
                }
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 8: Bullseye / Concentric Shields
    // =========================================================================
    private List<Brick> createLevel8Bullseye() {
        List<Brick> bricks = new ArrayList<>();
        for (int r = 0; r < 7; r++) {
            for (int c = 1; c < COLS - 1; c++) {
                int borderDist = Math.min(Math.min(r, 6 - r), Math.min(c - 1, (COLS - 2) - c));
                BrickColor col;
                switch (borderDist) {
                    case 0:
                        col = BrickColor.SILVER;
                        break;
                    case 1:
                        col = BrickColor.MAGENTA;
                        break;
                    case 2:
                        col = BrickColor.CYAN;
                        break;
                    default:
                        col = BrickColor.GOLD;
                        break;
                }
                bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, col));
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 9: Cosmic Sine Wave
    // =========================================================================
    private List<Brick> createLevel9CosmicWave() {
        List<Brick> bricks = new ArrayList<>();
        for (int c = 0; c < COLS; c++) {
            // Wave 1
            int r1 = (int) Math.round(3.0 + 2.5 * Math.sin(c * 0.6));
            // Wave 2
            int r2 = (int) Math.round(3.0 + 2.5 * Math.sin(c * 0.6 + Math.PI));

            bricks.add(new Brick(getCellX(c), getCellY(r1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.CYAN));
            if (r1 + 1 <= 7) {
                bricks.add(new Brick(getCellX(c), getCellY(r1 + 1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.BLUE));
            }

            bricks.add(new Brick(getCellX(c), getCellY(r2), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.ORANGE));
            if (r2 - 1 >= 0) {
                bricks.add(new Brick(getCellX(c), getCellY(r2 - 1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.YELLOW));
            }

            // Silver anchors on left and right borders
            if (c == 0 || c == COLS - 1) {
                bricks.add(new Brick(getCellX(c), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
                bricks.add(new Brick(getCellX(c), getCellY(7), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
            }
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 10: The Champion's Crown
    // =========================================================================
    private List<Brick> createLevel10Crown() {
        List<Brick> bricks = new ArrayList<>();
        // 5 crown peaks: cols 1, 4, 7, 10, 13
        int[] peaks = {1, 4, 7, 10, 12};
        for (int p : peaks) {
            bricks.add(new Brick(getCellX(p), getCellY(0), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        }

        // Peak slopes
        for (int c = 0; c < COLS; c++) {
            bricks.add(new Brick(getCellX(c), getCellY(1), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.GOLD));
        }

        // Jewel rows
        BrickColor[] jewels = {
            BrickColor.SILVER, BrickColor.RED, BrickColor.SILVER, BrickColor.GREEN,
            BrickColor.SILVER, BrickColor.BLUE, BrickColor.SILVER, BrickColor.RED,
            BrickColor.SILVER, BrickColor.GREEN, BrickColor.SILVER, BrickColor.BLUE,
            BrickColor.SILVER, BrickColor.SILVER
        };
        for (int c = 0; c < COLS; c++) {
            bricks.add(new Brick(getCellX(c), getCellY(2), BRICK_WIDTH, BRICK_HEIGHT, jewels[c]));
        }

        // Golden band
        for (int c = 0; c < COLS; c++) {
            bricks.add(new Brick(getCellX(c), getCellY(3), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.GOLD));
        }

        // Reinforced base of crown
        for (int c = 0; c < COLS; c++) {
            bricks.add(new Brick(getCellX(c), getCellY(4), BRICK_WIDTH, BRICK_HEIGHT, BrickColor.SILVER));
        }
        return bricks;
    }

    // =========================================================================
    // LEVEL 11+: Procedural Symmetrical Layout Generator
    // =========================================================================
    private List<Brick> createProceduralSymmetricLevel(int level) {
        List<Brick> bricks = new ArrayList<>();
        Random rand = new Random(level * 31337L);
        int rows = 6 + (level % 3); // 6 to 8 rows
        
        BrickColor[] allColors = {
            BrickColor.RED, BrickColor.ORANGE, BrickColor.YELLOW,
            BrickColor.GREEN, BrickColor.CYAN, BrickColor.BLUE,
            BrickColor.MAGENTA, BrickColor.SILVER, BrickColor.GOLD
        };

        for (int r = 0; r < rows; r++) {
            BrickColor rowColor1 = allColors[rand.nextInt(allColors.length)];
            BrickColor rowColor2 = allColors[rand.nextInt(allColors.length)];

            for (int c = 0; c < COLS / 2; c++) {
                // Symmetrical density check
                if (rand.nextDouble() < 0.78) {
                    BrickColor chosen = (c % 2 == 0) ? rowColor1 : rowColor2;
                    // Left half
                    bricks.add(new Brick(getCellX(c), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, chosen));
                    // Mirrored right half
                    int mirrorCol = COLS - 1 - c;
                    bricks.add(new Brick(getCellX(mirrorCol), getCellY(r), BRICK_WIDTH, BRICK_HEIGHT, chosen));
                }
            }
        }

        // Ensure at least some bricks exist
        if (bricks.isEmpty()) {
            return createLevel1Wing();
        }

        return bricks;
    }
}
